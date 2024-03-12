package kr.xit.ens.pplus.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.xit.biz.common.ApiConstants.SndngSeCode;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.cmm.SndngMssageParam;
import kr.xit.biz.ens.model.pplus.PplusDTO.BatchAcceptRequest;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpCommonResponse;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusRequest;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusResponse;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.spring.util.ApiWebClientUtil;
import kr.xit.core.support.utils.FileUtil;
import kr.xit.core.support.utils.JsonUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import kr.xit.ens.pplus.mapper.IPplusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.pplus.service
 * fileName    : PplusService
 * author      : limju
 * date        : 2023-10-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-04    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PplusService extends AbstractService implements IPplusService {
    @Value("${app.file.cmm.temp-path}")
    private String FILE_TEMP_PATH;

    @Value("${app.contract.pplus.host}")
    private String HOST;

    @Value("${app.contract.pplus.api.accept}")
    private String ACCEPT;

    @Value("${app.contract.pplus.api.status}")
    private String STATUS;

    private final StringEncryptor jasyptStringEncryptor;
    private final ApiWebClientUtil webClient;
    private final IPplusMapper mapper;

    @SuppressWarnings("unchecked")
    @Override
    @TraceLogging
    public PpCommonResponse sendBulks(final SndngMssageParam reqDTO) {
        final List<BatchAcceptRequest> tgtList = mapper.selectPostPlusSendTgts(reqDTO);

        if(!tgtList.isEmpty()){
            JSONObject master = new JSONObject();
            master.put("cols", JsonUtils.toObject(tgtList.get(0).getMasterCols(), ArrayList.class));
            master.put("rows", JsonUtils.toObject(tgtList.get(0).getMasterRows(), ArrayList.class));

            JSONObject detail = new JSONObject();
            detail.put("cols", JsonUtils.toObject(tgtList.get(0).getDetailCols(), ArrayList.class));
            detail.put("rows", tgtList.stream()
                                      .map(d -> jasyptStringEncryptor.decrypt(d.getDetailRows()))
                                      .map(d -> JsonUtils.toObject(d, ArrayList.class))
                                      .toList());

            JSONObject pstJson = new JSONObject();
            pstJson.put("master", master);
            pstJson.put("detail", detail);

            final String unitySndngMstId = tgtList.get(0).getUnitySndngMastrId();
            FileUtil.saveFile(FILE_TEMP_PATH,unitySndngMstId+".json", pstJson.toJSONString().getBytes(
                StandardCharsets.UTF_8));
            return accept(reqDTO, unitySndngMstId);
        }
        return null;
    }

    public PpCommonResponse accept(final SndngMssageParam paramDTO, final String unitySndngMstId) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = CmmEnsUtils.getRlaybsnmInfo(paramDTO.getSignguCode(), paramDTO.getFfnlgCode(), SndngSeCode.PPLUS);

        MultipartFile pstFile = FileUtil.createMutipartFile(unitySndngMstId + ".json", FILE_TEMP_PATH);

        List<MultipartFile> pstFiles = new ArrayList<>();
        pstFiles.add(pstFile);

        PpCommonResponse resDTO = webClient.exchangeFileData(
            HOST + ACCEPT + String.format("?apiKey=%s", ktMnsInfo.getPplusApiKey()),
            HttpMethod.POST,
            pstFiles,
            "pstFile",
            PpCommonResponse.class
        );
        resDTO.setUnitySndngMastrId(unitySndngMstId);

        File file = new File(FILE_TEMP_PATH + "/" +unitySndngMstId + ".json");
        if(file.exists()) {
            file.delete();
        }

        return resDTO;
    }

    @Override
    public PpStatusResponse statusBulks(PpStatusRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = CmmEnsUtils.getRlaybsnmInfo(reqDTO.getSignguCode(), reqDTO.getFfnlgCode(), SndngSeCode.PPLUS);
        reqDTO.setApiKey(ktMnsInfo.getPplusApiKey());
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        reqDTO.setTry1(null);
        CmmEnsUtils.validate(reqDTO);

        final Map<String,String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);

        return webClient.exchangeFormData(
            HOST + STATUS,
            HttpMethod.POST,
            reqDTO,
            PpStatusResponse.class,
            headerMap
        );
    }
}
