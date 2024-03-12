package kr.xit.biz.kt.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.kt.service.IBizKtMmsService;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.biz.kt.web
 * fileName    : BizKtMmsController
 * author      : limju
 * date        : 2023-09-22
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-22    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "BizKtMmsController", description = "KT MMS 업무처리 Controller")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/biz/kt/v1")
@Slf4j
public class BizKtMmsController {
    private static final String PARAM1 = """
                        {
                          "signguCode": "88328",
                          "ffnlgCode": "11",
                          "profile": "local"
                        }
                    """;
    private static final String PARAM2 = """
                        {
                          "signguCode": "88316",
                          "ffnlgCode": "11",
                          "profile": "local"
                        }
                    """;

    private final IBizKtMmsService service;

    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
        @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "교통시설운영처",
                    value = PARAM1),
                @ExampleObject(
                    name = "승화원",
                    value = PARAM2)
            })
    })
    @Operation(deprecated = true, summary = "기관용 토큰 발급 요청", description = "기관용 토큰 발급 요청")
    @PostMapping(value = "/requestToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestToken(@RequestBody final KtMnsRequest paramDTO) {
        return ApiResponseDTO.success(service.requestToken(paramDTO));
    }
}
