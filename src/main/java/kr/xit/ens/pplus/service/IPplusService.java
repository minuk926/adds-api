package kr.xit.ens.pplus.service;

import kr.xit.biz.ens.model.cmm.SndngMssageParam;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpCommonResponse;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusRequest;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusResponse;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.pplus.service
 * fileName    : IPplusService
 * author      : limju
 * date        : 2023-10-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-04    limju       최초 생성
 *
 * </pre>
 */
public interface IPplusService {
    PpCommonResponse sendBulks(final SndngMssageParam reqDTO);
    PpStatusResponse statusBulks(final PpStatusRequest reqDTO);
}
