package kr.xit.biz.sisul.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.sisul.model
 * fileName    : SisulSndngResultDTO
 * author      : limju
 * date        : 2023-11-02
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-11-02    limju       최초 생성
 *
 * </pre>
 */
public class SisulSndngResultDTO {

    @Schema(name = "RsltSisulRequest", description = "발송결과정보 조회 request DTO")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RsltSisulRequest {
        /**
         * 통합발송마스터 ID - 파일유일키
         */
        @Schema(requiredMode = RequiredMode.REQUIRED, title = "파일유일키", example = "DPMKK271000000777777")
        @Size(min = 1, max = 20, message = "파일유일키는 필수 입니다(max:20)")
        private String unitySndngMastrId;

        /**
         * 통합발송상세 ID - 우편물 일련 번호
         */
        @Schema(requiredMode = RequiredMode.AUTO, title = "우편물 일련번호", example = " ")
        @Size(max = 20, message = "우편물 일련번호는 20자를 넘을 수 없습니다.")
        private String unitySndngDetailId;
    }

    @Schema(name = "RsltSisulResMstData", description = "발송결과정보 master 조회 response DTO")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RsltSisulResMstData {

        /**
         * 통합발송마스터 ID - 파일유일키
         */
        private String unitySndngMastrId;

        /**
         * 발송구분코드 - KKO-MY-DOC|KT-BC|POST-PLUS
         */
        private String sndngSeCode;

        /**
         * 템플릿ID
         */
        private String tmplatId;

        /**
         * 발송건수
         */
        private Integer sndngCo;

        /**
         * 발송처리 상태
         */
        private String sndngProcessSttus;

        /**
         * 발송일시 : yyyyMMddHHmiss
         */
        private String sndngDt;
    }

    @Schema(name = "RsltSisulResDtlData", description = "발송결과정보 details 조회 response DTO")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RsltSisulResDtlData {
        /**
         * 통합발송마스터 ID - 파일유일키
         */
        private String unitySndngMastrId;

        /**
         * 통합발송상세 ID - 우편물 일련 번호
         */
        private String unitySndngDetailId;

        /**
         * 발송구분코드 - KKO-MY-DOC|KT-BC|POST-PLUS
         */
        private String sndngSeCode;

        /**
         * 템플릿ID
         */
        private String tmplatId;

        /**
         * 발송일시 : yyyyMMddHHmiss
         */
        @JsonProperty("sndngDt")
        private String requstDt;

        /**
         * 수신일시 : yyyyMMddHHmiss
         * recvDt 로 전송
         */
        @JsonProperty("recvDt")
        private String inqireDt;

        /**
         * 열람일시 : yyyyMMddHHmiss
         * readDt 로 전송
         */
        @JsonProperty("readDt")
        private String readngDt;

        /**
         * 등기번호
         */
        private String rgistno;

        /**
         * 발송결과 상태 - resultCode 로 전송
         */
        @JsonProperty("resultCode")
        private String sndngResultSttus;
    }
}
