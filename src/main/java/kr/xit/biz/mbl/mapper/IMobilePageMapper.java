package kr.xit.biz.mbl.mapper;

import kr.xit.biz.ens.model.kakao.KkopayDocDTO.OneTimeToken;
import kr.xit.biz.mbl.model.MobilePageDTO.MobilePageManage;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description : 전자문서 중개자 모바일 페이지 API mapper
 *
 * packageName : kr.xit.biz.mbl.mapper
 * fileName    : IMobilePageMapper
 * author      : limju
 * date        : 2023-08-31
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-08-31    limju       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IMobilePageMapper {
    MobilePageManage selectKkoMobilePage(final OneTimeToken dto);
    <T> MobilePageManage selectKtMobilePage(final T t);
}
