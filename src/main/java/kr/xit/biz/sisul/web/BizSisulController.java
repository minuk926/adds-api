package kr.xit.biz.sisul.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.xit.biz.ens.model.cmm.CmmEnsFileInfDTO.FmcExcelUpload;
import kr.xit.biz.sisul.model.SisulSndngResultDTO.RsltSisulRequest;
import kr.xit.biz.sisul.service.IBizSisulService;
import kr.xit.core.model.ApiResponseDTO;
import kr.xit.core.model.IApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * description : 전자고지 시설관리 시스템 연계 관련 처리
 *
 * packageName : kr.xit.biz.sisul.web
 * fileName    : BizSisulController
 * author      : limju
 * date        : 2023-09-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-04    limju       최초 생성
 *
 * </pre>
 */
@Slf4j
@Tag(name = "BizSisulController", description = "전자고지 시설관리 시스템 연계 관련 처리")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/biz/sisul/v1")
public class BizSisulController {

    private final IBizSisulService service;

    /**
     * 시설관리공단(Facility Management Corporation) 전자고지 대상
     * @param fileReq
     * @return
     */
    @Operation(summary = "시설관리공단 전자고지 대상 엑셀업로드 처리", description = "시설관리공단 전자고지 대상 엑셀업로드 처리<br><a href='http://localhost:8082/fmcExcelUpload.html'>전자고지연계파일처리</a>")
    @PostMapping(value = "/fmcExcelUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse fmcExcelUpload(@ModelAttribute FmcExcelUpload fileReq) {
        String msg = service.fmcExcelUpload(fileReq);
        return ApiResponseDTO.success(fileReq, msg);
    }

    @Operation(summary = "발송결과정보 마스터 조회 - 시설공단 내부시스템에서 호출", description = "발송결과정보 마스터 조회 - 시설공단 내부시스템에서 호출")
    @PostMapping(value = "/sndng/result/master", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findSndngResultMaster(@RequestBody RsltSisulRequest reqDTO) {
        return ApiResponseDTO.success(service.findSndngResultMaster(reqDTO));
    }

    @Operation(summary = "발송결과정보 상세 조회 - 시설공단 내부시스템에서 호출", description = "발송결과정보 상세 조회 - 시설공단 내부시스템에서 호출")
    @PostMapping(value = "/sndng/result/details", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public IApiResponse findSndngResultDetails(@RequestBody RsltSisulRequest reqDTO) {
        return ApiResponseDTO.success(service.findSndngResultDetails(reqDTO));
    }
}
