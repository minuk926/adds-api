package kr.xit.other.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.other.service.IOtherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.other.web
 * fileName    : OtherController
 * author      : jhseo
 * date        : 2024-01-09
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2024-01-09    jhseo       최초 생성
 *
 * </pre>
 */
@Tag(name = "OtherController", description = "외부 Oracle 연계 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/other/v1")
public class OtherController {
    private final IOtherService service;

    @Operation(summary = "외부 Oracle 연계 요청", description = "외부 Oracle 연계 요청")
    @PostMapping(value = "/merge", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse merge() {
        String msg = service.mergeData();
        String errMsg = "";
        if("success".equals(msg)) errMsg = service.mergeData();
        return ApiResponseDTO.success(msg, errMsg);
    }
}
