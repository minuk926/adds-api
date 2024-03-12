package kr.xit.core.spring.util;

import kr.xit.biz.cmm.service.CmmEnsCacheService;
import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.kt.service.IBizKtMmsService;
import kr.xit.biz.nice.service.IBizNiceCiService;
import kr.xit.core.spring.config.support.ApplicationContextProvider;
import kr.xit.ens.nice.service.INiceCiService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;

/**
 * <pre>
 * description : Get Bean Object
 *               Filter / Interceptor 등에서 Bean 사용시 필요
 *               (Bean으로 등록되는 클래스 내에서만 @Autowired / @Resource 등이 동작)
 * packageName : kr.xit.core.spring.util
 * fileName    : SpringUtils
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 * @see ApplicationContextProvider
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiSpringUtils {

	public static ApplicationContext getApplicationContext() {
		return ApplicationContextProvider.getApplicationContext();
	}
	
	public static boolean containsBean(String beanName) {
		return getApplicationContext().containsBean(beanName);
	}
	
	public static Object getBean(String beanName) {
		return getApplicationContext().getBean(beanName);
	}
	
	public static Object getBean(Class<?> clazz) {
		return getApplicationContext().getBean(clazz);
	}

	public static ICmmEnsCacheService getCmmEnsCacheService(){
		return (ICmmEnsCacheService)getBean(CmmEnsCacheService.class);
	}

	public static INiceCiService getNiceCiService(){
		return (INiceCiService)getBean(INiceCiService.class);
	}

	public static IBizNiceCiService getBizNiceCiService(){
		return (IBizNiceCiService)getBean(IBizNiceCiService.class);
	}

	public static IBizKtMmsService getBizKtMmsService(){
		return (IBizKtMmsService)getBean(IBizKtMmsService.class);
	}
}
