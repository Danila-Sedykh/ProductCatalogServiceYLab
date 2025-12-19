package marketplace;


import org.example.annotation.EnableLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "marketplace")
@SpringBootApplication
@EnableLogging
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}