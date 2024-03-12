package kr.xit.biz.sisul.service;

import static egovframework.com.cmm.util.EgovDateUtil.formatDate;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.NiceCiWrkDiv;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO.FmcExcelUpload;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO.FmcInfExcel;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO.FmcInfExcelRslt;
import kr.xit.biz.ens.model.cmm.TmplatManage;
import kr.xit.biz.ens.model.cntc.CntcDTO;
import kr.xit.biz.ens.model.nice.NiceCiDTO.IpinCiResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.IpinCiResEncData;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.biz.sisul.mapper.IBizSisulMapper;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulRequest;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResDtlData;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResMstData;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.service.AbstractService;
import kr.xit.core.support.utils.Checks;
import kr.xit.core.support.xlsx.StreamingReader;
import kr.xit.ens.cmm.CmmEnsUtils;
import kr.xit.ens.nice.cmm.CmmNiceCiUtils;
import kr.xit.ens.nice.service.INiceCiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jasypt.encryption.StringEncryptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.sisul.service
 * fileName    : BizSisulService
 * author      : limju
 * date        : 2023-09-05
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-05    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizSisulService extends AbstractService implements IBizSisulService {
    private final static int FMC_EXCEL_DATA_START_ROW = 1;
    private final static int FMC_EXCEL_CELL_CNT = 58;

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final StringEncryptor jasyptStringEncryptor;
    private final INiceCiService niceCiService;

    private final IBizSisulMapper mapper;

    @Transactional
    @Override
    public String fmcExcelUpload(FmcExcelUpload fileReq) {
        String rtnMsg = "";
        final Set<ConstraintViolation<FmcExcelUpload>> errors = validator.validate(fileReq);
        if (!errors.isEmpty()) {
            throw BizRuntimeException.create(errors.stream()
                .map(row -> String.format("%s=%s", row.getPropertyPath(), row.getMessageTemplate()))
                .toList().toString());
        }

        // 템플릿 정보 조회
        final List<FmcInfExcel> fmcExcels = parsingFmcExcel(fileReq.getFiles()[0]);
        checkValidated(fmcExcels);

        //23.11.22 jhseo: 하나의 엑셀에 unitySndngMastrId 여러개일 경우 동일한 unitySndngMastrId Row만 처리
        List<String> unitySndngMastrIdList = fmcExcels.stream().map(FmcInfExcel::getUnitySndngMastrId).collect(Collectors.toList());
        List<String> unitySndngMastrIds = unitySndngMastrIdList.stream().distinct().collect(Collectors.toList());
        for (String unitySndngMastrId : unitySndngMastrIds){
            String tmplatId = "";
            int excelCnt = 0;
            for(FmcInfExcel dto : fmcExcels){
                if(unitySndngMastrId.equals(dto.getUnitySndngMastrId())){
                    excelCnt++;
                    if("".equals(tmplatId)) tmplatId = dto.getTmplatId();
                }
            }

            final TmplatManage tmpDTO = mapper.selectDeptInfoByTmplId(tmplatId)
                .orElseThrow(() -> BizRuntimeException.create("템플릿 정보를 찾을 수 없습니다."));

            // Ci 변환
            fileReq.setSignguCode(tmpDTO.getSignguCode());
            fileReq.setFfnlgCode(tmpDTO.getFfnlgCode());
            fileReq.setTry1(tmpDTO.getTry1());
            fileReq.setPostDlvrSe(tmpDTO.getPostDlvrSe());
            fileReq.setPostTmplatCode(tmpDTO.getPostTmplatCode());

            List<FmcInfExcelRslt> ciRslts = null;
            if(Pattern.matches("([\\w]{4}[1-2])", tmplatId)) {
                ciRslts = getConvertCis(fileReq, fmcExcels, unitySndngMastrId, excelCnt);
            }

            // CNTC 테이블 insert
            final String rtnMsgSub = makeCntc(ciRslts, fmcExcels, fileReq, unitySndngMastrId, excelCnt);
            fileReq.setSignguCode(null);
            fileReq.setFfnlgCode(null);
            fileReq.setTry1(null);
            fileReq.setPostDlvrSe(null);
            fileReq.setPostTmplatCode(null);
            if("".equals(rtnMsg)) rtnMsg = unitySndngMastrId + ":" + rtnMsgSub;
            if(!"".equals(rtnMsg)) rtnMsg = rtnMsg + ", " + unitySndngMastrId + ":" + rtnMsgSub;
        }
//        final TmplatManage tmpDTO = mapper.selectDeptInfoByTmplId(
//                fmcExcels.get(0).getTmplatId())
//            .orElseThrow(() -> BizRuntimeException.create("템플릿 정보를 찾을 수 없습니다."));
//
//        // Ci 변환
//        fileReq.setSignguCode(tmpDTO.getSignguCode());
//        fileReq.setFfnlgCode(tmpDTO.getFfnlgCode());
//        fileReq.setTry1(tmpDTO.getTry1());
//        fileReq.setPostDlvrSe(tmpDTO.getPostDlvrSe());
//        fileReq.setPostTmplatCode(tmpDTO.getPostTmplatCode());
//
//        List<FmcInfExcelRslt> ciRslts = null;
//        if(Pattern.matches("([\\w]{4}[1-2])", fmcExcels.get(0).getTmplatId())) {
//            ciRslts = getConvertCis(fileReq, fmcExcels);
//        }
//
//        // CNTC 테이블 insert
//        final String rtnMsg = makeCntc(ciRslts, fmcExcels, fileReq);
//        fileReq.setPostDlvrSe(null);
//        fileReq.setSignguCode(null);
//        fileReq.setFfnlgCode(null);
        return rtnMsg;
    }

    @Override
    @Transactional(readOnly = true)
    public RsltSisulResMstData findSndngResultMaster(final RsltSisulRequest reqDTO) {
        CmmEnsUtils.validate(reqDTO);
        return mapper.selectSndngResultMaster(reqDTO)
            .orElseThrow(() -> BizRuntimeException.create(String.format("[%s] 데이타를 찾을 수 없습니다", reqDTO.getUnitySndngMastrId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RsltSisulResDtlData> findSndngResultDetails(final RsltSisulRequest reqDTO) {
        CmmEnsUtils.validate(reqDTO);
        List<RsltSisulResDtlData> resList = mapper.selectSndngResultDetails(reqDTO);
        if(resList.isEmpty())   throw BizRuntimeException.create(String.format("[%s] 데이타를 찾을 수 없습니다", reqDTO.getUnitySndngMastrId()));
        return mapper.selectSndngResultDetails(reqDTO);
    }

    //------------------------------------------------------------------------
    // private method
    //------------------------------------------------------------------------
    private List<FmcInfExcel> parsingFmcExcel(MultipartFile mf) {
        final List<FmcInfExcel> fmcExcels = new ArrayList<>();

        try(
            final InputStream is = mf.getInputStream();
            final Workbook wb = StreamingReader.builder().open(is)) {
            final Sheet sheet = wb.getSheetAt(0);
            if(sheet.getLastRowNum() <= FMC_EXCEL_DATA_START_ROW)   throw BizRuntimeException.create("업로드할 데이타가 존재하지 않습니다.");;

            final Iterator<Row> it = sheet.iterator();

            // 0 row
            it.hasNext();
            final Row row = it.next();
            if(row.getPhysicalNumberOfCells() != FMC_EXCEL_CELL_CNT)  throw BizRuntimeException.create("엑셀파일의 셀정보가 부정확 합니다(Cell 갯수 오류)");

            // 1 row
            it.hasNext();
            it.next();


            while(it.hasNext()) {
                final Row r = it.next();
                int i = 0;

                fmcExcels.add( new FmcInfExcel(
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i++)),
                    strCellValue(r.getCell(i))
                ));
            }
        }catch (IOException ie){
            throw BizRuntimeException.create(ie.getMessage());
        }
        return fmcExcels;
    }


    /**
     * <pre>
     * Cell Data Value 타입에 따라 String으로 형변환 Return
     * @param cell Cell
     * @return String
     * </pre>
     */
    private String strCellValue(Cell cell){
        String cv = "";
        // 셀에 있는 데이터들을 타입별로 분류해서 cv 처리
        if (cell == null || cell.getCellType() == CellType._NONE || cell.getCellType() == CellType.BLANK) {
            //별도 처리 없음
        }else{
            switch(cell.getCellType()){
                case STRING:
                    cv = cell.getStringCellValue().trim();
                    break;
                case NUMERIC:
                    int numbervalue=(int)cell.getNumericCellValue();
                    cv = String.valueOf(numbervalue);
                    break;
            }
        }
        return cv;
    }

    /**
     * <pre>
     * 전자고지 대상 CI 변환 : 카카오, KT-BC인 경우만 필요
     * -> tmplatId(우편물발송구분)의 마지막 자리가 1 또는 2인 경우만
     * -> 실패시 code, message에 에러 set
     * @param fileReq FmcExcelUpload
     * @param fmcExcels List<FmcInfExcel>
     * @return List<FmcInfExcelRslt>
     * </pre>
     */
    private List<FmcInfExcelRslt> getConvertCis(final FmcExcelUpload fileReq, final List<FmcInfExcel> fmcExcels, final String unitySndngMastrId, final int excelCnt){
        List<FmcInfExcelRslt> rslts = new ArrayList<>();
        int errCnt = 0;

        for(FmcInfExcel dto : fmcExcels) {
            //23.11.22 jhseo: 하나의 엑셀에 unitySndngMastrId 여러개일 경우 동일한 unitySndngMastrId Row만 처리
            if(unitySndngMastrId.equals(dto.getUnitySndngMastrId())){
                try {
                    IpinCiResDataBody dataBody = niceCiService.requestCi(
                        NiceCiRequest.builder()
                            .signguCode(fileReq.getSignguCode())
                            .ffnlgCode(fileReq.getFfnlgCode())
                            .juminId(dto.getRecveJuminno())
                            .build()
                    );
                    IpinCiResEncData resEncData = dataBody.toEncData();
                    rslts.add(
                        FmcInfExcelRslt.builder()
                            .unitySndngDetailId(dto.getUnitySndngDetailId())
                            .ci1(resEncData.getCi1())
                            .ci2(resEncData.getCi2())
                            .resultCd(dataBody.getResultCd())
                            .message(CmmNiceCiUtils.getFromResultCd(NiceCiWrkDiv.CI, dataBody.getResultCd()))
                            .build()
                    );
                } catch (BizRuntimeException e){
                    errCnt++;
                    rslts.add(
                        FmcInfExcelRslt.builder()
                            .unitySndngDetailId(dto.getUnitySndngDetailId())
                            .resultCd(e.getCode())
                            .message(e.getMessage())
                            .build()
                    );
                }
            }
        }
        fileReq.setSndngCo(excelCnt);

        if(excelCnt != rslts.size())    throw BizRuntimeException.create(
            String.format(
                "CI 전환 오류[변환대상-%d건, 변환완료-%d건, 변환오류-%d건]",
                excelCnt,
                rslts.size()-errCnt,
                errCnt)
        );
        return rslts;
    }

    /**
     * <pre>
     * 전자고지 대상(CNTC) 생성
     * -> 실패시 code, message에 에러 set
     * @param ciRslts List<FmcInfExcelRslt>
     * @return String
     * </pre>
     */
    private String makeCntc(final List<FmcInfExcelRslt> ciRslts, final List<FmcInfExcel> fmcExcels, FmcExcelUpload fileReq, final String unitySndngMastrId, final int excelCnt){
        int mst = 0;
        int errCnt = 0;
        String mpc = null;
        final String register = StringUtils.defaultString(fileReq.getRegister(),"");
//        String unitySndngMastrId = "";
        String[] ppMasterJson = new String[2];
        for(FmcInfExcel dto : fmcExcels){
            //23.11.22 jhseo: 하나의 엑셀에 unitySndngMastrId 여러개일 경우 동일한 unitySndngMastrId Row만 처리
            if(unitySndngMastrId.equals(dto.getUnitySndngMastrId())){
                //최초 1회 CNTC master 생성
//                if(StringUtils.isNotEmpty(dto.getUnitySndngMastrId()) && mst == 0){
//                    unitySndngMastrId = dto.getUnitySndngMastrId();

                // 교통시설운영처는 마감일시 = 발송일자 + 30일
                String closDt = "";
                if(ApiConstants.SignguCode.TRAFFIC.getCode().equals(fileReq.getSignguCode())) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    LocalDateTime dateTime = LocalDateTime.parse(dto.getSndngDt(), formatter).plusDays(30);
                    closDt = dateTime.format(formatter).substring(0,8) + "235959";
                } else {
                    closDt = dto.getNapPd() + "235959";
                }

                if(mst == 0){
                    fileReq.setUnitySndngMastrId(unitySndngMastrId);
                    mapper.insertCntcSndngMst(
                            CntcDTO.SndngMst.builder()
                                    .unitySndngMastrId(dto.getUnitySndngMastrId())
                                    .tmplatId(dto.getTmplatId())
                                    .sndngDt(dto.getSndngDt())
                                    .closDt(closDt)
                                    .sndngCo(excelCnt)
                                    .sndngProcessSttus(ApiConstants.SndngProcessStatus.ACCEPT.getCode())
                                    .register(register)
                                    .build()
                    );
                }
                mst++;

                // POST-PLUS(전자우편) 일 경우 tb_ens_post_plus_json 테이블 insert
                if("POST-PLUS".equals(fileReq.getTry1())){
                    //최초 1회 POST-PLUS master json 생성
                    if(mst == 1){
                        ppMasterJson = PPMasterJson(dto, fileReq, excelCnt);
                    }
                    String ppDetailJson[] = PPDetailJson(Integer.toString(mst),dto, fileReq.getSignguCode());
                    mapper.insertPostPlusJson(
                            CntcDTO.PostPlusJson.builder()
                                    .unitySndngDetailId(dto.getUnitySndngDetailId())
                                    .serviceCd("PST")
                                    .conKey(unitySndngMastrId)
                                    .sn(Integer.toString(mst))
                                    .masterCols(ppMasterJson[0])
                                    .masterRows(ppMasterJson[1])
                                    .detailCols(ppDetailJson[0])
                                    .detailRows(jasyptStringEncryptor.encrypt(ppDetailJson[1]))
                                    .register(register)
                                    .build()
                    );
                }else {
                    switch (fileReq.getSignguCode()) {
                        case "88328": //교통시설운영처
                            mpc = jsonCn1(dto);
                            break;
                        case "88316": //추모시설운영처
                            mpc = jsonCn2(dto);
                            break;
                        default:
                            break;
                    }
                }
                //CNTC detail 생성
                mapper.insertCntcSndngDtl(
                        CntcDTO.SndngDtl.builder()
                                .unitySndngMastrId(unitySndngMastrId)
                                .unitySndngDetailId(dto.getUnitySndngDetailId())
                                .tmplatId(dto.getTmplatId())
                                .mainCode(dto.getTaxNum1()+dto.getTaxNum2()+dto.getTaxNum3()+dto.getTaxNum4()) // 납세번호로 자료 찾기
                                .tmpltMsgData(dto.getNapAmountTotal())      // 고객사 요청 사항 납부금액 표기
                                .useInsttIdntfcId(dto.getGojiDetailNm())    // 교통시설운영처 제목 가변 항목
                                .mobilePageCn(mpc)
                                .register(register)
                                .build()
                );
            }
        }

        if(Checks.isNotEmpty(ciRslts)) {
            for(FmcInfExcelRslt dto : ciRslts) {
                if(StringUtils.isEmpty(String.join("", dto.getCi1(), dto.getCi2()))) {
                    errCnt++;
                }
                mapper.insertCi(dto);
            }
        }


        return String.format(
            "전자고지 대상 %d건 Upload[완료-%d건, 오류-%d건]",
            excelCnt,
            excelCnt-errCnt,
            errCnt);
    }

    void checkValidated(List<FmcInfExcel> fmcExcels){
        AtomicInteger idx = new AtomicInteger(1);
        fmcExcels.forEach(d -> {
            if(Checks.isEmpty(d.getUnitySndngMastrId()) || d.getUnitySndngMastrId().length() > 20){
                throw BizRuntimeException.create("fail.api.excel.upload.data", new String[]{
                    String.valueOf(idx.get()), "unitySndngMastrId(파일유일키)", "필수(최대 20자)", d.getUnitySndngMastrId()});
            }

            if(Checks.isEmpty(d.getUnitySndngDetailId()) || d.getUnitySndngDetailId().length() > 20){
                throw BizRuntimeException.create("fail.api.excel.upload.data", new String[]{
                    String.valueOf(idx.get()), "unitySndngDetailId(파일유일키)", "필수(최대 20자)", d.getUnitySndngDetailId()});
            }

            if(Checks.isEmpty(d.getSndngDt()) || d.getSndngDt().length() != 14 || !Pattern.matches("20([2-9][0-9][0-1][1-9][0-3][0-9][0-2][0-9][0-5][0-9][0-5][0-9])", d.getSndngDt())){
                throw BizRuntimeException.create("fail.api.excel.upload.data", new String[]{
                    String.valueOf(idx.get()), "sndngDt(우편물요청일시)", "필수(14자)", d.getSndngDt()});
            }

            if(Checks.isEmpty(d.getTmplatId()) || d.getTmplatId().length() != 5){
                throw BizRuntimeException.create("fail.api.excel.upload.data", new String[]{
                    String.valueOf(idx.get()), "tmplatId(우편물발송구분)", "필수(5자)", d.getTmplatId()});
            }

            // KAKAO or KT-BC의 경우에만 주민번호 필요
            if(Pattern.matches("([\\w]{4}[1-2])", d.getTmplatId())){
                if(Checks.isEmpty(d.getRecveJuminno()) || d.getRecveJuminno().length() != 13){
                    throw BizRuntimeException.create("fail.api.excel.upload.data", new String[]{
                        String.valueOf(idx.get()), "recveJuminno(수취인주민번호)", "필수(13자리)", d.getRecveJuminno()});
                }
            }
            idx.getAndIncrement();
        });
    }


    private String jsonCn1(FmcInfExcel dto){
        String jsonCn = "{"
                +  "\"details\": ["
                +    "{"
                +      "\"title\": \"제목 : "+ Checks.checkVal(dto.getGojiNm(),"") + "(" + Checks.checkVal(dto.getGojiDetailNm(),"") +")\","
                +      "\"item_type\": \"SUBJECT_TEXT\","
                +      "\"elements\": ["
                +        "\"\""
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"내용 : "+ Checks.checkVal(dto.getGojiNm(),"") + " " + Checks.checkVal(dto.getGojiGubun(),"") +" 안내입니다.\","
                +      "\"item_type\": \"SUBJECT_TEXT\","
                +      "\"elements\": ["
                +        "\"\""
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"내역\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"properties\": {"
                +           "\"style\" : {"
                +               "\"highlight\" : {"
                +                       "\"" + Checks.checkVal(dto.getTaxNum1(),"") + Checks.checkVal(dto.getTaxNum2(),"") + Checks.checkVal(dto.getTaxNum3(),"") + Checks.checkVal(dto.getTaxNum4(), "") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "}"
                +               "}"
                +           "}"
                +      "},"
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"부과대상\","
                +          "\"value\": \""+ Checks.checkVal(dto.getRecevDetailAddr(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"납부자\","
                +          "\"value\": \""+ Checks.checkVal(dto.getRecevNm(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"납부금액\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmountTotal(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"- 사용료\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmount1(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"- "+ Checks.checkVal(dto.getNapAmountDetailNm1(),"") +"\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmount2(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"- "+ Checks.checkVal(dto.getNapAmountDetailNm2(),"") +"\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmount3(),"") +"\","
                +          "\"level\": 1"
                +        "},"
/*                +        "{"
                +          "\"key\": \""+ Checks.checkVal(dto.getNapAmountDetailNm3(),"") +"\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmount4(),"") +"\","
                +          "\"level\": 1"
                +        "},"*/
                +        "{"
                +          "\"key\": \""+ Checks.checkVal(dto.getNapGubun1(),"") +"\","
                +          "\"value\": \""+ Checks.checkVal(formatDate(dto.getNapPd().substring(0,8), "-"),"") +"까지\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \""+ Checks.checkVal(dto.getNapGubun2(),"") +"\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNapAmountTotal(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"납세번호\","
                +          "\"value\": \""+ Checks.checkVal(dto.getTaxNum1(),"") + Checks.checkVal(dto.getTaxNum2(),"") + Checks.checkVal(dto.getTaxNum3(),"") + Checks.checkVal(dto.getTaxNum4(),"")
                                          +"\\\\n※ ETAX 납부 시 29자리 입력\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"서울시 세외수입 납부 전용계좌\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"properties\": {"
                +           "\"style\" : {"
                +               "\"highlight\" : {"
                +                       "\"" + Checks.checkVal(dto.getWVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getSVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getHVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "}"
                +               "}"
                +           "}"
                +      "},"
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"우리은행\","
                +          "\"value\": \""+ Checks.checkVal(dto.getWVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"신한은행\","
                +          "\"value\": \""+ Checks.checkVal(dto.getSVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"하나은행\","
                +          "\"value\": \""+ Checks.checkVal(dto.getHVacct(),"") +"\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\" : \"\" ,"
                +      "\"item_type\" : \"PRE_TEXT\","
                +      "\"elements\" : \" \\n※ 해당 고지서에 대한 연체료(공유재산법 의거 연7%~15%)는 최종 납부 이후 일할 정산하여 별도 고지서로 부과됩니다.\""
                +    "},"
                +    "{"
                +      "\"title\": \"세외수입 납부방법 안내\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"가상계좌\\\\n납부\","
                +          "\"value\": \"고지서에 기재된 가상계좌로 이체,납부\\\\n고지서 금액과 일치해야 이체 가능\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"모바일\\\\n어플\","
                +          "\"value\": \"앱,PLAY스토어에서 (서울시 세금납부) 앱 설치\\\\n계좌이체(전 은행), 신용카드, 간편결제 납부\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"웹페이지\","
                +          "\"value\": \"인터넷 검색창에 (이택스) 검색\\\\n계좌이체(전 은행), 신용카드, 간편결제 납부\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"ARS\","
                +          "\"value\": \"납부 전용전화(1599-3900) 안내에 따라 납부\\\\n계좌이체 및 신용카드 납부 가능\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"은행방문\","
                +          "\"value\": \"전국은행(한국은행 제외), 우체국, 농협, 새마을금고, 신협, 수협, 산림조합\\\\n은행창구 또는 현금인출기에서 계좌이체\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"연체료\","
                +      "\"item_type\": \"TABLE\","
                +      "\"elements\": {"
                +        "\"head\": ["
                +          "\"구분\","
                +          "\"1개월\\\\n미만\","
                +          "\"3개월\\\\n미만\","
                +          "\"6개월\\\\n미만\","
                +          "\"6개월\\\\n이상\""
                +        "],"
                +        "\"rows\": ["
                +          "["
                +            "\"연체료율\","
                +            "\"7%\","
                +            "\"8%\","
                +            "\"9%\","
                +            "\"10%\""
                +          "]"
                +        "]"
                +      "}"
                +    "},"
                +    "{"
                +      "\"title\" : \"\" ,"
                +      "\"item_type\" : \"PRE_TEXT\","
                +      "\"elements\" : \" \\n※ 2022.04.19.까지 연체료율 10%~15%\\n※ 체납 시 보중보험 청구, 재산 압류, 사용료 청구소송 등 조치가 이루어지며,"
                                         + " 금융기관 및 신용정보기관에 연체정보가 등록되어 금융거래 등 각종 경제활동에 불이익이 있을 수 있습니다.\""
                +    "}"
                +  "]"
                +"}";
        return jsonCn;
    }

    private String jsonCn2(FmcInfExcel dto){
        String buContent3 = Checks.checkVal(dto.getBuContent3().replace("$&$", "\\n"),"");

        String jsonCn = "{"
                +  "\"details\": ["
                +    "{"
                +      "\"title\": \""+ Checks.checkVal(dto.getGojiNm(),"") + " " + Checks.checkVal(dto.getGojiGubun(),"") +"\","
                +      "\"item_type\": \"SUBJECT_TEXT\","
                +      "\"elements\": ["
                +        "\"\""
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"내역\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"properties\": {"
                +           "\"style\" : {"
                +               "\"highlight\" : {"
                +                       "\"" + Checks.checkVal(dto.getTaxNum1(),"") + Checks.checkVal(dto.getTaxNum2(),"") + Checks.checkVal(dto.getTaxNum3(),"") + Checks.checkVal(dto.getTaxNum4(), "") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "}"
                +               "}"
                +           "}"
                +      "},"
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"납부자\","
                +          "\"value\": \""+ Checks.checkVal(dto.getRecevNm(),"") +"\","
                +          "\"level\": 1"
                +        "},"
/*                +        "{"
                +          "\"key\": \"고지서\","
                +          "\"value\": \""+ Checks.checkVal(dto.getGojiNm(),"") +"("+ Checks.checkVal(dto.getGojiGubun(),"") +")"+"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"고지서 상세\","
                +          "\"value\": \""+ Checks.checkVal(dto.getGojiDetailNm(),"") +"\","
                +          "\"level\": 1"
                +        "},"*/
                +        "{"
                +          "\"key\": \"자료관리번호\","
                +          "\"value\": \""+ Checks.checkVal(dto.getDataSn(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"관리비 기간\","
                +          "\"value\": \""+ Checks.checkVal(dto.getCostPd(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"부과내역\","
                +          "\"value\": \""+ buContent3 +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"납세번호\","
                +          "\"value\": \""+ Checks.checkVal(dto.getTaxNum1(),"") + Checks.checkVal(dto.getTaxNum2(),"") + Checks.checkVal(dto.getTaxNum3(),"") + Checks.checkVal(dto.getTaxNum4(),"")
                                          +"\\\\n※ ETAX 납부 시 29자리 입력\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"납부 금액\","
                +      "\"item_type\": \"TABLE\","
                +      "\"properties\": {"
                +           "\"style\" : {"
                +               "\"highlight\" : {"
                +                       "\"합 계\" : {"
                +                           "\"font-weight\" : \"bold\""
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getNapAmountTotal(),"") + "\" : {"
                +                           "\"font-weight\" : \"bold\""
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getNapAftAmountTotal(),"") + "\" : {"
                +                           "\"font-weight\" : \"bold\""
                +                       "}"
                +               "}"
                +           "}"
                +      "},"
                +      "\"elements\": {"
                +        "\"head\": ["
                +          "\"구 분\","
                +          "\"납기내\","
                +          "\"납기후\""
                +        "],"
                +        "\"rows\": ["
                +          "["
                +            "\"" + Checks.checkVal(dto.getNapAmountDetailNm1(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAmount1(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAftAmount1(),"") + "\""
                +          "],"
                +          "["
                +            "\"" + Checks.checkVal(dto.getNapAmountDetailNm2(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAmount2(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAftAmount2(),"") + "\""
                +          "],"
/*                +          "["
                +            "\"\","
                +            "\"" + Checks.checkVal(dto.getNapAmount3(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAftAmount3(),"") + "\""
                +          "],"*/
                +          "["
                +            "\"합 계\","
                +            "\"" + Checks.checkVal(dto.getNapAmountTotal(),"") + "\","
                +            "\"" + Checks.checkVal(dto.getNapAftAmountTotal(),"") + "\""
                +          "],"
                +          "["
                +            "\"기 한\","
                +            "\"" + Checks.checkVal(formatDate(dto.getNapPd().substring(0,8), "-"),"") + "까지\","
                +             "\"" + Checks.checkVal(dto.getNapAftPd(),"") + "\""
                +          "]"
                +        "]"
                +      "}"
                +    "},"
                +    "{"
                +      "\"title\" : \"\" ,"
                +      "\"item_type\" : \"PRE_TEXT\","
                +      "\"elements\" : \" \\n※  서울특별시 장사 등에 관한 조례 제11조에 의거, 사용료 또는 관리비를 납부기한이 경과한 날부터 6개월 이내에 납부하지 아니한 경우 사용허가 취소될 수 있습니다.\""
                +    "},"
                +    "{"
                +      "\"title\": \"고객 전용계좌\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"properties\": {"
                +           "\"style\" : {"
                +               "\"highlight\" : {"
                +                       "\"" + Checks.checkVal(dto.getNVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getKVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getWVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getSVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getHVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "},"
                +                       "\"" + Checks.checkVal(dto.getPVacct(),"") + "\" : {"
                +                           "\"use-clipboard\" : true"
                +                       "}"
                +               "}"
                +           "}"
                +      "},"
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"농협\","
                +          "\"value\": \""+ Checks.checkVal(dto.getNVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"국민\","
                +          "\"value\": \""+ Checks.checkVal(dto.getKVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"우리\","
                +          "\"value\": \""+ Checks.checkVal(dto.getWVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"신한\","
                +          "\"value\": \""+ Checks.checkVal(dto.getSVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"KEB하나\","
                +          "\"value\": \""+ Checks.checkVal(dto.getHVacct(),"") +"\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +          "\"key\": \"우체국\","
                +          "\"value\": \""+ Checks.checkVal(dto.getPVacct(),"") +"\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "},"
                +    "{"
                +      "\"title\": \"서울시 세외수입 납부방법 안내\","
                +      "\"item_type\": \"KEY_VALUE\","
                +      "\"elements\": ["
                +        "{"
                +          "\"key\": \"가상계좌\\\\n납부\","
                +          "\"value\": \"- 고지서에 기재된 고객 전용(가상)계좌로 납부\\\\n(단, 다른 은행 이체에 따른 수수료는 납세자가 부담하셔야 합니다.)\\\\n- 타인이 이체 하셔도 서울세외납부자성명으로 수납됩니다.\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"서울시 인터넷\\\\n세금납부\","
                +          "\"value\": \"- 이택스에 접속하여 로그인 후 계좌이체나 신용카드 납부\\\\n이택스: https://etax.seoul.go.kr\\\\n※ 비회원의 경우에는 고지서에 표시된 납세번호 입력 후 납부\","
                +          "\"level\": 1"
                +        "},"
                +        "{"
                +         "\"key\": \"ARS 납부\\\\n1599-3900\","
                +          "\"value\": \"- 서울시 지방세 세외수입 조회납부 선택하여 ARS안내에 따라 납부\\\\n- 계좌이체(신한은행만 가능)나 신용카드(국내 모든카드)로 납부\","
                +          "\"level\": 1"
                +        "}"
                +      "]"
                +    "}"
                +  "]"
                +"}";
        return jsonCn;
    }

    private String[] PPMasterJson(FmcInfExcel dto, FmcExcelUpload fileReq, int excelCnt){
        String[] returnJson = new String[2];
        switch (fileReq.getSignguCode()) {
            case "88328": //교통시설운영처
                returnJson[0] = "["
                        + "\"버전\",\"테스트여부\",\"서비스\",\"연계식별키\",\"봉투\","
                        + "\"봉투창\",\"흑백칼라\",\"단면양면\",\"배달\",\"템플릿코드\","
                        + "\"템플릿출력여부\",\"수취인수\",\"여백생성여부\",\"주소페이지유무\",\"맞춤자제유무\",\"메일머지유무\","
                        + "\"발송인명\",\"발송인우편번호\",\"발송인주소\",\"발송인상세주소\",\"발송인전화번호\""
                        + "]";
                returnJson[1] = "["
                        + "\"v1.10\","
                        + "\"N\","
                        + "\"PST\","
                        + "\"" + Checks.checkVal(dto.getUnitySndngMastrId(), "") + "\","
                        + "\"소봉투\","
                        + "\"이중창\","
                        + "\"칼라\","
                        + "\"양면\","
                        + "\"" + Checks.checkVal(fileReq.getPostDlvrSe(), "") + "\","
                        + "\"" + Checks.checkVal(fileReq.getPostTmplatCode(), "") + "\","
                        + "\"Y\","
                        + Checks.checkVal(String.valueOf(excelCnt), "") + ","
                        + "\"N\","
                        + "\"Y\","
                        + "\"N\","
                        + "\"Y\","
                        + "\"" + Checks.checkVal(dto.getSenderNm(), "") + "\","
                        + "\"" + Checks.checkVal(dto.getSenderZipNo(), "") + "\","
                        + "\"" + Checks.checkVal(dto.getSenderAddr(), "") + "\","
                        + "\"" + Checks.checkVal(dto.getSenderDetailAddr(), "") + "\","
                        + "\"" + Checks.checkVal(dto.getSenderTelno(), "") + "\""
                        + "]";
                break;
            case "88316": //추모시설운영처
                returnJson[0] = "["
                        + "\"버전\",\"테스트여부\",\"서비스\",\"연계식별키\",\"봉투\","
                        + "\"봉투창\",\"흑백칼라\",\"단면양면\",\"배달\",\"템플릿코드\","
                        + "\"템플릿출력여부\",\"수취인수\",\"여백생성여부\",\"주소페이지유무\",\"맞춤자제유무\",\"메일머지유무\","
                        + "\"발송인명\",\"발송인우편번호\",\"발송인주소\",\"발송인상세주소\",\"발송인전화번호\""
                        + "]";
                returnJson[1] = "["
                        + "\"v1.10\","
                        + "\"N\","
                        + "\"PST\","
                        + "\"" + Checks.checkVal(dto.getUnitySndngMastrId(), "") + "\","
                        + "\"소봉투\","
                        + "\"이중창\","
                        + "\"칼라\","
                        + "\"양면\","
                        + "\"" + Checks.checkVal(fileReq.getPostDlvrSe(), "") + "\","
                        + "\"" + Checks.checkVal(fileReq.getPostTmplatCode(), "") + "\","
                        + "\"Y\","
                        + Checks.checkVal(String.valueOf(excelCnt), "") + ","
                        + "\"N\","
                        + "\"Y\","
                        + "\"N\","
                        + "\"Y\","
                        + "\"서울특별시 시립승화원\","
                        + "\"10278\","
                        + "\"경기도 고양시 덕양구 통일로 504\","
                        + "\"\","
                        + "\"031-943-1930\""
                        + "]";
                break;
            default:
                break;
        }
        return returnJson;
    }

    private String[] PPDetailJson(String no, FmcInfExcel dto, String signguCode){
        String[] returnJson = new String[2];
        switch (signguCode) {
            case "88328": //교통시설운영처
                returnJson[0] = "["
                        + "\"순번\",\"이름\",\"우편번호\",\"주소\",\"상세주소\","
                        + "\"전화번호\",\"고지서구분명\",\"고지서명\",\"고지서상세명\",\"QR바코드\","
                        + "\"부과대상\",\"기관번호\",\"세목\",\"납세년월기\",\"과세번호\","
                        + "\"부과내역1\",\"부과내역2\",\"납부금액명1\",\"납부금액상세명1\",\"납부금액상세명2\","
                        + "\"납부금액상세명3\",\"납기구분1\",\"납기구분2\",\"납기내기한\",\"납기내금액1\","
                        + "\"납기내금액2\",\"납기내금액3\",\"납기내금액4\",\"납기내합계액\",\"출력일자\","
                        + "\"담당자\",\"우리가상계좌\",\"신한가상계좌\",\"하나가상계좌\""
                        + "]";
                returnJson[1] = "["
                        + "\""+ no +"\","
                        + "\""+ Checks.checkVal(dto.getRecevNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevZipNo(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevAddr(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevDetailAddr(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevTelno(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getGojiGubun(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getGojiNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getGojiDetailNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getQrBarcode(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getBuTarget(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum4(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getBuContent1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getBuContent2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountNm1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountDetailNm1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountDetailNm2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountDetailNm3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapGubun1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapGubun2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapPd(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount4(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountTotal(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getPrtDe(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getWorker(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getWVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getSVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getHVacct(),"") +"\""
                        + "]";
                break;
            case "88316": //추모시설운영처
                returnJson[0] = "["
                        + "\"순번\",\"이름\",\"우편번호\",\"주소\",\"상세주소\",\"전화번호\","
                        + "\"고지서구분명\",\"고지서명\",\"고지서상세명\",\"자료관리번호\",\"관리비기간\","
                        + "\"기관번호\",\"세목\",\"납세년월기\",\"과세번호\",\"부과내역3\","
                        + "\"부과내역4\",\"납기구분1\",\"납기구분2\",\"납기내기한\",\"납기내금액1\","
                        + "\"납기내금액2\",\"납기내금액3\",\"납기내합계액\",\"납기후기한\",\"납기후금액1\","
                        + "\"납기후금액2\",\"납기후금액3\",\"납기후합계액\",\"출력일자\",\"농협가상계좌\","
                        + "\"국민가상계좌\",\"우리가상계좌\",\"신한가상계좌\",\"하나가상계좌\",\"우체국가상계좌\","
                        + "\"가상계좌받는분\""
                        + "]";
                returnJson[1] = "["
                        + "\""+ no +"\","
                        + "\""+ Checks.checkVal(dto.getRecevNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevZipNo(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevAddr(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getRecevDetailAddr(),"") +"\","
                        + "\"\","
                        + "\""+ Checks.checkVal(dto.getGojiGubun(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getGojiNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getGojiDetailNm(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getDataSn(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getCostPd(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getTaxNum4(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getBuContent3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getBuContent4(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapGubun1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapGubun2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapPd(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmount3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAmountTotal(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAftPd(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAftAmount1(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAftAmount2(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAftAmount3(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNapAftAmountTotal(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getPrtDe(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getNVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getKVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getWVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getSVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getHVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getPVacct(),"") +"\","
                        + "\""+ Checks.checkVal(dto.getVacctNm(),"") +"\""
                        + "]";
                break;
            default:
                break;
        }
        return returnJson;
    }


    @SuppressWarnings("unchecked")
    private String jsonCn11(FmcInfExcel dto){
        JSONArray details = new JSONArray();

        details.add(getElementsObj(
            "제목 : " + String.format("%s(%s)", Checks.checkVal(dto.getGojiNm(),""), Checks.checkVal(dto.getGojiDetailNm(),"")),
            "SUBJECT_TEXT",
            new JSONArray()
        ));

        details.add(getElementsObj(
            "내용 : " + String.format("%s %s 안내입니다", Checks.checkVal(dto.getGojiNm(),""), Checks.checkVal(dto.getGojiGubun(),"")),
            "SUBJECT_TEXT",
            new JSONArray()
        ));

        // properties set //////////////////////////////////////////////////////
        // highlight key name
        String taxNum = Checks.checkVal(dto.getTaxNum1(),"")+Checks.checkVal(dto.getTaxNum2(),"")+Checks.checkVal(dto.getTaxNum3(),"")+Checks.checkVal(dto.getTaxNum4(), "");

        // highlight JSON object
        JSONObject htValue = new JSONObject();
        htValue.put(taxNum, getUseClipboard());

        JSONObject ht = new JSONObject();
        ht.put("highlight", htValue);

        // style < highlight
        JSONObject st = new JSONObject();
        st.put("style", ht);
        ///////////////////////////////////////////////////////////////////////////

        // elements set ////////////////////////////////////////////////////////
        JSONArray els = new JSONArray();
        els.add(getKeyValueObj("부과대상", Checks.checkVal(dto.getRecevDetailAddr(),"")));
        els.add(getKeyValueObj("납부자", Checks.checkVal(dto.getRecevNm(),"")));
        els.add(getKeyValueObj("- 사용료", Checks.checkVal(dto.getNapAmount1(),"")));
        els.add(getKeyValueObj("- " + Checks.checkVal(dto.getNapAmountDetailNm1(),""), Checks.checkVal(dto.getNapAmount2(),"")));
        els.add(getKeyValueObj("- " + Checks.checkVal(dto.getNapAmountDetailNm2(),""), Checks.checkVal(dto.getNapAmount3(),"")));
        //els.add(getKeyValueObj("- " + Checks.checkVal(dto.getNapAmountDetailNm3(),""), Checks.checkVal(dto.getNapAmount4(),"")));
        els.add(getKeyValueObj(Checks.checkVal(dto.getNapGubun1(),""), Checks.checkVal(formatDate(dto.getNapPd().substring(0,8), "-"),"")+"까지"));
        els.add(getKeyValueObj(Checks.checkVal(dto.getNapGubun2(),""), Checks.checkVal(dto.getNapAmountTotal(),"")+"원"));
        els.add(getKeyValueObj("납세번호", taxNum + "\\\\n※ ETAX 납부 시 29자리 입력"));
        /////////////////////////////////////////////////////////////////////////////

        details.add(getPropertiesObj(
            "내역",
            "KEY_VALUE",
            st,
            els
        ));

        // highlight JSON object
        htValue = new JSONObject();
        htValue.put(Checks.checkVal(dto.getWVacct(),""), getUseClipboard());
        htValue.put(Checks.checkVal(dto.getSVacct(),""), getUseClipboard());
        htValue.put(Checks.checkVal(dto.getHVacct(),""), getUseClipboard());

        // highlight
        ht = new JSONObject();
        ht.put("highlight", htValue);

        // style < highlight
        st = new JSONObject();
        st.put("style", ht);

        els = new JSONArray();
        els.add(getKeyValueObj("우리은행", Checks.checkVal(dto.getWVacct(),"")));
        els.add(getKeyValueObj("신한은행", Checks.checkVal(dto.getSVacct(),"")));
        els.add(getKeyValueObj("하나은행", Checks.checkVal(dto.getHVacct(),"")));

        details.add(getPropertiesObj(
            "서울시 세외수입 납부 전용계좌",
            "KEY_VALUE",
            st,
            els
        ));

        details.add(getElementsObj(
            "",
            "PRE_TEXT",
            " \\n\u203B 해당 고지서에 대한 연체료(공유재산법 의거 연7%~15%)는 최종 납부 이후 일할 정산하여 별도 고지서로 부과됩니다."
        ));

        els = new JSONArray();
        els.add(getKeyValueObj("가상계좌\\\\n납부", "고지서에 기재된 가상계좌로 이체,납부\\\\n고지서 금액과 일치해야 이체 가능"));
        els.add(getKeyValueObj("모바일\\\\n어플", "앱,PLAY스토어에서 (서울시 세금납부) 앱 설치\\\\n계좌이체(전 은행), 신용카드, 간편결제 납부"));
        els.add(getKeyValueObj("웹페이지", "인터넷 검색창에 (이택스) 검색\\\\n계좌이체(전 은행), 신용카드, 간편결제 납부"));
        els.add(getKeyValueObj("ARS", "납부 전용전화(1599-3900) 안내에 따라 납부\\\\n계좌이체 및 신용카드 납부 가능"));
        els.add(getKeyValueObj("은행방문", "전국은행(한국은행 제외), 우체국, 농협, 새마을금고, 신협, 수협, 산림조합\\\\n은행창구 또는 현금인출기에서 계좌이체"));

        details.add(getElementsObj(
            "세외수입 납부방법 안내",
            "KEY_VALUE",
            els
        ));

        ht = new JSONObject();
        //ht.put("head", new String[]{"구분","1개월\\\\n미만","3개월\\\\n미만","6개월\\\\n미만","6개월\\\\n이상"});
        ht.put("head", Arrays.asList("구분","1개월\\\\n미만","3개월\\\\n미만","6개월\\\\n미만","6개월\\\\n이상"));
        ht.put("rows", List.of("[\"연체료율\",\"7%\",\"8%\",\"9%\",\"10%\"]"));

        details.add(getElementsObj(
            "연체료",
            "TABLE",
            ht
        ));

        details.add(getElementsObj(
            "",
            "PRE_TEXT",
            " \\n\u203B 2022.04.19.까지 연체료율 10%~15%\\n\u203B 체납 시 보중보험 청구, 재산 압류, 사용료 청구소송 등 조치가 이루어지며, 금융기관 및 신용정보기관에 연체정보가 등록되어 금융거래 등 각종 경제활동에 불이익이 있을 수 있습니다."
        ));

        JSONObject json = new JSONObject();
        json.put("details", details);
        return json.toJSONString();
    }



    @SuppressWarnings("unchecked")
    private JSONObject getElementsObj(final String title, final String itemType, final Object elements){
        JSONObject dt = new JSONObject();
        dt.put("title", title);
        dt.put("item_type", itemType);
        dt.put("elements", elements);
        return dt;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getPropertiesObj(final String title, final String itemType, final Object properties, final Object elements){
        JSONObject dt = new JSONObject();
        dt.put("title", title);
        dt.put("item_type", itemType);
        dt.put("properties", properties);
        dt.put("elements", elements);
        return dt;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getKeyValueObj(final String key, final String value){
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("value", value);
        json.put("level", 1);
        return json;
    }

    private JSONObject getUseClipboard(){
        JSONObject json = new JSONObject();
        json.put("use-clipboard", true);
        return json;
    }

//    public static void main(String[] args) {
//        System.out.println(jsonCn11(null));
//    }
}
