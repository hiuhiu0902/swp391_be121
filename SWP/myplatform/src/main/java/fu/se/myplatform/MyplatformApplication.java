package fu.se.myplatform;

import fu.se.myplatform.entity.Account;
import fu.se.myplatform.enums.Role;
import fu.se.myplatform.repository.AuthenticationRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "MyPlatform API", version = "1.0", description = "API for MyPlatform application"))
@SecurityScheme(name = "api",scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)public class MyplatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyplatformApplication.class, args);
    }
//    @Component
//    public class AdminAccountInitializer implements CommandLineRunner {
//        @Autowired
//        AuthenticationRepository authenticationRepository;
//        @Autowired
//        PasswordEncoder passwordEncoder;
//
//        @Override
//        public void run(String... args) {
//            if (authenticationRepository.findAccountByUserName("admin") == null) {
//                Account admin = new Account();
//                admin.setUserName("admin");
//                admin.setPassword(passwordEncoder.encode("admin123")); // Đổi mật khẩu cho an toàn
//                admin.setRole(Role.ADMIN);
//                admin.setEmail("admin@example.com");
//                admin.setFullName("Super Admin");
//                authenticationRepository.save(admin);
//                System.out.println("Admin account created!");
//            }
//        }
//    }
}
