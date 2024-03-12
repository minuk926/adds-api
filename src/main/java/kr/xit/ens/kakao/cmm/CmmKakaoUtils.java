package kr.xit.ens.kakao.cmm;

import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.spring.util.ApiSpringUtils;
import kr.xit.core.spring.util.CoreSpringUtils;
import kr.xit.core.spring.util.MessageUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.kakao.cmm
 * fileName    : CmmKakaoUtils
 * author      : limju
 * date        : 2023-09-19
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-19    limju       최초 생성
 *
 * </pre>
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmmKakaoUtils {
    private static final MessageUtil messageUtil = CoreSpringUtils.getMessageUtil();
    private static final ICmmEnsCacheService cacheService = ApiSpringUtils.getCmmEnsCacheService();

    /**
     * KT MMS 인증 정보 조회
     * @return
     */
    public static CmmEnsRlaybsnmDTO getRlaybsnmInfo(final CmmEnsRequestDTO dto) {
        final CmmEnsRlaybsnmDTO ensDTO = cacheService.getRlaybsnmInfoCache(dto);
        if(ObjectUtils.isEmpty(dto))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.rlaybsnm.info"));
        cacheService.logCache();

        return ensDTO;
    }


}
