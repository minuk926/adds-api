package kr.xit.ens.pplus.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.cmm.SndngMssageParam;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpCommonResponse;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusRequest;
import kr.xit.biz.ens.model.pplus.PplusDTO.PpStatusResponse;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import kr.xit.ens.pplus.service.IPplusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.ens.pplus.web
 * fileName    : PplusController
 * author      : limju
 * date        : 2023-10-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-10-04    limju       최초 생성
 *
 * </pre>
 */
@Tag(name = "PplusController", description = "Postplus(포스토피아) API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ens/pplus/v1")
public class PplusController {
    private final IPplusService service;

    @Operation(summary = "우편제작접수", description = "우편제작접수 요청<br><a href='/pstFile.html'>우편제작접수</a>")
    @PostMapping(value = "/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse send(@ModelAttribute final SndngMssageParam paramDTO) {
        PpCommonResponse resDTO = service.sendBulks(paramDTO);
        return ApiResponseDTO.success(resDTO);
    }

    @Operation(summary = "우편제작 상태 조회", description = "우편제작상태조회")
    @PostMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse status(@RequestBody final PpStatusRequest paramDTO) {
        PpStatusResponse resDTO = service.statusBulks(paramDTO);
        return ApiResponseDTO.success(resDTO);
    }
}
