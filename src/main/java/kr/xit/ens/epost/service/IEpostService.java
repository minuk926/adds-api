package kr.xit.ens.epost.service;

import kr.xit.biz.ens.model.epost.EPostDTO.EpostTraceRequest;
import kr.xit.biz.ens.model.epost.EPostDTO.EpostTraceResponse;

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
public interface IEpostService {
    EpostTraceResponse postTrackInfo(final EpostTraceRequest reqDTO);
}
