package kr.xit.ens.nice.service;

import kr.xit.biz.ens.model.nice.NiceCiDTO.IpinCiResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceTokenResponse;
import kr.xit.biz.ens.model.nice.NiceCiDTO.PublickeyResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.SymkeyRegInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.SymkeyStatInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.TokenRevokeResponse;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.nice.service
 * fileName    : INiceCiService
 * author      : limju
 * date        : 2023-09-06
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-06    limju       최초 생성
 *
 * </pre>
 */
public interface INiceCiService {
    //--------------------------------------------------------------------------------
    // 기관용 Token
    //--------------------------------------------------------------------------------
    NiceTokenResponse generateToken(final NiceCiRequest reqDTO);
    TokenRevokeResponse revokeToken(final NiceCiRequest reqDTO);
    //--------------------------------------------------------------------------------
    // 기관용 Token
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------
    PublickeyResDataBody requestPublickey(final NiceCiRequest reqDTO);
    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------
    SymkeyStatInfo requestRegSymmetrickey(final NiceCiRequest reqDTO, final
    SymkeyRegInfo symkeyRegInfo);
    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 아이핀 CI 요청
    //--------------------------------------------------------------------------------
    IpinCiResDataBody requestCi(final NiceCiRequest reqDTO);
    //--------------------------------------------------------------------------------
    // 아이핀 CI 요청
    //--------------------------------------------------------------------------------
}
