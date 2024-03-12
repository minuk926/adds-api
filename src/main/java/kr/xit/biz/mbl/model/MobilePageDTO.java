package kr.xit.biz.mbl.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.LocalDateTime;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <pre>
 * description : 전자문서 중개자 모바일 페이지 API DTO
 *
 * packageName : kr.xit.biz.mbl.model
 * fileName    : MobilePageDTO
 * author      : limju
 * date        : 2023-08-31
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-08-31    limju       최초 생성
 *
 * </pre>
 */
public class MobilePageDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    @EqualsAndHashCode(callSuper = false)
    public static class MobilePageManage extends CmmEnsRequestDTO {

        /**
         * 발송 상세 id
         */
        private String sndngDetailId;
        /**
         * 발송 구분 코드
         */
        private String sndngSeCode;
        /**
         * 모바일 페이지 내용
         */
        private String mobilePageCn;

        private String signguCode;

        private String ffnlgCode;
        private String profile;

        /**
         * 등록 일시
         */
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
        private LocalDateTime registDt;
        /**
         * 등록자
         */
        private String register;
    }
}
