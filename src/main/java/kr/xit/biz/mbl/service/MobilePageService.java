package kr.xit.biz.mbl.service;

import kr.xit.biz.ens.model.kakao.KkopayDocDTO;
import kr.xit.biz.ens.model.kt.KtCommonDTO;
import kr.xit.biz.ens.model.kt.KtTokenDTO;
import kr.xit.biz.mbl.mapper.IMobilePageMapper;
import kr.xit.biz.mbl.model.MobilePageDTO.MobilePageManage;
import kr.xit.core.consts.ErrorCode;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.support.utils.Checks;
import kr.xit.ens.kakao.service.IKkopayEltrcDocService;
import kr.xit.ens.kt.service.IKtMmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * description : 전자문서 중개자 모바일 페이지 API Service
 *
 * packageName : kr.xit.biz.mbl.service
 * fileName    : MobilePageService
 * author      : limju
 * date        : 2023-08-31
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-08-31    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MobilePageService extends AbstractService implements IMobilePageService {
    private final IMobilePageMapper mapper;
    private final IKkopayEltrcDocService kkoService;
    private final IKtMmsService ktMmsService;

    /**
     * <pre>
     * 카카오 내문서함 모바일 페이지 컨탠트 요청</h3>
     * - 대상 : 문서발송요청(bulks-배치처리)을 통해 document_binder_uuid를 발급 받은 데이타
     * - 모바일의 redirect url을 통해 들어온 요청 처리
     *   -> 서버에서 해당 문서에 매핑한 모바일 페이지 내용을 앱에 전달
     *   -> 카카오페이 > 내문서함 > 문서 클릭시 실행
     *
     * 처리 내용
     * 1. 토큰유효성 검증(redirect url 접속 허용/불허
     * 2. 문서상태 변경
     *
     * @param reqDTO KkopayDocDTO.OneTimeToken
     * </pre>
     */
    @Override
    @TraceLogging
    public IApiResponse findKkoMyDocReadyAndMblPage(KkopayDocDTO.OneTimeToken reqDTO) {

        if (Checks.isEmpty(reqDTO.getToken()) || Checks.isEmpty(reqDTO.getDocument_binder_uuid()) || Checks.isEmpty(reqDTO.getExternal_document_uuid()))
            throw BizRuntimeException.create(String.valueOf(ErrorCode.BAD_REQUEST.getHttpStatus().value()), "정상적인 요청이 아닙니다. 재인증 후 시도하시기 바랍니다.");
        // document_binder_uuid와 external_document_uuid로 데이타 검증
        final MobilePageManage mobilePageManage = mapper.selectKkoMobilePage(reqDTO);
        if(mobilePageManage == null)    throw BizRuntimeException.create("데이타 오류[내문서함 문서가 없습니다]");

        reqDTO.setSignguCode(mobilePageManage.getSignguCode());
        reqDTO.setFfnlgCode(mobilePageManage.getFfnlgCode());

        final ApiResponseDTO<KkopayDocDTO.ValidTokenResponse> res = kkoService.findMyDocReadyAndMblPage(reqDTO);
        if(!res.isSuccess()){
            return res;
        }
        return ApiResponseDTO.success(mobilePageManage.getMobilePageCn());
    }

    /**
     * <pre>
     * KT 모바일 페이지 요청 처리
     * - 본문자 수신 등록 요청(BC-AG-SN-002) API 호출시
     *   -> url(callback)에 등록하여 호출 되게 됨
     *   -> callback url ? token=... 형태로
     * 1. 토큰인증확인조회요청(BC-AG-SN-008) API call
     * 2. 토큰열람확인결과 전송(BC-AG-SN-009) API call
     *    -> srcKey 획득
     * 3. kt 모바일 데이타 조회
     *    -> return
     * @param reqDTO KtTokenConfirmRequest
     * @return
     * </pre>
     */
    @Override
    @TraceLogging
    public IApiResponse findKtMblPage(final KtTokenDTO.KtTokenConfirmRequest reqDTO) {
        //TODO::테스트
        //MobilePageManage mblDTO = mapper.selectKtMobilePage("DPDKT202311080000111");
        //return ApiResponseDTO.success(mblDTO.getMobilePageCn());

        //TODO::실운영시 코멘트 제거
        if (Checks.isEmpty(reqDTO.getAccessToken()))
            throw BizRuntimeException.create(String.valueOf(ErrorCode.BAD_REQUEST.getHttpStatus().value()), "정상적인 요청이 아닙니다. 재인증 후 시도하시기 바랍니다.");

        final String signguCode = reqDTO.getSignguCode();
        final String ffnlgCode = reqDTO.getFfnlgCode();
        final KtTokenDTO.KtTokenConfirmResponse cfmRes = ktMmsService.cfmToken(reqDTO);

        if(cfmRes.getResultCd().equals("00")){
            KtCommonDTO.KtCommonResponse readRes = ktMmsService.readToken(
                KtTokenDTO.KtTokenReadRequest.builder()
                    .signguCode(signguCode)
                    .ffnlgCode(ffnlgCode)
                    .accessToken(reqDTO.getAccessToken())
                    .build()
            );

            if(readRes.getResultCd().equals("00")){
                String srcKey = cfmRes.getSrcKey();
                final MobilePageManage mobilePageManage = mapper.selectKtMobilePage(srcKey);
                return ApiResponseDTO.success(mobilePageManage.getMobilePageCn());
            }
            throw BizRuntimeException.create(readRes.getResultCd(), readRes.toStringErrorMsg());
        }
        throw BizRuntimeException.create(cfmRes.getResultCd(), cfmRes.toStringErrorMsg());
    }
}
