package kr.xit.biz.kt.service;

import java.util.List;
import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.kt.KtCommonDTO.ErrorMsg;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMsgRsltReqData;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMsgRsltRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenResponse;
import kr.xit.biz.kt.mapper.IBizKtMmsMapper;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.support.utils.Checks;
import kr.xit.core.support.utils.DateUtils;
import kr.xit.ens.cmm.CmmEnsUtils;
import kr.xit.ens.kt.service.IKtMmsService;
import kr.xit.ens.kt.web.KtMmsInboundController;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.kt.service
 * fileName    : BizKtMmsService
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
@RequiredArgsConstructor
@Service
public class BizKtMmsService extends AbstractService implements IBizKtMmsService {
    private final IKtMmsService ktMmsService;
    private final ICmmEnsCacheService cacheService;
    private final IBizKtMmsMapper mapper;

    /**
     * <pre>
     * kt Access token 획득
     * 1. kt Access token 요청
     * 2. 요청결과 DB 반영
     * 3. 정보중계자 정보 캐시 삭제
     * @param paramDTO KtMnsRequest
     * @return KtTokenResponse
     * </pre>
     */
    @Override
    public KtTokenResponse requestToken(final KtMnsRequest paramDTO) {
        KtTokenResponse resDTO = ktMmsService.requestToken(paramDTO);

        if(StringUtils.isEmpty(resDTO.getAccessToken())){
            throw BizRuntimeException.create("fail.api.kt.token.request");
        }
        mapper.updateRlaybsnmKtInfo(
            CmmEnsRlaybsnmDTO.builder()
                .signguCode(paramDTO.getSignguCode())
                .ffnlgCode(paramDTO.getFfnlgCode())
                .profile(ApiConstants.PROFILE)
                .ktAccessToken(resDTO.getAccessToken())
                .ktTokenExpiresIn(resDTO.getExpiresIn())
                .ktScope(resDTO.getScope())
                .ktTokenJti(resDTO.getJti())
                .build()
        );
        cacheService.removeRlaybsnmInfoCache(
            CmmEnsRequestDTO.builder()
                .signguCode(paramDTO.getSignguCode())
                .ffnlgCode(paramDTO.getFfnlgCode())
                .profile(paramDTO.getProfile())
                .build()
        );
        return resDTO;
    }

    /**
     * <pre>
     * 사전/본 문자 발송/수신 결과 전송
     * - KT 에서 호출 되는 서비스
     * - http://{각대행사(IP:Port)}/api/ag/message/result
     * {@link KtMmsInboundController#messageResult messageResult}
     *
     * 본문자 수신 등록 요청시 보낸 묶음 단위로 처리
     * -> 처리중 1건이라도 실패시, 전체 건 재 전송
     * -> 업무단에서는 건별 처리하도록 구현
     *
     * @param reqDTO KtMsgRsltRequest
     * @return KtCommonResponse
     * </pre>
     */
    @Override
    @TraceLogging
    @Transactional
    public KtCommonResponse messageResult(KtMsgRsltRequest reqDTO) {
        List<ErrorMsg> errors = CmmEnsUtils.getValidateErrors(reqDTO);

        if(!errors.isEmpty())   return throwError(errors);

        for(KtMsgRsltReqData dto : reqDTO.getReqs()){

            // 결과코드가 '40' 이고 열람타임스탬프가 있는 경우 -> 결과코드 '열람확인:60'으로 reset
            if("40".equals(dto.getMmsSndgRsltDvcd()) && Checks.isNotEmpty(dto.getMmsRdgTmst())){
                dto.setMmsSndgRsltDvcd("60");
            }
            dto.setMmsSndgRsltDvcdMsg(messageUtil.getMessage("info.api.kt.msg.rslt."+dto.getMmsSndgRsltDvcd()));
            if(mapper.updateKtBcDtl(dto) != 1) {
                errors.add(new ErrorMsg(
                    String.format("존재 하지 않는 발송 대상(src_key[%s], mms_sndg_rslt_sqno[%s]) 입니다",
                        dto.getSrcKey(), dto.getMmsSndgRsltSqno())));
            }
            mapper.saveKtCntcSndngResult(dto);
        }

        return KtCommonResponse.builder()
            .resultCd("00")
            .resultDt(DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT))
            .build();
    }

    private static KtCommonResponse throwError(List<ErrorMsg> errors) {
        return KtCommonResponse.builder()
            .resultCd("01")
            .resultDt(DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_EMPTY_DLT))
            .errors(errors)
            .build();
    }
}
