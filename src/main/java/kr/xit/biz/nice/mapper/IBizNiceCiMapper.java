package kr.xit.biz.nice.mapper;

import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description : 업무 Nice CI mapper
 *
 * packageName : kr.xit.biz.nice.mapper
 * fileName    : IBizNiceCiMapper
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
public interface IBizNiceCiMapper {
    int updateNiceCrtfToken(final NiceCiInfo dto);
    int updateNiceCrtfPublickey(final NiceCiInfo dto);
    int updateNiceCrtfSymkey(final NiceCiInfo dto);

}
