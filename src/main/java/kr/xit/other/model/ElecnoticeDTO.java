package kr.xit.other.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <pre>
 * description : 전자고지 파일 외부연계 DTO
 *
 * packageName : kr.xit.biz.other.model
 * fileName    : ElecnoticeDTO
 * author      : seojh
 * date        : 2024-01-03
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2024-01-03    seojh       최초 생성
 *
 * </pre>
 */
public class ElecnoticeDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class Elecnoticemst {

        /**
         * 파일 일련번호
         */
        private String unitysndngmastrid;
        /**
         * 우편물 발송건수
         */
        private String sndngco;
        /**
         * 발송처리상태
         */
        private String sndngprocesssttus;
        /**
         * 등록자
         */
        private String insuser;
        /**
         * 등록일자
         */
        private String insdate;
        /**
         * 수정자
         */
        private String upduser;
        /**
         * 수정일자
         */
        private String upddate;
        /**
         * 발송일시
         */
        private String sndngdt;
        /**
         * 발송구분
         */
        private String sndngsecode;
        /**
         * 고지서명
         */
        private String tmplatid;
        /**
         * 결과조회일시
         */
        private String searchdate;
        /**
         * 성공여부
         */
        private String success;
        /**
         * 메시지
         */
        private String message;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class Elecnoticedtl extends Elecnoticemst {
        /**
         * 우편물 일련번호
         */
        private String unitysndngdetailid;
        /**
         * 고지차수
         */
        private String gojidepth;
        /**
         * 기관번호
         */
        private String taxnum1;
        /**
         * 세목
         */
        private String taxnum2;
        /**
         * 납세년월기
         */
        private String taxnum3;
        /**
         * 과세번호
         */
        private String taxnum4;
        /**
         * 담당자
         */
        private String worker;
        /**
         * 가상계좌 일련번호
         */
        private String serialno;
        /**
         * 수신일자
         */
        private String recvdt;
        /**
         * 열람일자
         */
        private String readdt;
        /**
         * 발송결과코드
         */
        private String resultcode;

    }

}