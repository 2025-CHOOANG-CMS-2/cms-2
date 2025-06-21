package kr.ac.dhuniv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Spring 설정 클래스임을 나타냅니다.
public class WebConfig implements WebMvcConfigurer { // 웹 MVC 설정을 커스터마이징하기 위한 인터페이스를 구현합니다.

    // ⭐ 이미지 파일이 저장될 실제 파일 시스템 경로를 정의합니다. ⭐
    // System.getProperty("user.dir")는 애플리케이션이 실행되는 현재 작업 디렉토리(프로젝트 루트 등)를 반환합니다.
    // 이 경로 아래에 'uploads/' 폴더가 생성되고 그 안에 이미지가 저장됩니다.
    private final String uploadPath = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ⭐ 리소스 핸들러를 추가하여 웹 요청 경로와 실제 파일 시스템 경로를 매핑합니다. ⭐
        registry.addResourceHandler("/uploads/**") // 웹에서 '/uploads/'로 시작하는 모든 요청을 이 핸들러가 처리합니다.
                .addResourceLocations("file:" + uploadPath); // 'file:' 접두사를 사용하여 실제 파일 시스템 경로임을 명시합니다.
        
        // 디버깅을 위해 설정된 경로를 콘솔에 출력합니다.
        System.out.println("DEBUG: Static resource handler added for /uploads/** mapping to " + "file:" + uploadPath);
    }
}
