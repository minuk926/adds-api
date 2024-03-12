package kr.xit.core.biz.service;

import egovframework.com.cmm.model.LoginVO;
import egovframework.com.cmm.util.EgovFileScrty;
import javax.annotation.Resource;
import kr.xit.core.biz.mapper.IAuthApiMapper;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AuthApiService extends EgovAbstractServiceImpl implements IAuthApiService {

	@Resource
	private IAuthApiMapper mapper;

	/**
	 * 일반 로그인 처리
	 * @param vo LoginVO
	 * @return LoginVO
	 */
	@Override
	public LoginVO actionLogin(LoginVO vo) {

		// 1. 입력한 비밀번호를 암호화한다.
		String enpassword = EgovFileScrty.encryptPassword(vo.getPassword(), vo.getId());
		vo.setPassword(enpassword);

		// 2. 아이디와 암호화된 비밀번호가 DB와 일치하는지 확인한다.
		LoginVO loginVO = mapper.actionLogin(vo); //loginDAO.actionLogin(vo);

		// 3. 결과를 리턴한다.
		if (loginVO != null && !loginVO.getId().equals("") && !loginVO.getPassword().equals("")) {
			return loginVO;
		} else {
			loginVO = new LoginVO();
		}

		return loginVO;
	}
}
