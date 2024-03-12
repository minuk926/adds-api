package kr.xit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

import kr.xit.core.spring.config.support.CustomBeanNameGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * <pre>
 * description : ens API application main
 *               ServletComponentScan
 *               - 서블릿컴포넌트(필터, 서블릿, 리스너)를 스캔해서 빈으로 등록
 *               - WebFilter, WebServlet, WebListener annotaion sacan
 *               - SpringBoot의 내장톰캣을 사용하는 경우에만 동작
 *               ConfigurationPropertiesScan
 *               - ConfigurationProperties annotaion class scan 등록
 *               - EnableConfigurationProperties 대체
 * packageName : kr.xit
 * fileName    : EnsApiApplication
 * author      : julim
 * date        : 2023-04-28
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-04-28    julim       최초 생성
 *
 * </pre>
 */
@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"egovframework", "kr.xit"})
@ServletComponentScan
@ComponentScan(
	nameGenerator = CustomBeanNameGenerator.class,
	basePackages = {"egovframework", "kr.xit"}
)
public class EnsApiApplication {
	static final List<String> basePackages = new ArrayList<>(
		Arrays.asList("egovframework", "kr.xit")
	);

	public static void main(String[] args) {
		final String line = "====================================================================";
		log.info(line);
		log.info("====    EnsApiApplication start :: active profiles - {}    ====", System.getProperty("spring.profiles.active"));
		if(Objects.isNull(System.getProperty("spring.profiles.active"))) {

			log.error(">>>>>>>>>>>>>>        Undefined start VM option       <<<<<<<<<<<<<<");
			log.error(">>>>>>>>>>>>>> -Dspring.profiles.active=local|dev|prd <<<<<<<<<<<<<<");
			log.error("============== EnsApiApplication start fail ===============");
			log.error(line);
			System.exit(-1);
		}
		log.info(line);

		// beanName Generator 등록 : API v1, v2 등으로 분류하는 경우
		// Bean 이름 식별시 풀패키지 명으로 식별 하도록 함
		final CustomBeanNameGenerator beanNameGenerator = new CustomBeanNameGenerator();
		beanNameGenerator.addBasePackages(basePackages);

		final SpringApplicationBuilder applicationBuilder = new SpringApplicationBuilder(EnsApiApplication.class);
		applicationBuilder.beanNameGenerator(beanNameGenerator);

		final SpringApplication application = applicationBuilder.build();
		application.setBannerMode(Banner.Mode.OFF);
		application.setLogStartupInfo(false);

		//TODO : 이벤트 실행 시점이 Application context 실행 이전인 경우 리스너 추가
		//PID(Process ID 작성)
		application.addListeners(new ApplicationPidFileWriter()) ;
		application.run(args);

		log.info("=========================================================================================");
		log.info("==========      EnsApiApplication load Complete :: active profiles - {}     ==========", System.getProperty("spring.profiles.active"));
		log.info("=========================================================================================");
	}
}
