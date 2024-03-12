package kr.xit.ens.kt.service;

import java.util.HashMap;
import java.util.Map;
import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.SndngSeCode;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaResponse;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtApproveRcvRequest;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtRefuseRcvRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtBlacklistRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcDtlRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcDtlResponse;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcResponse;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtWhitelistRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtBefSendRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMainSendRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmResponse;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenExcaRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenReadRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenResponse;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.spring.util.ApiWebClientUtil;
import kr.xit.core.support.utils.DateUtils;
import kr.xit.core.support.utils.JsonUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.kt.service
 * fileName    : KtMmsService
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
@RequiredArgsConstructor
@Service
public class KtMmsService extends AbstractService implements IKtMmsService {
    @Value("${app.contract.kt.host}")
    private String HOST;
    //-----------------------------------------------------------------
    // mens 사용 API
    //-----------------------------------------------------------------
    /**
     * 토큰발행
     */
    @Value("${app.contract.kt.api.generate-token}")
    private String API_GENERATE_TOKEN;
    /**
     * 본문자수신등록 : BC-AG-SN-002
     */
    @Value("${app.contract.kt.api.main-send}")
    private String API_MAIN_SEND;
    /**
     * 토큰인증확인조회 : BC-AG-SN-008
     */
    @Value("${app.contract.kt.api.cfm-token}")
    private String API_CFM_TOKEN;
    /**
     * 토큰열람확인결과전송 : BC-AG-SN-009
     */
    @Value("${app.contract.kt.api.read-token}")
    private String API_READ_TOKEN;
    //-----------------------------------------------------------



    //-----------------------------------------------------------------
    // mens 미사용 API
    //-----------------------------------------------------------------
    /**
     * 사전문자수신등록 : BC-AG-SN-001
     */
    @Value("${app.contract.kt.api.before-send}")
    private String API_BEFORE_SEND;
    /**
     * 수신거부등록 : BC-AG-SN-007
     */
    @Value("${app.contract.kt.api.blacklist}")
    private String API_BLACKLIST;
    /**
     * 백오피스발송통계연계조회 : BC-AG-SN-011
     */
    @Value("${app.contract.kt.api.send-sttc}")
    private String API_SEND_STTC;
    /**
     * 백오피스발송결과연계조회 : BC-AG-SN-012
     */
    @Value("${app.contract.kt.api.send-sttcdtl}")
    private String API_SEND_STTCDTL;
    /**
     * whitelist등록 : BC-AG-SN-013
     */
    @Value("${app.contract.kt.api.whitelist}")
    private String API_WHITELIST;
    /**
     * 유통증명서발급처리 : BC-AG-SM-001
     */
    @Value("${app.contract.kt.api.cerf-acmd}")
    private String API_CERF_ACMD;
    /**
     * 기관정산화면연계토큰인증 : BC-AG-SM-002
     */
    @Value("${app.contract.kt.api.exca-token}")
    private String API_EXCA_ACMD;
    /**
     * 정산연계자료조회 : BC-AG-EC-001
     */
    @Value("${app.contract.kt.api.exca}")
    private String API_EXCA;
    /**
     * 전자문서유통정보수치조회 : BC-AG-HS-001
     */
    @Value("${app.contract.kt.api.info-acmd}")
    private String API_INFO_ACMD;
    /**
     * 전자문서유통정보수치확인서 발급처리 : BC-AG-HS-002
     */
    @Value("${app.contract.kt.api.cfm-acmd}")
    private String API_CFM_ACMD;
    /**
     * 수신거부상태전송 : BC-AG-SN-014
     */
    @Value("${app.contract.kt.api.refuse-rcv}")
    private String API_REFUSE_RCV;
    /**
     * 수신동의상태전송 : BC-AG-SN-015
     */
    @Value("${app.contract.kt.api.approve-rcv}")
    private String API_APPROVE_RCV;

    private final ApiWebClientUtil webClient;
    private final ICmmEnsCacheService cacheService;
    //-----------------------------------------------------------


    //------------------------------------------------------------------------------
    // mens 사용 API
    //------------------------------------------------------------------------------
    @Override
    @TraceLogging
    public KtTokenResponse requestToken(final KtMnsRequest paramDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = cacheService.getRlaybsnmInfoCache(CmmEnsRequestDTO.builder()
            .signguCode(paramDTO.getSignguCode())
            .ffnlgCode(paramDTO.getFfnlgCode())
            .profile(ApiConstants.PROFILE)
            .build());

        final KtTokenRequest reqDTO = KtTokenRequest.builder()
            .clientId(ktMnsInfo.getKtSvcClientId())
            .clientSecret(ktMnsInfo.getKtSvcClientSecret())
            .scope(ktMnsInfo.getKtScope())
            .build();
        CmmEnsUtils.validate(reqDTO);

        final Map<String,String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headerMap.put("client-id", ktMnsInfo.getKtClientId());
        headerMap.put("client-tp", ktMnsInfo.getKtClientTp());

        return webClient.exchangeFormData(
            HOST + API_GENERATE_TOKEN,
            HttpMethod.POST,
            reqDTO,
            KtTokenResponse.class,
            headerMap
        );
    }

