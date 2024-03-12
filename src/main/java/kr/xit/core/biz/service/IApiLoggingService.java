package kr.xit.core.biz.service;

import java.util.List;

import kr.xit.core.biz.model.LoggingDTO;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.core.biz.service
 * fileName    : ILoggingService
 * author      : limju
 * date        : 2023-05-11
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-05-11    limju       최초 생성
 *
 * </pre>
 */
public interface IApiLoggingService {
    List<LoggingDTO> findApiLogging(LoggingDTO reqDTO);
    LoggingDTO findApiLogging();
    void saveApiLogging(LoggingDTO reqDTO);
    void modifyApiLogging(LoggingDTO reqDTO);
}
