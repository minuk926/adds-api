package kr.xit.ens.kakao.service;

import kr.xit.biz.ens.model.kakao.KkopayDocAttrDTO.DocumentBinderUuid;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendResponses;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkStatusRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkStatusResponses;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.DocStatusResponse;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.OneTimeToken;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.SendRequest;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.SendResponse;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.ValidTokenRequest;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.ValidTokenResponse;
import kr.xit.core.model.ApiResponseDTO;

/**
 * <pre>
 * description : 카카오 페이 전자 문서 발송 요청 인터 페이스
 * packageName : kr.xit.ens.kakao.service
 * fileName    : IKkopayEltrcDocService
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 */
public interface IKkopayEltrcDocService {

    /**
     * <pre>
     * 모바일웹 연계 문서발송 요청
     * -.이용기관 서버에서 전자문서 서버로 문서발송 처리를 요청합니다.
     * </pre>
     * @param reqDTO KkopayDocDTO.SendRequest
     * @return KkopayDocDTO.SendResponse
     */
    SendResponse requestSend(final SendRequest reqDTO);

    /**
     * <pre>
     * 토큰 유효성 검증(Redirect URL  접속 허용/불허)
     * </pre>
     * @param reqDTO KkopayDocDTO.ValidTokenRequest
     * @return ApiResponseDTO<KkopayDocDTO.ValidTokenResponse>
     */
    ValidTokenResponse validToken(final ValidTokenRequest reqDTO);

    /**
     * <pre>
     * 문서 상태 변경 API
     * -.문서에 대해서 열람 상태로 변경. 사용자가 문서열람 시(OTT 검증 완료 후 페이지 로딩 완료 시점) 반드시 문서 열람 상태 변경 API를 호출해야 함.
     * -.미 호출 시 아래와 같은 문제 발생
     * 1)유통증명시스템을 사용하는 경우 해당 API를 호출한 시점으로 열람정보가 등록되어 미 호출 시 열람정보가 등록 되지 않음.
     * 2)문서상태조회 API(/v1/documents/{document_binder_uuid}/status) 호출 시 read_at최초 열람시간) 데이터가 내려가지 않음.
     * </pre>
     * @param reqDTO KkopayDocAttrDTO.DocumentBinderUuid
     */
    void modifyStatus(final DocumentBinderUuid reqDTO);


    /**
     * <pre>
     * 문서 상태 조회 API
     * -.이용기관 서버에서 카카오페이 전자문서 서버로 문서 상태에 대한 조회를 요청 합니다.
     * : 발송된 문서의 진행상태를 알고 싶은 경우, flow와 상관없이 요청 가능
     * : polling 방식으로 호출할 경우, 호출 간격은 5초를 권장.
     * -.doc_box_status 상태변경순서
     * : SENT(송신) > RECEIVED(수신) > READ(열람)/EXPIRED(미열람자료의 기한만료)
     * </pre>
     * @param reqDTO KkopayDocAttrDTO.DocumentBinderUuid
     * @return KkopayDocDTO.DocStatusResponse
     */
    DocStatusResponse findStatus(final DocumentBinderUuid reqDTO);

    /**
     * <pre>
     * 대량(bulk) 문서발송 요청
     * -.이용기관 서버에서 카카오페이 내문서함 서버로 대량(bulk) 문서발송 처리를 요청합니다.
     * </pre>
     * @param reqDTO KkopayDocBulkDTO.BulkSendRequests
     * @return KkopayDocBulkDTO.BulkSendResponses
     */
    BulkSendResponses requestSendBulk(final BulkSendRequests reqDTO);

    /**
     * <pre>
     * 대량(bulk) 문서 상태 조회 API
     * -.이용기관 서버에서 카카오페이 전자문서 서버로 문서 상태에 대한 조회를 요청 합니다.
     * : 발송된 문서의 진행상태를 알고 싶은 경우, flow와 상관없이 요청 가능
     * : polling 방식으로 호출할 경우, 호출 간격은 5초를 권장.
     * -.doc_box_status 상태변경순서
     * : SENT(송신) > RECEIVED(수신) > READ(열람)/EXPIRED(미열람자료의 기한만료)
     * </pre>
     * @param reqDTO KkopayDocBulkDTO.BulkStatusRequests
     * @return KkopayDocBulkDTO.BulkStatusResponses
     */
    BulkStatusResponses findBulkStatus(final BulkStatusRequests reqDTO);


    ApiResponseDTO<ValidTokenResponse> findMyDocReadyAndMblPage(OneTimeToken reqDTO);
}

