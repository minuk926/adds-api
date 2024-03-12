package kr.xit.ens.kt.service;


import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdCerfResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoCfmResponse;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoRequest;
import kr.xit.biz.ens.model.kt.KtAcmdDTO.KtAcmdInfoResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaRequest;
import kr.xit.biz.ens.model.kt.KtExcaDTO.KtExcaResponse;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtApproveRcvRequest;
import kr.xit.biz.ens.model.kt.KtInputDTO.KtRefuseRcvRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtBlacklistRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcDtlRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcDtlResponse;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcRequest;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtSendSttcResponse;
import kr.xit.biz.ens.model.kt.KtMmsDTO.KtWhitelistRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtBefSendRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMainSendRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenConfirmResponse;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenExcaRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenReadRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenResponse;


/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.kt.service
 * fileName    : IKtMmsService
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
public interface IKtMmsService {
    //------------------------------------------------------------------------------
    // mens 사용 API
    //------------------------------------------------------------------------------
    KtTokenResponse requestToken(final KtMnsRequest paramDTO);
    KtCommonResponse mainSend(final KtMainSendRequest reqDTO);
    KtTokenConfirmResponse cfmToken(final KtTokenConfirmRequest reqDTO);
    KtCommonResponse readToken(final KtTokenReadRequest reqDTO);
    //------------------------------------------------------------------------------


    //------------------------------------------------------------------------------
    // mens 미사용 API
    //------------------------------------------------------------------------------

    KtCommonResponse beforeSend(final KtBefSendRequest reqDTO);
    KtCommonResponse blacklist(final KtBlacklistRequest reqDTO);
    KtSendSttcResponse sendSttc(final KtSendSttcRequest reqDTO);
    KtSendSttcDtlResponse sendSttcDtl(final KtSendSttcDtlRequest reqDTO);
    KtCommonResponse whitelist(final KtWhitelistRequest reqDTO);

    KtCommonResponse refuseRcv(final KtRefuseRcvRequest reqDTO);
    KtCommonResponse approveRcv(final KtApproveRcvRequest reqDTO);

    KtAcmdCerfResponse cerfAcmd(final KtAcmdCerfRequest reqDTO);

    KtCommonResponse excaAcmd(final KtTokenExcaRequest reqDTO);
    KtExcaResponse exca(final KtExcaRequest reqDTO);
    KtAcmdInfoResponse infoAcmd(final KtAcmdInfoRequest reqDTO);
    KtAcmdInfoCfmResponse cfmAcmd(final KtAcmdInfoCfmRequest reqDTO);
    //------------------------------------------------------------------------------
}
