package kr.xit.core.biz.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.core.biz.model
 * fileName    : LoggingDTO
 * author      : limju
 * date        : 2023-05-11
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-05-11    limju       최초 생성
 *
 * </pre>
 */
@Schema(name = "LoggingDTO", description = "API 로깅 DTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LoggingDTO {
    /**
     * 요청 ID
     */
    private String requestId;
    /**
     * 시스템 ID(ENS|FIMS등)
     */
    private String systemId;
    /**
     * 요청시스템 ID(KAKAO|KT등)
     */
    private String reqSystemId;
    /**
     * 메소드(GET|PUT|POST|DELETE)
     */
    private String method;
    /**
     * 호출 URI
     */
    private String uri;
    /**
     * 성공/실패(true|false)
     */
    private String success;
    /**
     * 파라메터
     */
    private String param;
    /**
     * 호출 결과
     */
    private String response;
    /**
     * 메세지(에러메세지)
     */
    private String message;
    /**
     * IP
     */
    private String ip;
    /**
     * 토큰
     */
    private String accessToken;
    /**
     * 세션ID
     */
    private String sessionId;
    /**
     * 변경일시(now(3)-밀리세컨드까지)
     */
    //private java.sql.Timestamp updtDt;
    /**
     * 변경자
     */
    private String updtId;
    /**
     * 생성일시(now(3)-밀리세컨드까지)
     */
    //private java.sql.Timestamp registDt;
    /**
     * 생성자
     */
    //@Convert(converter = Jsr310.LocalDateTimeConverter.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss.SSS")
    private String registId;
}
