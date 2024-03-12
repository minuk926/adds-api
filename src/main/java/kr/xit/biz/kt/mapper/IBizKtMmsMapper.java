package kr.xit.biz.kt.mapper;

import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMsgRsltReqData;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.kt.mapper
 * fileName    : IBizKtMmsMapper
 * author      : limju
 * date        : 2023-10-12
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-12    limju       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IBizKtMmsMapper {
    int updateRlaybsnmKtInfo(final CmmEnsRlaybsnmDTO dto);
    int updateKtBcDtl(final KtMsgRsltReqData dto);
    int saveKtCntcSndngResult(final KtMsgRsltReqData dto);
}
