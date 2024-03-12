package kr.xit.biz.cmm.mapper;

import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description : 전자고지 Cache mapper
 *               - cache: CaffeineCache use
 *
 * packageName : kr.xit.biz.cmm.mapper
 * fileName    : ICmmEnCacheMapper
 * author      : limju
 * date        : 2023-09-12
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-12    limju       최초 생성
 *
 * </pre>
 */
@Mapper
public interface ICmmEnsCacheMapper {
    NiceCiInfo selectNiceCiInfo(final String signguCode, final String ffnlgCode);
    CmmEnsRlaybsnmDTO selectEnsRlaybsnmInfo(final CmmEnsRequestDTO dto);
}
