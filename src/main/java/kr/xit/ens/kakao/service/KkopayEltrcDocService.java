package kr.xit.ens.kakao.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import kr.xit.biz.common.ApiConstants.SndngSeCode;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.kakao.KkopayDocAttrDTO.DocumentBinderUuid;
import kr.xit.biz.ens.model.kakao.KkopayDocAttrDTO.Receiver;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendReq;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkSendResponses;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkStatusRequests;
import kr.xit.biz.ens.model.kakao.KkopayDocBulkDTO.BulkStatusResponses;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.DocStatusResponse;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.OneTimeToken;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.RequestSend;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.SendRequest;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.SendResponse;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.ValidTokenRequest;
import kr.xit.biz.ens.model.kakao.KkopayDocDTO.ValidTokenResponse;
import kr.xit.biz.ens.model.kakao.KkopayErrorDTO;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.spring.util.ApiWebClientUtil;
import kr.xit.core.support.utils.Checks;
import kr.xit.core.support.utils.JsonUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * description : 카카오 페이 전자 문서 발송 요청 서비스
 * packageName : kr.xit.ens.kakao.service
 * fileName    : KkopayEltrcDocService
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class KkopayEltrcDocService extends AbstractService implements
    IKkopayEltrcDocService {

    @Value("${app.contract.kakao.host}")
    private String HOST;
    @Value("#{'${app.contract.kakao.api.send}'.split(';')}")
    private String[] API_SEND;
    @Value("#{'${app.contract.kakao.api.validToken}'.split(';')}")
    private String[] API_VALID_TOKEN;
    @Value("#{'${app.contract.kakao.api.modifyStatus}'.split(';')}")
    private String[] API_MODIFY_STATUS;
    @Value("#{'${app.contract.kakao.api.findStatus}'.split(';')}")
    private String[] API_STATUS;
    @Value("#{'${app.contract.kakao.api.bulksend}'.split(';')}")
    private String[] API_BULKSEND;
    @Value("#{'${app.contract.kakao.api.bulkstatus}'.split(';')}")
    private String[] API_BULKSTATUS;

    private final ApiWebClientUtil webClient;
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final CharSequence DOCUMENT_BINDER_UUID = "{document_binder_uuid}";


    /**
     * <pre>
     * 모바일웹 연계 문서발송 요청 : POST
     * -.이용기관 서버에서 전자문서 서버로 문서발송 처리 요청
     * </pre>
     * @param reqDTO KkoPayEltrDocDTO.RequestSendReq
     * @return ApiResponseDTO<KkopayDocDTO.SendResponse>
     */
    @Override
    @TraceLogging
    public SendResponse requestSend(final SendRequest reqDTO) {
        List<String> errors = new ArrayList<>();
        errors = validate(reqDTO.getDocument(), errors);

        final RequestSend reqSendDTO = reqDTO.getDocument();
        if(reqSendDTO.getRead_expired_at() != null && reqSendDTO.getRead_expired_sec() != null){
            Objects.requireNonNull(errors).add("처리마감시간(절대시간 또는 상대시간)은 하나만 지정해야 합니다.");
        }
        if(reqSendDTO.getRead_expired_at() == null && reqSendDTO.getRead_expired_sec() == null){
            Objects.requireNonNull(errors).add("처리마감시간(절대시간 또는 상대시간)을 지정해야 합니다.");
        }

        final Receiver receiver = reqSendDTO.getReceiver();
        if(Checks.isEmpty(receiver.getCi())){
            if(Checks.isEmpty(receiver.getName()))          Objects.requireNonNull(errors).add("receiver.name=받는이 이름은 필수입니다.");
            if(Checks.isEmpty(receiver.getPhone_number()))  Objects.requireNonNull(errors).add("receiver.phone_number=받는이 전화번호는 필수입니다.");
            if(Checks.isEmpty(receiver.getBirthday()))      Objects.requireNonNull(errors).add("receiver.birthday=받는이 생년월일은 필수입니다.");
        }
        if(!Objects.requireNonNull(errors).isEmpty()) throw BizRuntimeException.create(errors.toString());
        return webClient.exchangeKko(HOST + API_SEND[0], HttpMethod.valueOf(API_SEND[1]), JsonUtils.toJson(reqDTO), SendResponse.class, getRlaybsnmInfo(reqDTO));
    }

    /**
     * <pre>
     * 토큰 유효성 검증(Redirect URL  접속 허용/불허) : GET
     * </pre>
     * @param reqDTO KkopayDocDTO.ValidTokenRequest
     * @return ApiResponseDTO<KkopayDocDTO.ValidTokenResponse>
     */
    @Override
    @TraceLogging
    public ValidTokenResponse validToken(final ValidTokenRequest reqDTO) {
        validate(reqDTO, null);

        final String url = HOST
            + API_VALID_TOKEN[0].replace(DOCUMENT_BINDER_UUID, reqDTO.getDocument_binder_uuid())
                                .replace("{tokens}", reqDTO.getToken());
        return webClient.exchangeKko(url, HttpMethod.valueOf(API_VALID_TOKEN[1]), null, ValidTokenResponse.class, getRlaybsnmInfo(reqDTO));
    }

    /**
     * <pre>
     * 문서 상태 변경 API : POST
     * -.문서에 대해서 열람 상태로 변경. 사용자가 문서열람 시(OTT 검증 완료 후 페이지 로딩 완료 시점) 반드시 문서 열람 상태 변경 API를 호출해야 함.
     * -.미 호출 시 아래와 같은 문제 발생
     * 1)유통증명시스템을 사용하는 경우 해당 API를 호출한 시점으로 열람정보가 등록되어 미 호출 시 열람정보가 등록 되지 않음.
     * 2)문서상태조회 API(/v1/documents/{document_binder_uuid}/status) 호출 시 read_at최초 열람시간) 데이터가 내려가지 않음.
     * </pre>
     * @param reqDTO KkopayDocAttrDTO.DocumentBinderUuid
     */
    @Override
    @TraceLogging
    public void modifyStatus(final DocumentBinderUuid reqDTO){
        validate(reqDTO, null);

        final String body = "{\"document\": {\"is_detail_read\": true} }";
        final String url = HOST + API_MODIFY_STATUS[0].replace(DOCUMENT_BINDER_UUID, reqDTO.getDocument_binder_uuid());

        webClient.exchangeKko(url, HttpMethod.valueOf(API_MODIFY_STATUS[1]), body, Void.class, getRlaybsnmInfo(reqDTO));
    }

    /**
     * <pre>
     * 문서 상태 조회 API : GET
     * -.이용기관 서버에서 카카오페이 전자문서 서버로 문서 상태에 대한 조회를 요청 합니다.
     * : 발송된 문서의 진행상태를 알고 싶은 경우, flow와 상관없이 요청 가능
     * : polling 방식으로 호출할 경우, 호출 간격은 5초를 권장.
     * -.doc_box_status 상태변경순서
     * : SENT(송신) > RECEIVED(수신) > READ(열람)/EXPIRED(미열람자료의 기한만료)
     * </pre>
     * @param reqDTO KkopayDocAttrDTO.DocumentBinderUuid
     * @return KkopayDocDTO.DocStatusResponse
     */
    @Override
    @TraceLogging
    public DocStatusResponse findStatus(final DocumentBinderUuid reqDTO){
        validate(reqDTO, null);

        final String url = HOST + API_STATUS[0].replace(DOCUMENT_BINDER_UUID, reqDTO.getDocument_binder_uuid());
        return webClient.exchangeKko(url, HttpMethod.valueOf(API_STATUS[1]), null, DocStatusResponse.class, getRlaybsnmInfo(reqDTO));
    }

    /**
     * <pre>
     * 모바일웹 연계 문서발송 요청 : POST
     * -.이용기관 서버에서 전자문서 서버로 문서발송 처리를 요청합니다.
     * </pre>
     * @param reqDTO KkopayDocBulkDTO.BulkSendRequests
     * @return KkopayDocBulkDTO.BulkSendResponses
     */
    @Override
    @TraceLogging
    public BulkSendResponses requestSendBulk(final BulkSendRequests reqDTO) {
        List<String> errors = new ArrayList<>();

        List<BulkSendReq> dtos = reqDTO.getDocuments();
        for(int idx = 0; idx < dtos.size(); idx++) {
            final Set<ConstraintViolation<BulkSendReq>> list = validator.validate(dtos.get(idx));
            if (!list.isEmpty()) {

                int finalIdx = idx;
                errors.addAll(list.stream()
                    .map(row -> String.format("%s[%d]=%s", row.getPropertyPath(), finalIdx +1, row.getMessageTemplate()))
                    .toList()
                );
            }
        }

        for(int idx = 0; idx < dtos.size(); idx++) {
            if(dtos.get(idx).getRead_expired_at() != null && dtos.get(idx).getRead_expired_sec() != null){
                errors.add("처리마감시간(절대시간 또는 상대시간)은 하나만 지정해야 합니다.");
            }
            if(dtos.get(idx).getRead_expired_at() == null && dtos.get(idx).getRead_expired_sec() == null){
                errors.add("처리마감시간(절대시간 또는 상대시간)을 지정해야 합니다.");
            }

            final Receiver receiver = dtos.get(idx).getReceiver();
            if (Checks.isEmpty(receiver.getCi())) {
                if (Checks.isEmpty(receiver.getName()))         errors.add(String.format("받는이 이름은 필수입니다(receiver.name[%d] 번째 오류)", idx+1));
                if (Checks.isEmpty(receiver.getPhone_number())) errors.add(String.format("받는이 전화번호는 필수입니다(receiver.phone_number[%d] 번째 오류)", idx+1));
                if (Checks.isEmpty(receiver.getBirthday()))     errors.add(String.format("받는이 생년월일은 필수입니다(receiver.birthday[%d] 번째 오류)", idx+1));
            } else {
                final StringBuilder sb = new StringBuilder()
                    .append(StringUtils.defaultString(receiver.getName(), StringUtils.EMPTY))
                    .append(StringUtils.defaultString(receiver.getPhone_number(), StringUtils.EMPTY))
                    .append(StringUtils.defaultString(receiver.getBirthday(), StringUtils.EMPTY));

                if(Checks.isNotEmpty(sb.toString())){
                    errors.add(String.format("CI가 지정 되었습니다(받는이 정보 불필요:[%d] 번째 오류) .", idx+1));
                }
            }
        }
        if(!errors.isEmpty()){
            throw BizRuntimeException.create(errors.toString());
        }

        return webClient.exchangeKko(HOST + API_BULKSEND[0], HttpMethod.valueOf(API_BULKSEND[1]), JsonUtils.toJson(reqDTO), BulkSendResponses.class, getRlaybsnmInfo(reqDTO));
    }

    /**
     * <pre>
     * 대량(bulk) 문서 상태 조회 API : POST
     * -.이용기관 서버에서 카카오페이 전자문서 서버로 문서 상태에 대한 조회를 요청 합니다.
     * : 발송된 문서의 진행상태를 알고 싶은 경우, flow와 상관없이 요청 가능
     * : polling 방식으로 호출할 경우, 호출 간격은 5초를 권장.
     * -.doc_box_status 상태변경순서
     * : SENT(송신) > RECEIVED(수신) > READ(열람)/EXPIRED(미열람자료의 기한만료)
     * </pre>
     * @param reqDTO KkopayDocBulkDTO.BulkStatusRequests
     * @return KkopayDocBulkDTO.BulkStatusResponses
     */
    @Override
    @TraceLogging
    public BulkStatusResponses findBulkStatus(final BulkStatusRequests reqDTO) {
        List<String> errors = new ArrayList<>();

        List<String> dtos = reqDTO.getDocument_binder_uuids();
        for(int idx = 0; idx < dtos.size(); idx++) {
            final String binderUuid = dtos.get(idx);
            if (Checks.isEmpty(binderUuid) || binderUuid.length() > 40) {
                errors.add(String.format("문서 식별 번호는 40자를 넘을 수 없습니다[%d번째]", idx+1));
            }
        }
        if(!errors.isEmpty()) {
            throw BizRuntimeException.create(errors.toString());
        }
        return webClient.exchangeKko(HOST + API_BULKSTATUS[0], HttpMethod.valueOf(API_BULKSTATUS[1]), JsonUtils.toJson(reqDTO), BulkStatusResponses.class, getRlaybsnmInfo(reqDTO));
    }

    @Override
    public ApiResponseDTO<ValidTokenResponse> findMyDocReadyAndMblPage(final OneTimeToken reqDTO) {
        final String url = HOST + API_VALID_TOKEN[0].replace(DOCUMENT_BINDER_UUID, reqDTO.getDocument_binder_uuid())
                .replace("{tokens}", reqDTO.getToken());

        // 유효성 검증
        final ValidTokenResponse validTokenRes = webClient.exchangeKko(url, HttpMethod.valueOf(API_VALID_TOKEN[1]), null,
                ValidTokenResponse.class, getRlaybsnmInfo(reqDTO));

        if(!"USED".equals(validTokenRes.getToken_status())){
            return ApiResponseDTO.error(validTokenRes.getError_code(), validTokenRes.getError_message());
        }

        // 문서상태 변경
        final String body = "{\"document\": {\"is_detail_read\": true} }";
        final String url2 = HOST + API_MODIFY_STATUS[0].replace(DOCUMENT_BINDER_UUID, reqDTO.getDocument_binder_uuid());

        // 정상 : HttpStatus.NO_CONTENT(204) return
        // error : body에 error_code, error_message return
        final KkopayErrorDTO errorDTO = webClient.exchangeKko(url2, HttpMethod.valueOf(API_MODIFY_STATUS[1]), body, KkopayErrorDTO.class, getRlaybsnmInfo(reqDTO));
        if(errorDTO != null){
            return ApiResponseDTO.error(errorDTO.getErrorCode(), errorDTO.getErrorMessage());
        }


        return ApiResponseDTO.success();
    }

    //-------------------------------------------------------------------------------------------------------------------
    private static <T> List<String> validate(T t, List<String> errList) {
        final Set<ConstraintViolation<T>> list = validator.validate(t);

        if(!list.isEmpty()) {
            final List<String> errors = list.stream()
                .map(row -> String.format("%s=%s", row.getPropertyPath(), row.getMessageTemplate()))
                .toList();

            // 추가적인 유효성 검증이 필요 없는 경우
            if(errList == null){
                if(!errors.isEmpty())   throw BizRuntimeException.create(errors.toString());
                return null;
            }
            errList.addAll(errors);
        }
        return errList;
    }

    private CmmEnsRlaybsnmDTO getRlaybsnmInfo(final CmmEnsRequestDTO request){
        return CmmEnsUtils.getRlaybsnmInfo(request.getSignguCode(), request.getFfnlgCode(), SndngSeCode.KAKAO);
    }
}
