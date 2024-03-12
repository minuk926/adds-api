package kr.xit.biz.nice.service;

import kr.xit.biz.cmm.service.CmmEnsCacheService;
import kr.xit.biz.common.ApiConstants.NiceCiWrkDiv;
import kr.xit.biz.common.ApiConstants.SignguCode;
import kr.xit.biz.ens.model.nice.NiceCiDTO.IpinCiResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceTokenResponse;
import kr.xit.biz.ens.model.nice.NiceCiDTO.PublickeyResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.ResponseDataHeader;
import kr.xit.biz.ens.model.nice.NiceCiDTO.SymkeyRegInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.SymkeyStatInfo;
import kr.xit.biz.ens.model.nice.NiceCiDTO.TokenResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.TokenRevokeResDataBody;
import kr.xit.biz.ens.model.nice.NiceCiDTO.TokenRevokeResponse;
import kr.xit.biz.nice.mapper.IBizNiceCiMapper;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.service.AbstractService;
import kr.xit.core.support.utils.JsonUtils;
import kr.xit.ens.nice.cmm.CmmNiceCiUtils;
import kr.xit.ens.nice.service.INiceCiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <pre>
 * description : Nice CI 업무 처리 service
 *
 * packageName : kr.xit.biz.nice.service
 * fileName    : BizNiceCiService
 * author      : limju
 * date        : 2023-09-06
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-06    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BizNiceCiService extends AbstractService implements IBizNiceCiService {
    private final INiceCiService niceCiService;
    private final CmmEnsCacheService cacheService;
    private final IBizNiceCiMapper niceCiMapper;

    //--------------------------------------------------------------------------------
    // 기관용 Token
    //--------------------------------------------------------------------------------
    /**
     * <pre>
     * NICE 토큰 발급 요청
     * 만료일 이전(expired_in) 이전에 요청시 신규 발급되지 않는다
     * -> 발급이 필요한 경우, 먼저 토큰 폐기후 요청 해야
     * 1. 토큰발급 요청
     * 2. 토큰 정보 갱신
     * @return
     * </pre>
     */
    @Override
    @Transactional
    public NiceTokenResponse generateToken(final NiceCiRequest reqDTO){

        NiceTokenResponse tokenResponse = niceCiService.generateToken(reqDTO);
        ResponseDataHeader dataHeader = tokenResponse.getDataHeader();

        // token이 만료 및 폐기된 상태
        if(dataHeader.getGwRsltCd().equals("1800")){
            // 1. 토큰 폐기
            try {
                final TokenRevokeResponse revokeResDTO = revokeToken(reqDTO);

                if(!(revokeResDTO.getDataHeader().getGwRsltCd().equals("1200") && revokeResDTO.getDataBody().isResult())){
                    throw BizRuntimeException.create(JsonUtils.toJson(revokeResDTO.getDataHeader()));
                }
            } catch (BizRuntimeException be){
                throw be;
            } catch (Exception e){
                throw BizRuntimeException.create(e.getMessage());
            }

            // 토큰 폐기후 재발급
            tokenResponse = niceCiService.generateToken(reqDTO);
            dataHeader = tokenResponse.getDataHeader();
        }

        if(dataHeader.getGwRsltCd().equals("1200")){
            final TokenResDataBody dataBody = tokenResponse.getDataBody();
            final NiceCiInfo niceDTO = NiceCiInfo.builder()
                .signguCode(reqDTO.getSignguCode())
                .ffnlgCode(reqDTO.getFfnlgCode())
                .accessToken(dataBody.getAccessToken())
                .expiresIn(dataBody.getExpiresIn())
                .tokenType(dataBody.getTokenType())
                .scope(dataBody.getScope())
                .build();

            niceCiMapper.updateNiceCrtfToken(niceDTO);

            // 공개키 정보 캐쉬 삭제
            cacheService.removeNiceCiInfoCache(niceDTO.getSignguCode(), niceDTO.getFfnlgCode());

            updateNiceCerfInfoSync(niceDTO, reqDTO, NiceCiWrkDiv.TOKEN);
            //----------------------------------------------------------------------------------

        }
        return tokenResponse;
    }

    /**
     * <pre>
     * Authorization : Basic + Base64Encoding(access_token:current_timestamp:client_id)
     * - access_token : 만료할 access_token
     * - client_id : access_token발급에 사용된 client_id
     * - current_timestamp
     *   Date currentDate = new Date();
     *   long current_timestamp = currentDate.getTime() /1000
     * @return
     * </pre>
     */
    @Override
    @Transactional
    public TokenRevokeResponse revokeToken(final NiceCiRequest reqDTO){
        final TokenRevokeResponse resDTO = niceCiService.revokeToken(reqDTO);
        final ResponseDataHeader dataHeader = resDTO.getDataHeader();

        if(dataHeader.getGwRsltCd().equals("1200")){
            final TokenRevokeResDataBody dataBody = resDTO.getDataBody();

            if(dataBody.isResult()){
                final NiceCiInfo niceDTO = NiceCiInfo.builder()
                    .signguCode(reqDTO.getSignguCode())
                    .ffnlgCode(reqDTO.getFfnlgCode())
                    .accessToken(null)
                    .expiresIn(null)
                    .tokenType(null)
                    .scope(null)
                    .build();
                niceCiMapper.updateNiceCrtfToken(niceDTO);
                // 공개키 정보 캐쉬 삭제
                cacheService.removeNiceCiInfoCache(niceDTO.getSignguCode(), niceDTO.getFfnlgCode());

                updateNiceCerfInfoSync(niceDTO, reqDTO, NiceCiWrkDiv.TOKEN);
            }else{
                throw BizRuntimeException.create("토큰을 폐기하지 못했습니다.");
            }
        }
        return resDTO;
    }
    //--------------------------------------------------------------------------------

    /* create a function email validation  */



    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------
    /**
     * <pre>
     * 공개키 요청
     * 1. 공개키 요청
     * 2. 공개키 정보 update
     * 3. 공개키 정보 조회 캐쉬 삭제
     * @return PublickeyResponse
     * </pre>
     */
    @Override
    @Transactional
    public PublickeyResDataBody requestPublickey(final NiceCiRequest reqDTO) {
        final NiceCiInfo niceDTO = CmmNiceCiUtils.getNiceCiInfo(reqDTO);
        if(ObjectUtils.isEmpty(niceDTO.getAccessToken()))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.nice.token.info"));

        final PublickeyResDataBody dataBody = niceCiService.requestPublickey(reqDTO);

        // 3. 공개키 정보 update
        niceDTO.setSiteCode(dataBody.getSiteCode());
        niceDTO.setKeyVersion(dataBody.getKeyVersion());
        niceDTO.setPublicKey(dataBody.getPublicKey());
        niceDTO.setValidDtim(dataBody.getValidDtim());
        niceCiMapper.updateNiceCrtfPublickey(niceDTO);
        // 공개키 정보 캐쉬 삭제
        cacheService.removeNiceCiInfoCache(niceDTO.getSignguCode(), niceDTO.getFfnlgCode());

        updateNiceCerfInfoSync(niceDTO, reqDTO, NiceCiWrkDiv.PUBLIC_KEY);

        return dataBody;
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------
    /**
     * <pre>
     * 0. cache call
     *  --> 공개키 잔여일 수가 5보다 작으면
     * 1. 공개키 재발급후 공개키 정보 update
     * 2. cache 삭제 && cache call
     * @return
     * </pre>
     */
    @Override
    @Transactional
    public SymkeyStatInfo requestRegSymmetrickey(final NiceCiRequest reqDTO) {
        final SymkeyRegInfo symkeyRegInfo = CmmNiceCiUtils.getSymkeyRegInfo();
        final SymkeyStatInfo symkeyStatInfo = niceCiService.requestRegSymmetrickey(reqDTO, symkeyRegInfo);
        final NiceCiInfo niceDTO = NiceCiInfo.builder()
            .signguCode(reqDTO.getSignguCode())
            .ffnlgCode(reqDTO.getFfnlgCode())
            .build();

        niceDTO.setBefSymkeyVersion(symkeyStatInfo.getBefSymkeyVersion());
        niceDTO.setBefSymkeyValidDtim(symkeyStatInfo.getBefValidDtim());
        niceDTO.setBefSymkeyKey(niceDTO.getCurSymkeyKey());
        niceDTO.setBefSymkeyIv(niceDTO.getCurSymkeyIv());
        niceDTO.setBefSymkeyHmacKey(niceDTO.getCurSymkeyHmacKey());

        niceDTO.setCurSymkeyVersion(symkeyStatInfo.getCurSymkeyVersion());
        niceDTO.setCurSymkeyValidDtim(symkeyStatInfo.getCurValidDtim());
        niceDTO.setCurSymkeyKey(symkeyRegInfo.getKey());
        niceDTO.setCurSymkeyIv(symkeyRegInfo.getIv());
        niceDTO.setCurSymkeyHmacKey(symkeyRegInfo.getHmacKey());

        niceCiMapper.updateNiceCrtfSymkey(niceDTO);
        // 공개키 정보 캐쉬 삭제
        cacheService.removeNiceCiInfoCache(niceDTO.getSignguCode(), niceDTO.getFfnlgCode());

        updateNiceCerfInfoSync(niceDTO, reqDTO, NiceCiWrkDiv.SYM_KEY);

        return symkeyStatInfo;
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 아이핀 CI 요청
    //--------------------------------------------------------------------------------
    public IpinCiResDataBody requestCi(final NiceCiRequest reqDTO) {
        return niceCiService.requestCi(reqDTO);
    }
    //--------------------------------------------------------------------------------


    //--------------------------------------------------------------------------------

    /**
     * <pre>
     * 교통시설운영처와 승화원의 Nice CI 정보 sync
     * @param niceDTO NiceCiInfo
     * @param reqDTO NiceCiRequest
     * @param wrkDiv NiceCiWrkDiv
     * </pre>
     */
    private void updateNiceCerfInfoSync(final NiceCiInfo niceDTO, final NiceCiRequest reqDTO, final NiceCiWrkDiv wrkDiv) {
        // 교통시설운영처 또는 장사시설인 경우 sync ----------------------------------------
        if(SignguCode.TRAFFIC.getCode().equals(reqDTO.getSignguCode())){
            niceDTO.setSignguCode(SignguCode.FUNERAL.getCode());
        }

        if(SignguCode.FUNERAL.getCode().equals(reqDTO.getSignguCode())){
            niceDTO.setSignguCode(SignguCode.TRAFFIC.getCode());
        }

        switch(wrkDiv){
            case TOKEN -> niceCiMapper.updateNiceCrtfToken(niceDTO);
            case PUBLIC_KEY -> niceCiMapper.updateNiceCrtfPublickey(niceDTO);
            case SYM_KEY -> niceCiMapper.updateNiceCrtfSymkey(niceDTO);
            //default -> ;
        }

        // 공개키 정보 캐쉬 삭제
        cacheService.removeNiceCiInfoCache(niceDTO.getSignguCode(), niceDTO.getFfnlgCode());
        cacheService.logCache();
    }
}
