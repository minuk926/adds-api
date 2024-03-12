package kr.xit.ens.epost.service;

import java.util.HashMap;
import java.util.Map;
import kr.xit.biz.common.ApiConstants.SndngSeCode;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.epost.EPostDTO.EpostTraceRequest;
import kr.xit.biz.ens.model.epost.EPostDTO.EpostTraceResponse;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.spring.util.ApiWebClientUtil;
import kr.xit.ens.cmm.CmmEnsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.epost.service
 * fileName    : EpostService
 * author      : limju
 * date        : 2023-10-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-04    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EpostService extends AbstractService implements IEpostService {
    @Value("${app.contract.epost.host}")
    private String HOST;

    @Value("${app.contract.epost.api.postTrackInfo}")
    private String POST_TRACK_INFO;

    private final ApiWebClientUtil webClient;

    @Override
    @TraceLogging
    public EpostTraceResponse postTrackInfo(final EpostTraceRequest reqDTO) {
        final CmmEnsRlaybsnmDTO ktMnsInfo = CmmEnsUtils.getRlaybsnmInfo(reqDTO.getSignguCode(), reqDTO.getFfnlgCode(),
            SndngSeCode.PPLUS);reqDTO.setServiceKey(ktMnsInfo.getEpostServiceKey());

        final Map<String,String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        reqDTO.setSignguCode(null);
        reqDTO.setFfnlgCode(null);
        reqDTO.setProfile(null);
        CmmEnsUtils.validate(reqDTO);

        return webClient.exchange(
            HOST + POST_TRACK_INFO + String.format("?serviceKey=%s&rgist=%s",reqDTO.getServiceKey(), reqDTO.getRgist()),
            HttpMethod.GET,
            reqDTO,
            EpostTraceResponse.class,
            headerMap
        );
    }
}
