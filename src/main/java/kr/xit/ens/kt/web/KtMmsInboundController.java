package kr.xit.ens.kt.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMsgRsltRequest;
import kr.xit.biz.kt.service.IBizKtMmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description :  KT BC 에서 사용 되는 API
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
@Tag(name = "KtMmsInboundController", description = "KT MMS Inbound API")
@RequiredArgsConstructor
@RestController
public class KtMmsInboundController {

    private final IBizKtMmsService bizService;

    /**
     * <pre>
     * 사전/본 문자 발송/수신 결과 전송(BC-AG-SN-010)
     * - KT 에서 호출 되는 서비스
     * - url 고정 : http://{각대행사(IP:Port)}/api/ag/message/result
     * @param reqDTO KtMsgRsltRequest
     * @return KtCommonResponse
     * </pre>
     */
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
        @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    value = """
                        {
                             "service_cd" : "CHUMO",
                             "req_msg_type_dvcd" : "1",
                             "reqs" : [
                                 {
                                     "src_key" : "MEDKT202311030000011",
                                     "mms_sndg_rslt_sqno" : 1,
                                     "prcs_dt" : "20231103",
                                     "mms_bsns_dvcd" : "ME112",
                                     "mbl_bzowr_dvcd" : "02",
                                     "rl_mms_sndg_telno" : "4345",
                                     "mms_sndg_rslt_dvcd" : "40",
                                     "mms_sndg_tmst" : "20231103123912",
                                     "mms_rcv_tmst" : "20231103123916",
                                     "mms_rdg_tmst" : "",
                                     "prev_approve_yn" : "N",
                                     "msg_type" : "2",
                                     "rcv_npost" : "",
                                     "rcv_plfm_id": "",
                                     "click_dt": "",
                                     "approve_dt": "",
                                     "part_nm": null,
                                     "rcv_yn": "N"
                                 },
                                 {
                                     "src_key" : "MEDKT202311030000012",
                                     "mms_sndg_rslt_sqno" : 1,
                                     "prcs_dt" : "20231103",
                                     "mms_bsns_dvcd" : "ME112",
                                     "mbl_bzowr_dvcd" : "02",
                                     "rl_mms_sndg_telno" : null,
                                     "mms_sndg_rslt_dvcd" : "4V",
                                     "mms_sndg_tmst" : "20231103123917",
                                     "mms_rcv_tmst" : "20231103123917",
                                     "mms_rdg_tmst" : "",
                                     "prev_approve_yn" : "",
                                     "msg_type" : "2",
                                     "rcv_npost" : "",
                                     "rcv_plfm_id": "",
                                     "click_dt": "",
                                     "approve_dt": "",
                                     "part_nm": null,
                                     "rcv_yn": "N"
                                 }
                            ]
                        }
                        """
                ),
            })
    })
    @Operation(summary = "사전/본 문자 발송/수신 결과 전송 요청(BC-AG-SN-010) -> KT BC에서 호출", description = "사전/본 문자 발송/수신 결과 전송 요청(BC-AG-SN-010) -> bulk처리를 위해 업무단의 API를 사용(KT BC에서 호출)")
    @PostMapping(value = "/api/ag/message/result", produces = MediaType.APPLICATION_JSON_VALUE)
    public KtCommonResponse messageResult(@RequestBody final KtMsgRsltRequest reqDTO) {
        return bizService.messageResult(reqDTO);
    }
    //------------------------------------------------------------------------------
}
