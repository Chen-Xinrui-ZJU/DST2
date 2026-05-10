package cn.edu.zju.dao;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(FavoriteDao.class);

    public void addFavorite(int userId, String resourceType, String resourceId) {
        DBUtils.execSQL(connection -> {
            try {
                String sql = "INSERT IGNORE INTO favorites (user_id, resource_type, resource_id) VALUES (?, ?, ?)";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, resourceType);
                ps.setString(3, resourceId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public void removeFavorite(int userId, String resourceType, String resourceId) {
        DBUtils.execSQL(connection -> {
            try {
                String sql = "DELETE FROM favorites WHERE user_id = ? AND resource_type = ? AND resource_id = ?";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, resourceType);
                ps.setString(3, resourceId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public Set<String> findFavoriteResourceIds(int userId, String resourceType) {
        Set<String> ids = new HashSet<String>();

        DBUtils.execSQL(connection -> {
            try {
                String sql = "SELECT resource_id FROM favorites WHERE user_id = ? AND resource_type = ?";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, resourceType);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    ids.add(rs.getString("resource_id"));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return ids;
    }

    public List<Drug> findFavoriteDrugsByUserId(int userId) {
        List<Drug> drugs = new ArrayList<Drug>();

        DBUtils.execSQL(connection -> {
            try {
                String sql =
                        "SELECT d.id, d.name, d.obj_cls, d.drug_url, d.biomarker " +
                                "FROM favorites f " +
                                "JOIN drug d ON f.resource_id = d.id " +
                                "WHERE f.user_id = ? AND f.resource_type = 'drug' " +
                                "ORDER BY f.created_at DESC";

                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Drug drug = new Drug(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getBoolean("biomarker"),
                            rs.getString("drug_url"),
                            rs.getString("obj_cls")
                    );
                    drug.setFavorited(true);
                    drugs.add(drug);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugs;
    }
}
