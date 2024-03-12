package kr.xit.biz.cmm.service;

import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;

/**
 * <pre>
 * description : CaffeineCache 적용
 *
 * packageName : kr.xit.biz.cmm.service
 * fileName    : ICmmEnsCacheService
 * author      : limju
 * date        : 2023-09-12
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-12    limju       최초 생성
 *
 * </pre>
 */
public interface ICmmEnsCacheService {
    NiceCiInfo getNiceCiInfoCache(final String signguCode, final String ffnlgCode);
    void removeNiceCiInfoCache(final String signguCode, final String ffnlgCode);

    CmmEnsRlaybsnmDTO getRlaybsnmInfoCache(final CmmEnsRequestDTO dto);
    void removeRlaybsnmInfoCache(final CmmEnsRequestDTO dto);

    void logCache();
}
