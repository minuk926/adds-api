package kr.xit.biz.sisul.service;

import java.util.List;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO.FmcExcelUpload;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulRequest;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResDtlData;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulResMstData;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.sisul.service
 * fileName    : IBizSisulService
 * author      : limju
 * date        : 2023-09-05
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-05    limju       최초 생성
 *
 * </pre>
 */
public interface IBizSisulService {

    String fmcExcelUpload(final FmcExcelUpload fileReq);

    RsltSisulResMstData findSndngResultMaster(final RsltSisulRequest dto);
    List<RsltSisulResDtlData> findSndngResultDetails(final RsltSisulRequest dto);
}
