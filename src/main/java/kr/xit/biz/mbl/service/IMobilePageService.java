package kr.xit.biz.mbl.service;

import kr.xit.biz.ens.model.kakao.KkopayDocDTO.OneTimeToken;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmRequest;
import kr.xit.core.model.IApiResponse;

/**
 * <pre>
 * description : 전자문서 중개자 모바일 페이지 API Service Interface
 *
 * packageName : kr.xit.biz.mbl.service
 * fileName    : IMobilePageService
 * author      : limju
 * date        : 2023-08-31
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-08-31    limju       최초 생성
 *
 * </pre>
 */
public interface IMobilePageService {

    IApiResponse findKkoMyDocReadyAndMblPage(OneTimeToken reqDTO);

    IApiResponse findKtMblPage(final KtTokenConfirmRequest reqDTO);
}
