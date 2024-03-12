package kr.xit.other.service;

import kr.xit.biz.sisul.mapper.IBizSisulMapper;
import kr.xit.core.service.AbstractService;
import kr.xit.core.spring.annotation.TraceLogging;
import kr.xit.core.support.utils.Checks;
import kr.xit.other.mapper.IOtherMapper;
import kr.xit.other.model.ElecnoticeDTO.Elecnoticedtl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 * description :
 *
 * packageName : kr.xit.other.service
 * fileName    : OtherService
 * author      : seojh
 * date        : 2024-01-04
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2024-01-04    seojh       최초 생성
 *
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OtherService extends AbstractService implements IOtherService {

    private final IOtherMapper mapper;
    private final IBizSisulMapper mapper2;

    /**
     *
     * @return String
     */
    @Override
    public String mergeData(){
        List<Elecnoticedtl> ohterResult = mapper2.selectOhterResult();

        for(Elecnoticedtl dto: ohterResult){
            Elecnoticedtl elecnoticedtl = mapper.selectElecnotice(dto);

            if((Checks.isEmpty(elecnoticedtl))
                    || (Checks.isEmpty(elecnoticedtl.getSndngprocesssttus()))
                    || (Checks.isNotEmpty(elecnoticedtl) && !Checks.checkVal(dto.getSndngprocesssttus(),"").equals(Checks.checkVal(elecnoticedtl.getSndngprocesssttus(),"")))
            ){
                mapper.saveElecnoticemst(dto);
                mapper.saveElecnoticedtl(dto);
            } else if ((Checks.isEmpty(elecnoticedtl.getResultcode())) || (!Checks.checkVal(dto.getResultcode(),""). equals(Checks.checkVal(elecnoticedtl.getResultcode(),"")))) {
                mapper.saveElecnoticedtl(dto);
            }
        }

        return "success";
    }
}
