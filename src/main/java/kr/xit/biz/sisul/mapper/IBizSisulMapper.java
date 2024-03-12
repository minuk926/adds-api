package kr.xit.biz.sisul.mapper;

import java.util.List;
import java.util.Optional;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO;
import kr.xit.biz.ens.model.cmm.TmplatManage;
import kr.xit.biz.ens.model.cntc.CntcDTO;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulRequest;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResDtlData;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResMstData;
import kr.xit.other.model.ElecnoticeDTO.Elecnoticedtl;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description : 전자고지 File mapper
 *
 * packageName : kr.xit.biz.sisul.mapper
 * fileName    : IBizSisulMapper
 * author      : seojh
 * date        : 2023-10-19
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-19    seojh       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IBizSisulMapper {
    Optional<TmplatManage> selectDeptInfoByTmplId(final String tmplatId);
    int insertCntcSndngMst(CntcDTO.SndngMst dto);
    int insertCntcSndngDtl(CntcDTO.SndngDtl dto);
    int insertCi(CmmEnsFileInfDTO.FmcInfExcelRslt dto);
    int insertPostPlusJson(CntcDTO.PostPlusJson dto);

    Optional<RsltSisulResMstData> selectSndngResultMaster(final RsltSisulRequest dto);
    List<RsltSisulResDtlData> selectSndngResultDetails(final RsltSisulRequest dto);
    List<Elecnoticedtl> selectOhterResult();
}
