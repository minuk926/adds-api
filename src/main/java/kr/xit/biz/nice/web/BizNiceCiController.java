package kr.xit.biz.nice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiRequest;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceTokenResponse;
import kr.xit.biz.ens.model.nice.NiceCiDTO.ResponseDataHeader;
import kr.xit.biz.ens.model.nice.NiceCiDTO.TokenRevokeResponse;
import kr.xit.biz.nice.service.IBizNiceCiService;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description : Nice CI 업무 처리 controller
 *
 * packageName : kr.xit.biz.nice.web
 * fileName    : BizNiceCiController
 * author      : limju
 * date        : 2023-09-06
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-06    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "BizNiceCiController", description = "Nice CI 업무처리 controller")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/biz/nice/v1")
public class BizNiceCiController {
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
    private final IBizNiceCiService service;

    //--------------------------------------------------------------------------------
    // 기관용 Token
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
    @Operation(summary = "기관용 토큰 발급 요청", description = "기관용 토큰 발급 요청")
    @PostMapping(value = "/generateToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse generateToken(@RequestBody final NiceCiRequest reqDTO) {
        final NiceTokenResponse tokenResponse = service.generateToken(reqDTO);
        return niceTokenResponse(tokenResponse.getDataHeader(), tokenResponse.getDataBody());
    }

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
    @Operation(summary = "기관용 토큰 폐기", description = "기관용 토큰 폐기")
    @PostMapping(value = "/revokeToken", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse revokeToken(@RequestBody final NiceCiRequest reqDTO) {
        final TokenRevokeResponse tokenRevokeResponse = service.revokeToken(reqDTO);
        return niceTokenResponse(tokenRevokeResponse.getDataHeader(), tokenRevokeResponse.getDataBody());
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 공개키(Publickey)
    //--------------------------------------------------------------------------------
    /**
     *
     * @param reqDTO
     * @return
     */
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
    @Operation(summary = "공개키 요청", description = "공개키 요청")
    @PostMapping(value = "/requestPublickey", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestPublickey(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.requestPublickey(reqDTO));
    }
    //--------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------
    // 대칭키 : symmetrickey
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
    @Operation(summary = "대칭키(symmetrickey) 등록 요청", description = "대칭키(symmetrickey) 등록 요청")
    @PostMapping(value = "/requestRegSymmetrickey", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse requestRegSymmetrickey(@RequestBody final NiceCiRequest reqDTO) {
        return ApiResponseDTO.success(service.requestRegSymmetrickey(reqDTO));
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


    //--------------------------------------------------------------------------------
    private  <T> ApiResponseDTO<T> niceTokenResponse(final ResponseDataHeader resHeader, final T t){
        if(resHeader.getGwRsltCd().equals("1200")) {
            return ApiResponseDTO.success(t);
        }
        return ApiResponseDTO.error(resHeader.getGwRsltCd(), resHeader.getGwRsltMsg());
    }
}
