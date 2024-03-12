package kr.xit.ens.epost.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.epost.EPostDTO.EpostTraceRequest;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.ens.epost.service.IEpostService;
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
 * packageName : kr.xit.ens.epost.web
 * fileName    : EpostController
 * author      : limju
 * date        : 2023-10-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-04    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "EpostController", description = "EPost API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ens/epost/v1")
public class EpostController {
    private final IEpostService service;

    @Operation(summary = "우편물 종적추적", description = "우편물 종적 추적")
    @PostMapping(value = "/trace", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse postTrackInfo(@RequestBody final EpostTraceRequest paramDTO) {
        return ApiResponseDTO.success(service.postTrackInfo(paramDTO));
    }
}
