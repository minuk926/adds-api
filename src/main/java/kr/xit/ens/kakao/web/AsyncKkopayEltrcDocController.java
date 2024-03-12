package kr.xit.ens.kakao.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.kakao.KkopayDocAttrDTO.DocumentBinderUuid;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkStatusRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.SendRequest;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.ValidTokenRequest;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.ens.kakao.service.IAsyncKkopayEltrcDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description : 카카오 페이 전자 문서 발송 비동기용 controller
 * packageName : kr.xit.ens.kakao.web
 * fileName    : AsyncKkopayEltrcDocController
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 */
@Tag(name = "AsyncKkopayEltrcDocController", description = "카카오페이 MyDoc API(비동기)")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ens/kakao/v2")
public class AsyncKkopayEltrcDocController {

    private final IAsyncKkopayEltrcDocService service;

    /**
     * <pre>
     * 모바일웹 연계 문서발송 요청
     * -.이용기관 서버에서 전자문서 서버로 문서발송 처리를 요청합니다.
     * </pre>
     * @param reqDTO KkopayDocDTO.SendRequest
     * @return ApiResponseDTO<KkopayDocDTO.SendResponse>
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
        @Content(mediaType = "application/json", examples = {
            @ExampleObject(value = """
                {
                  "document": {
                    "title": "문서 제목",
                    "read_expired_sec": 3600,
                    "hash": "6EFE827AC88914DE471C621AE",
                    "common_categories": [
                      "NOTICE"
                    ],
                    "receiver": {
                      "phone_number": "01093414345",
                      "name": "김지호",
                      "birthday": "19831218",
                      "is_required_verify_name": false
                    },
                    "property": {
                      "link": "http://ip:8081/api/kakaopay/v1/ott",
                      "cs_number": "02-123-4567",
                      "cs_name": "콜센터",
                      "payload": "payload 파라미터 입니다.",
                      "message": "해당 안내문은 다음과 같습니다."
                    }
                  },
                  "signguCode": "88328",
                  "ffnlgCode": "11"
                }
            """)
        })
    })
    @Operation(hidden = true, summary = "문서발송 요청", description = "카카오페이 전자문서 서버로 문서발송 처리를 요청")
    @PostMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestSend(
        @RequestBody final SendRequest reqDTO
    ) {
        return ApiResponseDTO.of(service.requestSend(reqDTO));
    }


    /**
     * <pre>
     * 토큰 유효성 검증(Redirect URL  접속 허용/불허)
     * </pre>
     * @param reqDTO KkopayDocDTO.ValidTokenRequest
     * @return ApiResponseDTO<KkopayDocDTO.ValidTokenResponse>
     */
    @Operation(hidden = true, summary = "토큰 유효성 검증", description = "Redirect URL 접속 허용/불허")
    @PostMapping(value = "/validToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse validToken(
        @RequestBody final ValidTokenRequest reqDTO
    ) {
        return ApiResponseDTO.of(service.validToken(reqDTO));
    }

    /**
     * <pre>
     * 문서 상태 변경 API
     * -.문서에 대해서 열람 상태로 변경. 사용자가 문서열람 시(OTT 검증 완료 후 페이지 로딩 완료 시점) 반드시 문서 열람 상태 변경 API를 호출해야 함.
     * -.미 호출 시 아래와 같은 문제 발생
     * 1)유통증명시스템을 사용하는 경우 해당 API를 호출한 시점으로 열람정보가 등록되어 미 호출 시 열람정보가 등록 되지 않음.
     * 2)문서상태조회 API(/v1/documents/{document_binder_uuid}/status) 호출 시 read_at최초 열람시간) 데이터가 내려가지 않음.
     * </pre>
     * @param reqDTO KkopayDocAttrDTO.DocumentBinderUuid
     * @return ApiResponseDTO<Void>
     */
    @Operation(hidden = true, summary = "문서 상태 변경", description = "문서 상태 변경")
    @PostMapping(value = "/modifyStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse modifyStatus(
        @RequestBody final DocumentBinderUuid reqDTO
    ) {
        return ApiResponseDTO.of(service.modifyStatus(reqDTO));
    }

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
     * @return ApiResponseDTO<KkopayDocDTO.DocStatusResponse>
     */
    @Operation(hidden = true, summary = "문서 상태 조회", description = "문서 상태 조회")
    @PostMapping(value = "/findStatus", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findStatus(
        @RequestBody final DocumentBinderUuid reqDTO
    ) {
        return ApiResponseDTO.of(service.findStatus(reqDTO));
    }


    @Operation(hidden = true, summary = "대량 문서발송 요청", description = "카카오페이 전자문서 서버로 대량 문서발송 처리를 요청")
    @PostMapping(value = "/documents/bulk", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestSendBulk(
        @RequestBody final BulkSendRequests reqDTO
    ) {
        return ApiResponseDTO.of(service.requestSendBulk(reqDTO));
    }

    /**
     * <pre>
     * 모바일웹 연계 문서발송 요청
     * -.이용기관 서버에서 전자문서 서버로 문서발송 처리를 요청합니다.
     * </pre>
     * @param reqDTO KkopayDocBulkDTO.BulkStatusRequests
     * @return ApiResponseDTOApiResponseDTO<BulkStatusResponses.BulkStatusResponses>
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
        @Content(mediaType = "application/json", examples = {
            @ExampleObject(value = "{\"document_binder_uuids\": [\n"
                + "    \"BIN-883246dbff7b11edb3bb7affed8a016d\"\n"
                + "  ]}")
        })
    })
    @Operation(hidden = true, summary = "대량 문서 상태 조회 요청", description = "카카오페이 전자문서 서버로 대량 문서 상태 조회 요청")
    @PostMapping(value = "/documents/bulk/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findBulkStatus(
        @RequestBody final BulkStatusRequests reqDTO
    ) {
        return ApiResponseDTO.of(service.findBulkStatus(reqDTO));
    }
}
