package kr.xit.ens.kt.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaResponse;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtApproveRcvRequest;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtRefuseRcvRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtBlacklistRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcDtlRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtWhitelistRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtBefSendRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMainSendRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmResponse;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenExcaRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenReadRequest;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.ens.kt.service.IKtMmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.kt.web
 * fileName    : KtMmsController
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "KtMmsController", description = "KT MMS API")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ens/kt/v1")
public class KtMmsController {

    private final IKtMmsService service;

    //------------------------------------------------------------------------------
    // mens 사용 API
    //------------------------------------------------------------------------------
    @Operation(deprecated = true, summary = "기관용 토큰 발급 요청 -> 업무(Biz)단 API 에서 처리", description = "KT 문서 중개자 정보 DB update가 필요하여 업무단의 API를 사용하여야 함")
    @PostMapping(value = "/requestToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestToken(@RequestBody final KtMnsRequest paramDTO) {
        return ApiResponseDTO.success(service.requestToken(paramDTO));
    }

    @Operation(summary = "본문자 수신 등록 요청(BC-AG-SN-002)", description = "본문자 수신 등록 요청(BC-AG-SN-002)")
    @PostMapping(value = "/mainSend", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse mainSend(@RequestBody final KtMainSendRequest reqDTO) {
        KtCommonResponse dto = service.mainSend(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    /**
     * <pre>
     * - 본문자 수신 등록 요청(BC-AG-SN-002) API 호출시 set한 url
     *   -> OTT token get
     * - 토큰인증확인조회요청(BC-AG-SN-008) API call
     * - 토큰열람확인결과 전송(BC-AG-SN-009) API call
     * @param reqDTO KtTokenConfirmRequest
     * @return KtTokenConfirmResponse
     * </pre>
     */
    @Operation(deprecated = true, summary = "토큰인증확인 조회(BC-AG-SN-008) -> KT 모바일 데이타 요청(모바일 페이지에서 호출)에서 처리", description = "토큰인증확인조회요청(BC-AG-SN-008) <br><a href='http://localhost:8081/swagger-ui/index.html?urls.primaryName=6.%20%EC%A0%84%EC%9E%90%EA%B3%A0%EC%A7%80%20%EC%97%85%EB%AC%B4%20API#/MobilePageController'>전자문서중개자모바일페이지</a>")
    @PostMapping(value = "/cfmToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse cfmToken(@RequestBody final KtTokenConfirmRequest reqDTO) {
        KtTokenConfirmResponse dto = service.cfmToken(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    /**
     * <pre>
     * - 본문자 수신 등록 요청(BC-AG-SN-002) API 호출시 set한 url
     *   -> OTT token get
     * - 토큰인증확인조회요청(BC-AG-SN-008) API call
     * - 토큰열람확인결과 전송(BC-AG-SN-009) API call
     * @param reqDTO KtTokenReadRequest
     * @return KtCommonResponse
     * </pre>
     */
    @Operation(deprecated = true, summary = "토큰열람확인결과 전송(BC-AG-SN-009) -> KT 모바일 데이타 요청(모바일 페이지에서 호출)에서 처리", description = "토큰열람확인결과 전송(BC-AG-SN-009) <br><a href='http://localhost:8081/swagger-ui/index.html?urls.primaryName=6.%20%EC%A0%84%EC%9E%90%EA%B3%A0%EC%A7%80%20%EC%97%85%EB%AC%B4%20API#/MobilePageController'>전자문서중개자모바일페이지</a>")
    @PostMapping(value = "/readToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse readToken(@RequestBody final KtTokenReadRequest reqDTO) {
        KtCommonResponse dto = service.readToken(reqDTO);
        return ApiResponseDTO.success(dto);
    }
    //------------------------------------------------------------------------------








    //------------------------------------------------------------------------------
    // mens 미사용 API
    //------------------------------------------------------------------------------

    @Operation(hidden = true, summary = "사전 문자 수신 등록 요청", description = "사전 문자 수신 등록 요청(BC-AG-SN-001)")
    @PostMapping(value = "/beforeSend", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse beforeSend(@RequestBody final KtBefSendRequest reqDTO) {
        KtCommonResponse dto = service.beforeSend(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "수신 거부 등록 요청", description = "수신 거부 등록 요청(BC-AG-SN-007)")
    @PostMapping(value = "/blacklist", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse blacklist(@RequestBody final KtBlacklistRequest reqDTO) {
        KtCommonResponse dto = service.blacklist(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "백오피스 발송 통계 연계 조회 요청", description = "백오피스 발송 통계 연계 조회 요청(BC-AG-SN-011)")
    @PostMapping(value = "/sendSttc", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse sendSttc(@RequestBody final KtSendSttcRequest reqDTO) {
        KtMmsDTO.KtSendSttcResponse dto = service.sendSttc(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "백오피스 발송 결과 연계 조회 요청", description = "백오피스 발송 결과 연계 조회 요청(BC-AG-SN-012)")
    @PostMapping(value = "/sendSttcDtl", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse sendSttcDtl(@RequestBody final KtSendSttcDtlRequest reqDTO) {
        KtMmsDTO.KtSendSttcDtlResponse dto = service.sendSttcDtl(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "whitelist 등록 요청", description = "whitelist 등록 요청(BC-AG-SN-013)")
    @PostMapping(value = "/whitelist", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse whitelist(@RequestBody final KtWhitelistRequest reqDTO) {
        KtCommonResponse dto = service.whitelist(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "수신거부상태 전송", description = "수신거부상태전송(BC-AG-SN-014)")
    @PostMapping(value = "/refuseRcv", produces = MediaType.APPLICATION_JSON_VALUE)
    public KtCommonResponse refuseRcv(@RequestBody final KtRefuseRcvRequest reqDTO) {
        KtCommonResponse dto = service.refuseRcv(reqDTO);
        return dto;
    }

    @Operation(hidden = true, summary = "수신동의상태 전송", description = "수신동의상태전송(BC-AG-SN-015)")
    @PostMapping(value = "/approveRcv", produces = MediaType.APPLICATION_JSON_VALUE)
    public KtCommonResponse approveRcv(@RequestBody final KtApproveRcvRequest reqDTO) {
        KtCommonResponse dto = service.approveRcv(reqDTO);
        return dto;
    }

    @Operation(hidden = true, summary = "유통증명서발급처리 요청", description = "유통증명서발급처리 요청(BC-AG-SM-001)")
    @PostMapping(value = "/cerfAcmd", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse cerfAcmd(@RequestBody final KtAcmdCerfRequest reqDTO) {
        KtAcmdCerfResponse dto = service.cerfAcmd(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "가상정산화면 연계토큰 인증 요청", description = "가상정산화면 연계토큰 인증 요청(BC-AG-SM-002)")
    @PostMapping(value = "/excaAcmd", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse excaAcmd(@RequestBody final KtTokenExcaRequest reqDTO) {
        KtCommonResponse dto = service.excaAcmd(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "정산연계자료조회", description = "정산연계자료조회(BC-AG-EC-001)")
    @PostMapping(value = "/exca", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse exca(@RequestBody final KtExcaRequest reqDTO) {
        KtExcaResponse dto = service.exca(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "전자문서 유통정보 수치 조회", description = "전자문서 유통정보 수치 조회(BC-AG-HS-001)")
    @PostMapping(value = "/infoAcmd", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse infoAcmd(@RequestBody final KtAcmdInfoRequest reqDTO) {
        KtAcmdInfoResponse dto = service.infoAcmd(reqDTO);
        return ApiResponseDTO.success(dto);
    }

    @Operation(hidden = true, summary = "전자문서 유통정보 수치확인서 발급 처리", description = "전자문서 유통정보 수치확인서 발급 처리(BC-AG-HS-002)")
    @PostMapping(value = "/cfmAcmd", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse cfmAcmd(@RequestBody final KtAcmdInfoCfmRequest reqDTO) {
        KtAcmdInfoCfmResponse dto = service.cfmAcmd(reqDTO);
        return ApiResponseDTO.success(dto);
    }
    //------------------------------------------------------------------------------
}
