package kr.xit.biz.cmm.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import kr.xit.biz.cmm.mapper.ICmmEnsCacheMapper;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.nice.NiceCiDTO.NiceCiInfo;
import kr.xit.core.spring.config.cache.CacheType;
import kr.xit.core.spring.config.cache.CachingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <pre>
 * description : 전자고지 Cache 서비스
 *               - cache: CaffeineCache use
 *
 * packageName : kr.xit.biz.cmm.service
 * fileName    : CmmEnsCacheService
 * author      : limju
 * date        : 2023-09-12
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-12    limju       최초 생성
 *
 * </pre>
 * @see CacheType
 * @see CachingConfig
 * @see org.springframework.cache.interceptor.SimpleKeyGenerator
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CmmEnsCacheService implements ICmmEnsCacheService {
    private final ICmmEnsCacheMapper cmmEnsMapper;
    private final CacheManager cacheManager;

    /**
     * Nice CI 인증 관련 정보 cache load
     * @param signguCode String
     * @param ffnlgCode String
     * @return NiceCiInfo
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "niceCiInfo", keyGenerator = "simpleKeyGenerator")
    public NiceCiInfo getNiceCiInfoCache(final String signguCode, final String ffnlgCode) {
        return cmmEnsMapper.selectNiceCiInfo(signguCode, ffnlgCode);
    }

    /**
     * Nice CI 인증 관련 정보 cache remove
     * @param signguCode String
     * @param ffnlgCode String
     */
    @Transactional(readOnly = true)
    @CacheEvict(cacheNames = "niceCiInfo", keyGenerator = "simpleKeyGenerator")
    public void removeNiceCiInfoCache(final String signguCode, final String ffnlgCode) {
    }

    /**
     * KT / KAKAO / postplus / epost 문서중개자 관련 정보 cache load
     * @param dto CmmEnsRequestDTO
     * @return CmmEnsRlaybsnmDTO
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "rlaybsnmInfo", keyGenerator = "simpleKeyGenerator")
    public CmmEnsRlaybsnmDTO getRlaybsnmInfoCache(final CmmEnsRequestDTO dto) {
        return cmmEnsMapper.selectEnsRlaybsnmInfo(dto);
    }

    /**
     * KT / KAKAO / postplus / epost 문서중개자 관련 정보 cache remove
     * @param dto CmmEnsRequestDTO
     */
    @Transactional(readOnly = true)
    @CacheEvict(cacheNames = "rlaybsnmInfo", keyGenerator = "simpleKeyGenerator")
    public void removeRlaybsnmInfoCache(final CmmEnsRequestDTO dto) {
        log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&{} cache remove &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&", dto);
    }

    /**
     * cache log : hit rate
     */
    public void logCache(){
        if(log.isDebugEnabled()) {
            for(String cacheName : cacheManager.getCacheNames()) {
                Cache cache = ((CaffeineCache) cacheManager.getCache(cacheName)).getNativeCache();

                for(Object key : cache.asMap().keySet()) {
                    //Object value = cache.getIfPresent(key);
                    log.info("key: {}", key);
                    //log.info("key: {} - value: {}", key, value.toString());
                }

                CacheStats stats = cache.stats();
                log.info("cache '{}' - stats : {}", cacheName, stats.toString());
            }
        }
    }
}
