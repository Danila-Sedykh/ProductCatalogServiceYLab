package marketplace.in.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import marketplace.application.AuditService;
import marketplace.application.ProductService;
import marketplace.aspect.AuditAspect;
import marketplace.config.*;
import marketplace.db.DataSourceFactory;
import marketplace.domain.Product;
import marketplace.dto.CreateProductRequest;
import marketplace.dto.ProductDto;
import marketplace.dto.UpdateProductRequest;
import marketplace.mapper.ProductMapper;
import marketplace.out.cache.LruCache;
import marketplace.out.repository.*;
import org.hibernate.validator.HibernateValidator;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {
        "/api/products",
        "/api/products/*"
})
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private AuditService auditService;
    private AuditAspect auditAspect;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Validator validator;

    @Override
    public void init() {
        var ds = DataSourceFactory.create(ConfigLoader.get("db.url"),
                ConfigLoader.get("db.username"),
                ConfigLoader.get("db.password"),
                Integer.parseInt(ConfigLoader.get("db.maximumPoolSize")));
        var productRepo = new ProductRepositoryJdbc(ds, ConfigLoader.get("db.appSchema"));
        var auditRepo = new AuditRepositoryJdbc(ds, ConfigLoader.get("db.appSchema"));
        var cache = new LruCache<String, List<Product>>(50);
        this.productService = new ProductService(productRepo, cache);
        this.auditService = new AuditService(auditRepo);
        this.auditAspect = new AuditAspect(auditService);

        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure().buildValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        auditAspect.setRequest(req);
        try {
            CreateProductRequest request = objectMapper.readValue(req.getInputStream(), CreateProductRequest.class);

            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(objectMapper.writeValueAsString(violations));
                return;
            }

            Product product = ProductMapper.INSTANCE.toEntity(request);
            productService.addProduct(product);

            ProductDto dto = ProductMapper.INSTANCE.toDto(product);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(objectMapper.writeValueAsString(dto));
        } finally {
            auditAspect.clearRequest();
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        auditAspect.setRequest(req);
        try {
            UpdateProductRequest request = objectMapper.readValue(req.getInputStream(), UpdateProductRequest.class);

            Product product = new Product();
            product.setId(request.getId());
            product.setCode(request.getCode());
            product.setName(request.getName());
            product.setCategory(request.getCategory());
            product.setBrand(request.getBrand());
            product.setPrice(request.getPrice().doubleValue());

            boolean updated = productService.updateProduct(product);
            if (updated) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } finally {
            auditAspect.clearRequest();
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } else {
            try {
                Long id = Long.parseLong(path.substring(1));
                var productOpt = productService.getById(id);
                if (productOpt.isPresent()) {
                    ProductDto dto = ProductMapper.INSTANCE.toDto(productOpt.get());
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(objectMapper.writeValueAsString(dto));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        auditAspect.setRequest(req);
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Long id = Long.parseLong(path.substring(1));
            boolean deleted = productService.deleteProduct(id);
            resp.setStatus(deleted ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_NOT_FOUND);
        } finally {
            auditAspect.clearRequest();
        }
    }
}