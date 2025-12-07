package marketplace.out.repository;

import marketplace.application.port.MetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MetaRepositoryJdbc implements MetaRepository {
    private final DataSource ds;
    private final String schema;

    public MetaRepositoryJdbc(DataSource ds, @Value("${spring.liquibase.db.appSchema}") String schema) {
        this.ds = ds;
        this.schema = schema;
    }

    public List<String> listCategories() {
        String sql = String.format("SELECT name FROM %s.categories ORDER BY name", schema);
        List<String> res = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(rs.getString("name"));
            return res;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void addCategory(String name) {
        String sql = String.format("INSERT INTO %s.categories (name) VALUES (?) ON CONFLICT DO NOTHING", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(0, name);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void removeCategory(String name) {
        String sql = String.format("DELETE FROM %s.categories WHERE name=?", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(0, name);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<String> listBrands() {
        String sql = String.format("SELECT name FROM %s.brands ORDER BY name", schema);
        List<String> res = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(rs.getString("name"));
            return res;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void addBrand(String name) {
        String sql = String.format("INSERT INTO %s.brands (name) VALUES (?) ON CONFLICT DO NOTHING", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(0, name);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void removeBrand(String name) {
        String sql = String.format("DELETE FROM %s.brands WHERE name=?", schema);
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(0, name);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
