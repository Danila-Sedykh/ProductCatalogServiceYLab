package marketplace.out.repository;

import marketplace.application.port.ProductRepository;
import marketplace.domain.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryJdbc implements ProductRepository {
    private final DataSource ds;
    private final String schema;

    public ProductRepositoryJdbc(DataSource ds, @Value("${spring.liquibase.db.appSchema}") String schema) {
        this.ds = ds;
        this.schema = schema;
    }

    public Product save(Product p) {
        String insert = String.format(
                "INSERT INTO %s.products (id, code, name, category, brand, price) VALUES (nextval('%s.product_seq'), ?, ?, ?, ?, ?) RETURNING id",
                schema, schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setLong(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getBrand());
            ps.setBigDecimal(5, p.getPrice());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    p.setId(id);
                }
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(Product p) {
        String sql = String.format("UPDATE %s.products SET code=?, name=?, category=?, brand=?, price=? WHERE id=?", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getCategory());
            ps.setString(4, p.getBrand());
            ps.setBigDecimal(5, p.getPrice());
            ps.setLong(6, p.getCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(long id) {
        String sql = String.format("DELETE FROM %s.products WHERE id=?", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Product> findById(long id) {
        String sql = String.format("SELECT id, code, name, category, brand, price FROM %s.products WHERE id=?", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product p = mapRow(rs);
                    return Optional.of(p);
                }
            }
            return Optional.empty();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<Product> findAll() {
        String sql = String.format("SELECT id, code, name, category, brand, price FROM %s.products", schema);
        List<Product> res = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(mapRow(rs));
            return res;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setCode(rs.getLong("code"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setBrand(rs.getString("brand"));
        p.setPrice(rs.getBigDecimal("price"));
        return p;
    }
}
