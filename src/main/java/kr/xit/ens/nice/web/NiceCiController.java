package kr.xit.ens.nice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.ens.nice.cmm.CmmNiceCiUtils;
import kr.xit.ens.nice.service.INiceCiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.nice.web
 * fileName    : NiceCiController
 * author      : limju
 * date        : 2023-09-06
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-06    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "NiceCiController", description = "Nice CI API")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/ens/nice/v1")
public class NiceCiController {
    private static final String PARAM1 = """
                        {
                          "signguCode": "88328",
                          "ffnlgCode": "11",
                          "juminId": "9901011263512"
                        }
                    """;
    private static final String PARAM2 = """
                        {
                          "signguCode": "88316",
                          "ffnlgCode": "11",
                          "juminId": "9901011263512"
                        }
                    """;

    private final INiceCiService service;

    //--------------------------------------------------------------------------------
    // 기관용 Token
    //--------------------------------------------------------------------------------
    @Operation(deprecated = true, summary = "기관용 토큰 발급 요청 -> 업무(Biz)단 API 사용", description = "NICE 정보 DB update가 필요하여 업무단의 API를 사용하여야 함")
    @PostMapping(value = "/generateToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse generateToken(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.generateToken(reqDTO));
    }

    @Operation(deprecated = true, summary = "기관용 토큰 폐기 -> 업무(Biz)단 API 사용", description = "NICE 정보 DB update가 필요하여 업무단의 API를 사용하여야 함")
    @PostMapping(value = "/revokeToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse revokeToken(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.revokeToken(reqDTO));
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------
    /**
     *
     * @return
     */
    @Operation(deprecated = true, summary = "공개키 요청 -> 업무(Biz)단 API 사용", description = "NICE 정보 DB update가 필요하여 업무단의 API를 사용하여야 함")
    @PostMapping(value = "/requestPublickey", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestPublickey(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.requestPublickey(reqDTO));
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
    //--------------------------------------------------------------------------------
    @Operation(deprecated = true, summary = "대칭키(symmetrickey) 등록 요청 -> 업무(Biz)단 API 사용", description = "NICE 정보 DB update가 필요하여 업무단의 API를 사용하여야 함")
    @PostMapping(value = "/requestRegSymmetrickey", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestRegSymmetrickey(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.requestRegSymmetrickey(reqDTO, CmmNiceCiUtils.getSymkeyRegInfo()));
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 아이핀 CI 요청
    //--------------------------------------------------------------------------------
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
    @Operation(summary = "아이핀 CI 요청", description = "아이핀 CI 요청")
    @PostMapping(value = "/requestCi", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestCi(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.requestCi(reqDTO));
    }
    //--------------------------------------------------------------------------------
}
