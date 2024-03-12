package kr.xit.core.spring.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>
 * description : Springdoc(swagger) 설정
 *               설정내용이 상이한 경우 동일한 파일로 재정의 하거나 상속받아 사용
 * packageName : kr.xit.core.spring.config
 * fileName    : SpringDocsConfig
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 */
@ConditionalOnProperty(value = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = false)
@Configuration
public class SpringDocsApiConfig {
    @Bean
    public GroupedOpenApi authentification() {
        return GroupedOpenApi.builder()
            .group("1. Common API")
            .pathsToMatch(
                "/api/auth/**",
                "/api/cmm/**"
            )
            .build();
    }

    @Bean
    public GroupedOpenApi kakaopayEltrcDoc() {
        return GroupedOpenApi.builder()
            .group("2. 문서중계자 API")
            .pathsToMatch(
                "/api/ens/**",
                "/api/ag/**"
            )
            .build();
    }

    @Bean
    public GroupedOpenApi bizDoc() {
        return GroupedOpenApi.builder()
            .group("6. 전자고지 업무 API")
            .pathsToMatch(
                "/api/biz/**"
            )
            .build();
    }

    @Bean
    public GroupedOpenApi otherDoc() {
        return GroupedOpenApi.builder()
                .group("9. 외부DB 연계 API")
                .pathsToMatch(
                        "/api/other/**"
                )
                .build();
    }
}
