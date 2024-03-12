package kr.xit.core.biz.mapper;

import egovframework.com.cmm.model.LoginVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.core.biz.mapper
 * fileName    : IAuthApiMapper
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
public interface IAuthApiMapper {
    LoginVO actionLogin(LoginVO vo);
    // LoginVO searchId(LoginVO vo);
    // LoginVO searchPassword(LoginVO vo);
    // void updatePassword(LoginVO vo);
}
