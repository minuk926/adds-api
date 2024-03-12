package kr.xit.ens.pplus.mapper;

import java.util.List;
import kr.xit.biz.ens.model.cmm.SndngMssageParam;
import kr.xit.biz.ens.model.pplus.PplusDTO.BatchAcceptRequest;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.pplus.mapper
 * fileName    : IPplusMapper
 * author      : limju
 * date        : 2023-11-01
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-11-01    limju       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IPplusMapper {
    List<BatchAcceptRequest> selectPostPlusSendTgts(final SndngMssageParam dto);
}
