package kr.xit.other.mapper;

import kr.xit.other.model.ElecnoticeDTO.Elecnoticedtl;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description : 전자고지 파일 외부연계 mapper
 *
 * packageName : kr.xit.biz.other.mapper
 * fileName    : IOtherMapper
 * author      : seojh
 * date        : 2024-01-03
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2024-01-03    seojh       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IOtherMapper {

    Elecnoticedtl selectElecnotice(final Elecnoticedtl dto);
    int saveElecnoticemst(final Elecnoticedtl dto);
    int saveElecnoticedtl(final Elecnoticedtl dto);
}
