package kr.xit.biz.kt.service;


import kr.xit.biz.ens.model.kt.KtCommonDTO.KtCommonResponse;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.ens.model.kt.KtMmsSendDTO.KtMsgRsltRequest;
import kr.xit.biz.ens.model.kt.KtTokenDTO.KtTokenResponse;


/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.kt.service
 * fileName    : IBizKtMmsService
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
public interface IBizKtMmsService {

    KtTokenResponse requestToken(final KtMnsRequest paramDTO);
//    KtCommonResponse mainSend(final KtMnsRequest reqDTO);
    KtCommonResponse messageResult(final KtMsgRsltRequest reqDTO);

//    KtCommonResponse beforeSend(final KtBefSendRequest reqDTO);
//    KtCommonResponse mainSend(final KtMainSendRequest reqDTO);
//    KtCommonResponse blacklist(final KtBlacklistRequest reqDTO);
//    KtCommonResponse cfmToken(final KtTokenConfirmRequest reqDTO);
//    KtCommonResponse readToken(final KtTokenReadRequest reqDTO);
//    KtCommonResponse messageResult(final KtMsgRsltRequest reqDTO);
//    KtSendSttcResponse sendSttc(final KtSendSttcRequest reqDTO);
//    KtSendSttcDtlResponse sendSttcDtl(final KtSendSttcDtlRequest reqDTO);
//    KtCommonResponse whitelist(final KtWhitelistRequest reqDTO);
//
//    KtCommonResponse refuseRcv(final KtRefuseRcvRequest reqDTO);
//    KtCommonResponse approveRcv(final KtApproveRcvRequest reqDTO);
//
//    KtAcmdCerfResponse cerfAcmd(final KtAcmdCerfRequest reqDTO);
//
//    KtCommonResponse excaAcmd(final KtTokenExcaRequest reqDTO);
//    KtExcaResponse exca(final KtExcaRequest reqDTO);
//    KtAcmdInfoResponse infoAcmd(final KtAcmdInfoRequest reqDTO);
//    KtAcmdInfoCfmResponse cfmAcmd(final KtAcmdInfoCfmRequest reqDTO);
}
