package marketplace.application.initializ;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import marketplace.config.AopConfig;
import marketplace.config.AppConfig;
import marketplace.config.DataConfig;
import marketplace.config.SwaggerConfig;
import marketplace.config.WebConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(AppConfig.class, DataConfig.class, WebConfig.class, AopConfig.class, SwaggerConfig.class);

        DispatcherServlet servlet = new DispatcherServlet(ctx);
        ServletRegistration.Dynamic reg = servletContext.addServlet("dispatcher", servlet);
        reg.setLoadOnStartup(1);
        reg.addMapping("/");
    }
}
