package kr.xit.ens.nice.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.NiceCiWrkDiv;
import kr.xit.biz.ens.model.nice.NiceCiDTO.*;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.spring.util.ApiWebClientUtil;
import kr.xit.core.support.utils.DateUtils;
import kr.xit.core.support.utils.IpMacUtils;
import kr.xit.core.support.utils.JsonUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import kr.xit.ens.nice.cmm.CmmNiceCiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.nice.service
 * fileName    : NiceCiService
 * author      : limju
 * date        : 2023-09-06
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-06    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NiceCiService extends AbstractService implements INiceCiService {

    @Value("${app.contract.nice.host}")
    private String HOST;
    @Value("${app.contract.nice.api.generate-token}")
    private String API_GENERATE_TOKEN;
    @Value("${app.contract.nice.api.revoke-token}")
    private String API_REVOKE_TOKEN;
    @Value("${app.contract.nice.api.publickey}")
    private String API_PUBLICKEY;
    @Value("${app.contract.nice.api.symmetrickey}")
    private String API_SYMMETRICKEY;
    @Value("${app.contract.nice.api.ci}")
    private String API_CI;

    private static final String AUTH_TYPE_BASIC = "Basic";
    private static final String AUTH_TYPE_BEARER = "bearer";
    private static final String CNTY_CD = "ko";

    private final ApiWebClientUtil webClient;

    //--------------------------------------------------------------------------------
    // 기관용 Token
    //--------------------------------------------------------------------------------
    /**
     *
     * @return TokenResponse
     */
    @Override
    @TraceLogging
    public NiceTokenResponse generateToken(final NiceCiRequest reqDTO){
        final NiceTokenRequest tokenRequest = NiceTokenRequest.builder().build();
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getNiceCiInfo(reqDTO);
        CmmEnsUtils.validate(tokenRequest);

        final Map<String,String> map = new HashMap<>();
        map.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        map.put(HttpHeaders.AUTHORIZATION,
            String.format(
                "%s %s",
                AUTH_TYPE_BASIC,
                Base64Utils.encodeToString(
                    String.format(
                        "%s:%s",
                        niceDTO.getClientId(),
                        niceDTO.getClientSecret()).getBytes(StandardCharsets.UTF_8)
                )
            )
        );

        return webClient.exchangeFormData(HOST + API_GENERATE_TOKEN, HttpMethod.POST, tokenRequest, NiceTokenResponse.class, map);
    }

    /**
     * <pre>
     * Authorization : Basic + Base64Encoding(access_token:current_timestamp:client_id)
     * - access_token : 만료할 access_token
     * - client_id : access_token발급에 사용된 client_id
     * - current_timestamp
     *   Date currentDate = new Date();
     *   long current_timestamp = currentDate.getTime() /1000
     * @return
     * </pre>
     */
    @Override
    @TraceLogging
    public TokenRevokeResponse revokeToken(final NiceCiRequest reqDTO){
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getNiceCiInfo(reqDTO);
        if(ObjectUtils.isEmpty(niceDTO.getAccessToken()))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.token.info"));

        return webClient.exchangeFormData(
            HOST + API_REVOKE_TOKEN,
            HttpMethod.POST,
            null,
            TokenRevokeResponse.class,
            CmmNiceCiUtils.getAuthHeaderMap(
                MediaType.APPLICATION_FORM_URLENCODED_VALUE, AUTH_TYPE_BASIC,
                niceDTO.getAccessToken(),
                niceDTO.getClientId()
            )
        );
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------
    @Override
    @TraceLogging
    public PublickeyResDataBody requestPublickey(final NiceCiRequest reqDTO) {
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getNiceCiInfo(reqDTO);
        if(ObjectUtils.isEmpty(niceDTO.getAccessToken()))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.token.info"));

        final String todayDt = DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT);
        final String tranId = CmmEnsUtils.generateLengthUuid(24);
        final PublickeyRequest pubReqDTO = PublickeyRequest.builder()
            .dataHeader(RequestDataHeader.builder()
                .cntyCd(CNTY_CD)
                .tranId(tranId)
                .build())
            .dataBody(PublickeyReqDataBody.builder()
                .reqDtim(todayDt)
                .build())
            .build();
        CmmEnsUtils.validate(pubReqDTO);

        final Map<String,String> headerMap = CmmNiceCiUtils.getAuthHeaderMap(
            MediaType.APPLICATION_JSON_VALUE, AUTH_TYPE_BEARER,
            niceDTO.getAccessToken(),
            niceDTO.getClientId()
        );
        headerMap.put("ProductID", niceDTO.getProductId());

        final PublickeyResponse pubResDTO = webClient.exchange(
            HOST + API_PUBLICKEY,
            HttpMethod.POST,
            pubReqDTO,
            PublickeyResponse.class,
            headerMap
        );
        CmmNiceCiUtils.checkApiResponse(tranId, pubResDTO.getDataHeader(), pubResDTO.getDataBody(), NiceCiWrkDiv.PUBLIC_KEY);

        return pubResDTO.getDataBody();
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------
    /**
     * <pre>
     * 0. cache call
     *  --> 공개키 잔여일 수가 5보다 작으면
     * 1. 공개키 재발급후 공개키 정보 update
     * 2. cache 삭제 && cache call
     * @return
     * </pre>
     */
    @Override
    @TraceLogging
    public SymkeyStatInfo requestRegSymmetrickey(final NiceCiRequest reqDTO, final SymkeyRegInfo symkeyRegInfo) {
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getPublickeyNiceCiInfo(reqDTO);
        // siteCode set
        symkeyRegInfo.setSiteCode(niceDTO.getSiteCode());

        final String encSymkeyRegInfo = CmmEnsUtils.encSymkeyRegInfo(niceDTO.getPublicKey(), JsonUtils.toJson(symkeyRegInfo));
        final String tranId = CmmEnsUtils.generateLengthUuid(24);
        final SymmetrickeyRequest symReqDTO = SymmetrickeyRequest.builder()
            .dataHeader(RequestDataHeader.builder()
                .cntyCd(CNTY_CD)
                .tranId(tranId)
                .build())
            .dataBody(SymmetrickeyReqDataBody.builder()
                .pubkeyVersion(niceDTO.getKeyVersion())
                .symkeyRegInfo(encSymkeyRegInfo)
                .build())
            .build();
        CmmEnsUtils.validate(symReqDTO);

        final Map<String,String> headerMap = CmmNiceCiUtils.getAuthHeaderMap(
            MediaType.APPLICATION_JSON_VALUE, AUTH_TYPE_BEARER,
            niceDTO.getAccessToken(),
            niceDTO.getClientId()
        );
        headerMap.put("ProductID", niceDTO.getProductId());

        final SymmetrickeyResponse symResDTO = webClient.exchange(
            HOST + API_SYMMETRICKEY,
            HttpMethod.POST,
            symReqDTO,
            SymmetrickeyResponse.class,
            headerMap
        );
        SymmetrickeyResDataBody dataBody = symResDTO.getDataBody();
        CmmNiceCiUtils.checkApiResponse(tranId, symResDTO.getDataHeader(), dataBody, NiceCiWrkDiv.SYM_KEY);

        return JsonUtils.toObject(dataBody.getSymkeyStatInfo(), SymkeyStatInfo.class);
    }
    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 아이핀 CI 요청
    //--------------------------------------------------------------------------------
    @Override
    @TraceLogging
    public IpinCiResDataBody requestCi(final NiceCiRequest reqDTO) {
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getSymkeyNiceCiInfo(reqDTO);
        final IpinCiReqEncData encDataDTO = IpinCiReqEncData.builder()
            .siteCode(niceDTO.getSiteCode())
            //.infoReqType()  //default: 1-CI제공
            .reqDtim(DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT))
            .juminId(reqDTO.getJuminId())
            .reqNo(CmmEnsUtils.generateLengthUuid(30))

            .clientIp(IpMacUtils.getIpAddress().get(0))
            .build();
        CmmEnsUtils.validate(encDataDTO);

        final String tranId = CmmEnsUtils.generateLengthUuid(24);
        final String jsonEncData = CmmEnsUtils.encodeAesData(niceDTO.getCurSymkeyKey(), niceDTO.getCurSymkeyIv(), JsonUtils.toJson(encDataDTO));
        final String integrityValue = CmmEnsUtils.encodeHmacSha256(niceDTO.getCurSymkeyHmacKey(), jsonEncData);
        final IpinCiRequest ipinCiRequest = IpinCiRequest.builder()
            .dataHeader(RequestDataHeader.builder()
                .cntyCd(CNTY_CD)
                .tranId(tranId)
                .build())
            .dataBody(IpinCiReqDataBody.builder()
                .symkeyVersion(niceDTO.getCurSymkeyVersion())
                .encData(jsonEncData)
                .integrityValue(integrityValue)
                .build())
            .build();
        CmmEnsUtils.validate(ipinCiRequest);

        final Map<String,String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headerMap.put(HttpHeaders.AUTHORIZATION,
            String.format("%s %s",
                AUTH_TYPE_BASIC,
                Base64Utils.encodeToString(
                    String.format(
                            "%s:%s",
                            niceDTO.getClientId(),
                            niceDTO.getClientSecret()).getBytes(StandardCharsets.UTF_8)
                )
            )
        );
        headerMap.put("ProductID", niceDTO.getProductId());

        final IpinCiResponse ipinCiResponse = webClient.exchange(
            HOST + API_CI,
            HttpMethod.POST,
            ipinCiRequest,
            IpinCiResponse.class,
            headerMap
        );

        final IpinCiResDataBody dataBody = ipinCiResponse.getDataBody();
        CmmNiceCiUtils.checkApiResponse(tranId, ipinCiResponse.getDataHeader(), dataBody, NiceCiWrkDiv.CI);

        final String encData = dataBody.getEncData();
        final String resIntegrityValue = dataBody.getIntegrityValue();
        final String checkData = CmmEnsUtils.encodeHmacSha256(niceDTO.getCurSymkeyHmacKey(), encData);
        if(resIntegrityValue.equals(checkData)){
            final String decStr = CmmEnsUtils.decodeAesData(encData, niceDTO.getCurSymkeyKey(), niceDTO.getCurSymkeyIv());
            dataBody.setEncData(decStr);

            //return JsonUtils.toObject(decStr, IpinCiResEncData.class);
            return dataBody;
        }
        throw BizRuntimeException.create("CI 변환 응답 데이터에 오류가 있습니다(무결성 체크 오류)");
    }
    //--------------------------------------------------------------------------------
}
