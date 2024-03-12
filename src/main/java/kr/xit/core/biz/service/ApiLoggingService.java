package kr.xit.core.biz.service;

import java.util.List;
import kr.xit.core.biz.mapper.IApiLoggingMapper;
import kr.xit.core.biz.model.LoggingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.core.biz.service
 * fileName    : LoggingServiceImpl
 * author      : limju
 * date        : 2023-05-11
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-05-11    limju       최초 생성
 *
 * </pre>
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class ApiLoggingService extends EgovAbstractServiceImpl implements IApiLoggingService {
    private final IApiLoggingMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void saveApiLogging(final LoggingDTO reqDTO){
        mapper.saveApiLogging(reqDTO);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void modifyApiLogging(final LoggingDTO reqDTO){
        mapper.updateApiLogging(reqDTO);
    }

    @Override
    public List<LoggingDTO> findApiLogging(final LoggingDTO reqDTO) {
        return mapper.selectApiLogging(reqDTO);
    }

    @Override
    public LoggingDTO findApiLogging() {
        return mapper.selectApiLogging();
    }
}
