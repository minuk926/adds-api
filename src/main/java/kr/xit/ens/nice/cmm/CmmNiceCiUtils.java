package kr.xit.ens.nice.cmm;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.NiceCiWrkDiv;
import kr.xit.biz.ens.model.nice.NiceCiDTO.CommonResponseDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.biz.ens.model.nice.NiceCiDTO.ResponseDataHeader;
import kr.xit.biz.ens.model.nice.NiceCiDTO.SymkeyRegInfo;
import kr.xit.biz.nice.service.IBizNiceCiService;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.spring.util.ApiSpringUtils;
import kr.xit.core.spring.util.CoreSpringUtils;
import kr.xit.core.spring.util.MessageUtil;
import kr.xit.core.support.utils.DateUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import kr.xit.ens.nice.service.INiceCiService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.nice.cmm
 * fileName    : CmmNiceCiUtils
 * author      : limju
 * date        : 2023-09-19
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-19    limju       최초 생성
 *
 * </pre>
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmmNiceCiUtils {
    private static final MessageUtil messageUtil = CoreSpringUtils.getMessageUtil();
    private static final ICmmEnsCacheService cacheService = ApiSpringUtils.getCmmEnsCacheService();
    private static final INiceCiService niceCiService = ApiSpringUtils.getNiceCiService();
    private static final IBizNiceCiService bizNiceService = ApiSpringUtils.getBizNiceCiService();

    /**
     * Nice 인증 정보 조회
     * @param reqDTO NiceCiRequest
     * @return NiceCiInfo
     */
    public static NiceCiInfo getNiceCiInfo(final NiceCiRequest reqDTO) {
        final NiceCiInfo dto = cacheService.getNiceCiInfoCache(reqDTO.getSignguCode(), reqDTO.getFfnlgCode());
        if(ObjectUtils.isEmpty(dto))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.info"));
        cacheService.logCache();

        return dto;
    }

    /**
     * <pre>
     * 공개키 정보 조회
     * 0. cache call
     *  --> 공개키 잔여일 기준 보다 작으면
     * 1. 공개키 재발급
     * 2. cache reload
     * @param reqDTO NiceCiRequest
     * @return NiceCiRequest
     * </pre>
     */
    public static NiceCiInfo getPublickeyNiceCiInfo(final NiceCiRequest reqDTO) {
        final NiceCiInfo niceDTO = getNiceCiInfo(reqDTO);
        if(ObjectUtils.isEmpty(niceDTO.getAccessToken()))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.token.info"));

        if(StringUtils.isNotEmpty(niceDTO.getValidDtim())
            && DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT).compareTo(niceDTO.getValidDtim()) < 0)     return niceDTO;

//        // 1. 토큰 폐기
//        TokenRevokeResponse revokeResDTO = bizNiceService.revokeToken(reqDTO);
//
//        if(!(revokeResDTO.getDataHeader().getGwRsltCd().equals("1200") && revokeResDTO.getDataBody().isResult())){
//            throw BizRuntimeException.create(JsonUtils.toJson(revokeResDTO.getDataHeader()));
//        }

        // 2. 공개키 발급 처리
        //TODO::에러 처리 - throw exception
        bizNiceService.requestPublickey(reqDTO);

        return cacheService.getNiceCiInfoCache(reqDTO.getSignguCode(), reqDTO.getFfnlgCode());
    }

    /**
     * <pre>
     * 대칭키 정보 조회
     * 0. cache call
     *  --> 현재 대칭키 잔여일 기준 보다 작으면
     * 1. 대칭키 등록
     * 4. cache reload
     * @param reqDTO NiceCiRequest
     * @return NiceCiInfo
     * </pre>
     */
    public static NiceCiInfo getSymkeyNiceCiInfo(final NiceCiRequest reqDTO) {
        final NiceCiInfo niceDTO = getPublickeyNiceCiInfo(reqDTO);
        if(StringUtils.isNotEmpty(niceDTO.getCurSymkeyValidDtim())
            && DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT).compareTo(niceDTO.getCurSymkeyValidDtim()) < 0)   return niceDTO;

        // 대칭키 발행 등록
        //TODO::에러 처리 - throw exception
        bizNiceService.requestRegSymmetrickey(reqDTO);

        return cacheService.getNiceCiInfoCache(reqDTO.getSignguCode(), reqDTO.getFfnlgCode());
    }

    /**
     * <pre>
     * 대칭키(symmetrickey) 등록 요청시 symkey_reg_info JSON 데이타 생성
     * -> siteCode는 DB 정보 set
     * @return SymkeyRegInfo
     * </pre>
     */
    public static SymkeyRegInfo getSymkeyRegInfo() {
        return SymkeyRegInfo.builder()
            //.siteCode()
            .requestNo(CmmEnsUtils.generateLengthUuid(30))
            .key(CmmEnsUtils.generateLengthUuid(32))
            .iv(CmmEnsUtils.generateLengthUuid(16))
            .hmacKey(CmmEnsUtils.generateLengthUuid(32))
            .build();
    }

    /**
     * Nice CI API 호출 헤더 맵 set
     * 공개키, 대칭키, ipin 요청시의 헤더
     * @param contentType String
     * @param type String
     * @param accessToken String
     * @param clientId String
     * @return Map<String,String>
     */
    public static Map<String,String> getAuthHeaderMap(final String contentType, final String type, final String accessToken, final String clientId){
        final Map<String,String> map = new HashMap<>();
        if(ObjectUtils.isNotEmpty(contentType))  map.put(HttpHeaders.CONTENT_TYPE, contentType);
        map.put(
            HttpHeaders.AUTHORIZATION,
            String.format("%s %s",
                type,
                Base64Utils.encodeToString(
                    String.format("%s:%s:%s",
                        accessToken,
                        (new Date().getTime() / 1000), clientId).getBytes(StandardCharsets.UTF_8))
            )
        );
        return map;
    }

    /**
     * Nice CI 인증 에러 메세지 return
     * @param wrkDiv API구분 - 공개키|대칭키|ipin
     * @param resultCd String 에러코드
     * @return String 에러메세지
     */
    public static String getFromResultCd(NiceCiWrkDiv wrkDiv, String resultCd){
        final String preFix = "err.api.nice";

        return switch(wrkDiv) {
            case PUBLIC_KEY -> messageUtil.getMessage(preFix + ".pubKey." + resultCd);
            case SYM_KEY -> messageUtil.getMessage(preFix + ".symKey." + resultCd);
            case CI -> messageUtil.getMessage(preFix + ".ci." + resultCd);
            default -> StringUtils.EMPTY;
        };
    }

    /**
     * <pre>
     * Nice CI 에러 처리 : 공개키요청|대칭키등록|아이핀CI요청
     * RW_RSLT_CD - 1200 && rsp_cd - P000 && result_cd - 0000인 경우만 정상 처리
     * RW_RSLT_CD - 1200 && rsp_cd - P000 && result_cd != 0000 인 경우 해당 업무 에러 메세지 return
     * -> message-api.properties err.api.nice.[pubKey|symKey|ci].[에러코드] 메세지
     * @param tranId String
     * @param resHeader ResponseDataHeader
     * @param resBody CommonResponseDataBody
     * </pre>
     */
    public static void checkApiResponse(final String tranId, final ResponseDataHeader resHeader,
        final CommonResponseDataBody resBody, final NiceCiWrkDiv wrkDiv) {

        // 실패
        if(!resHeader.getGwRsltCd().equals("1200")) {
            throw BizRuntimeException.create(resHeader.getGwRsltCd(), resHeader.getGwRsltMsg());
        }

        // 성공(1200)시 tranId 체크
        if(!tranId.equals(resHeader.getTranId()))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.invalid.tranId"));

        // 실패
        if(ObjectUtils.isNotEmpty(resBody) && !resBody.getRspCd().equals("P000")) {
            throw BizRuntimeException.create(resBody.getRspCd(), resBody.getResMsg());
        }

        // 실패
        if(ObjectUtils.isNotEmpty(resBody) && !resBody.getResultCd().equals("0000")) {
            throw BizRuntimeException.create(resBody.getRspCd(), CmmNiceCiUtils.getFromResultCd(wrkDiv, resBody.getResultCd()));
        }
    }
}