    @Override
    @TraceLogging
    public KtCommonResponse mainSend(final KtMainSendRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setServiceCd(ktMnsInfo.getKtServiceCode());
        reqDTO.setServiceKey(ktMnsInfo.getKtSvcCerfKey());
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_MAIN_SEND,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtCommonResponse.class,
                ktMnsInfo
        );
    }

    @Override
    @TraceLogging
    public KtTokenConfirmResponse cfmToken(final KtTokenDTO.KtTokenConfirmRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setServiceCd(ktMnsInfo.getKtServiceCode());
        reqDTO.setServiceKey(ktMnsInfo.getKtSvcCerfKey());
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_CFM_TOKEN,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtTokenConfirmResponse.class,
                ktMnsInfo
        );
    }

    @Override
    @TraceLogging
    public KtCommonResponse readToken(final KtTokenReadRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setServiceCd(ktMnsInfo.getKtServiceCode());
        reqDTO.setServiceKey(ktMnsInfo.getKtSvcCerfKey());
        reqDTO.setMmsRdgTmst(DateUtils.getTodayAndNowTime("yyyyMMddHHmmss"));
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_READ_TOKEN,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtCommonResponse.class,
                ktMnsInfo
        );
    }
    //------------------------------------------------------------------------------




    //------------------------------------------------------------------------------
    // mens 미사용 API
    //------------------------------------------------------------------------------
    /**
     * 사전 문자 수신 등록 요청(BC-AG-SN-001)
     * @param reqDTO
     */
    @Override
    public KtCommonResponse beforeSend(final KtBefSendRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);

        reqDTO.setServiceCd(ktMnsInfo.getKtServiceCode());
        reqDTO.setServiceKey(ktMnsInfo.getKtSvcCerfKey());
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
            HOST + API_BEFORE_SEND,
            HttpMethod.POST,
            JsonUtils.toJson(reqDTO),
            KtCommonResponse.class,
            ktMnsInfo
        );
    }

    @Override
    public KtCommonResponse blacklist(final KtBlacklistRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
            HOST + API_BLACKLIST,
            HttpMethod.POST,
            JsonUtils.toJson(reqDTO),
            KtCommonResponse.class,
            ktMnsInfo
        );
    }

    @Override
    public KtSendSttcResponse sendSttc(final KtSendSttcRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_SEND_STTC,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtSendSttcResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtSendSttcDtlResponse sendSttcDtl(final KtSendSttcDtlRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_SEND_STTCDTL,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtSendSttcDtlResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtCommonResponse whitelist(final KtWhitelistRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_WHITELIST,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtCommonResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtCommonResponse refuseRcv(final KtRefuseRcvRequest reqDTO) {
        //final CmmEnsRlaybsnmDTO ktMnsInfo = CmmKtMmsUtils.getRlaybsnmInfo(reqDTO.getSignguCode(), reqDTO.getFfnlgCode());
        //reqDTO.setServiceCd(ktMnsInfo.get);
        //reqDTO.setServiceKey(ktMnsInfo.get);
        CmmEnsUtils.validate(reqDTO);

        return KtCommonResponse.builder()
                .resultCd("")
                .resultDt("")
                .build();
    }

    @Override
    public KtCommonResponse approveRcv(final KtApproveRcvRequest reqDTO) {
        //final CmmEnsRlaybsnmDTO ktMnsInfo = CmmKtMmsUtils.getRlaybsnmInfo(reqDTO.getSignguCode(), reqDTO.getFfnlgCode());
        //reqDTO.setServiceCd(ktMnsInfo.get);
        //reqDTO.setServiceKey(ktMnsInfo.get);
        CmmEnsUtils.validate(reqDTO);

        return KtCommonResponse.builder()
                .resultCd("")
                .resultDt("")
                .build();
    }

    @Override
    public KtAcmdCerfResponse cerfAcmd(final KtAcmdCerfRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_CERF_ACMD,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtAcmdCerfResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtCommonResponse excaAcmd(KtTokenExcaRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_EXCA_ACMD,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtCommonResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtExcaResponse exca(final KtExcaRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_EXCA,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtExcaResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtAcmdInfoResponse infoAcmd(final KtAcmdInfoRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_INFO_ACMD,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtAcmdInfoResponse.class,
                ktMnsInfo
        );
    }

    @Override
    public KtAcmdInfoCfmResponse cfmAcmd(final KtAcmdInfoCfmRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = getRlaybsnmInfo(reqDTO);
        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchangeKt(
                HOST + API_CFM_ACMD,
                HttpMethod.POST,
                JsonUtils.toJson(reqDTO),
                KtAcmdInfoCfmResponse.class,
                ktMnsInfo
        );
    }
    //------------------------------------------------------------------------------

    private CmmEnsRlaybsnmDTO getRlaybsnmInfo(final KtCommonDTO.KtMnsRequest request){
        return CmmEnsUtils.getRlaybsnmInfo(request.getSignguCode(), request.getFfnlgCode(), SndngSeCode.KT_BC);
    }
}
