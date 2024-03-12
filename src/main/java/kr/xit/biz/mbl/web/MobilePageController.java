package kr.xit.biz.mbl.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.SignguCode;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO;
import kr.xit.biz.ens.model.kt.KtTokenDTO;
import kr.xit.biz.mbl.service.IMobilePageService;
import kr.xit.core.model.IApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description : 전자문서 중개자 모바일 페이지 API Controller
 *               - mens-web에서 호출
 * packageName : kr.xit.biz.mbl.web
 * fileName    : MobilePageController
 * author      : limju
 * date        : 2023-08-31
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-08-31    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "MobilePageController", description = "전자문서 중개자 모바일 페이지 API Controller")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/biz/mbl/v1")
public class MobilePageController {
    private final IMobilePageService service;

    /**
     * <pre>
     *  카카오 모바일 페이지 요청
     *  - mens-web에서 호출 : {@code EnsMobileApiClientController.findKkoMyDocReadyAndMblPage}
     *    <a href="http://localhost:8080/api/web/mbl/v1/kko/mblPage.do">kakao mobile page</a>
     * @param reqDTO OneTimeToken
     * @return IApiResponse 모바일 페이지 데이타 String return
     * </pre>
     */
    @Operation(summary = "카카오 모바일 데이타 요청(모바일 페이지에서 호출)", description = "카카오 모바일 데이타 요청(모바일 페이지에서 호출)")
    @PostMapping(value = "/kko/mblPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findKkoMyDocReadyAndMblData(@RequestBody final KkopayDocDTO.OneTimeToken reqDTO) {
        return service.findKkoMyDocReadyAndMblPage(reqDTO);
    }

    /**
     * <pre>
     * KT 모바일 페이지 요청
     * - 본문자 수신 등록 요청(BC-AG-SN-002) API 호출시
     *   -> url(callback)에 등록하여 호출 되게 됨
     *   -> callback url ? token=... 형태로
     * - 토큰인증확인조회요청(BC-AG-SN-008) API call
     * - 토큰열람확인결과 전송(BC-AG-SN-009) API call
     *
     * - mens-web에서 호출 : {@code EnsMobileApiClientController.findKtMblPage}
     *   <a href="http://localhost:8080/api/web/mbl/v1/kt/dpMblPage.do?token=">KT DP mobile page</a>
     * - mens-web에서 호출 : {@code EnsMobileApiClientController.findKtMblPage}
     *   <a href="http://localhost:8080/api/web/mbl/v1/kt/meMblPage.do?token=">KT ME mobile page</a>
     *
     * @param token OTT token
     * @param request HttpServletRequest
     * @return IApiResponse 모바일 페이지 데이타 String return
     * </pre>
     */
    @Operation(summary = "KT 모바일 데이타 요청(모바일 페이지에서 호출)", description = "KT 모바일 데이타 요청(모바일 페이지에서 호출)")
    @PostMapping(
        value = {"/kt/dpMblPage", "/kt/meMblPage"},
        produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findKtMblPage(@RequestParam final String token, final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final KtTokenDTO.KtTokenConfirmRequest cfmReqDTO = KtTokenDTO.KtTokenConfirmRequest.builder()
            .signguCode(SignguCode.TRAFFIC.getCode())
            .ffnlgCode(ApiConstants.FFNLN_CODE)
            .accessToken(token)
            .build();

        if(uri.contains("meMblPage")){
            cfmReqDTO.setSignguCode(SignguCode.FUNERAL.getCode());
        }
        return service.findKtMblPage(cfmReqDTO);
    }
}
