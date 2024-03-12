package kr.xit.core.biz.mapper;

import java.util.List;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import kr.xit.core.biz.model.LoggingDTO;

/**
 * <pre>
 * description : API db logging
 *
 * packageName : kr.xit.core.biz.mapper
 * fileName    : IApiLoggingMapper
 * author      : limju
 * date        : 2023-05-11
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-05-11    limju       최초 생성
 *
 * </pre>
 */
@Mapper
public interface IApiLoggingMapper {

    List<LoggingDTO> selectApiLogging(final LoggingDTO reqDTO);
    LoggingDTO selectApiLogging();

    void saveApiLogging(final LoggingDTO reqDTO);
    void updateApiLogging(final LoggingDTO reqDTO);
}
