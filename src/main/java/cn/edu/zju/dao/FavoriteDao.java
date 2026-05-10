package cn.edu.zju.dao;

import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class FavoriteDao {

    private static final Logger log = LoggerFactory.getLogger(FavoriteDao.class);

    public void addFavorite(String userId, String resourceType, String resourceId) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert ignore into favorites (user_id, resource_type, resource_id) values (?, ?, ?)"
                );

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, resourceType);
                preparedStatement.setString(3, resourceId);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public void removeFavorite(String userId, String resourceType, String resourceId) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "delete from favorites where user_id = ? and resource_type = ? and resource_id = ?"
                );

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, resourceType);
                preparedStatement.setString(3, resourceId);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public Set<String> findFavoriteResourceIds(String userId, String resourceType) {
        Set<String> ids = new HashSet<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select resource_id from favorites where user_id = ? and resource_type = ?"
                );

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, resourceType);

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    ids.add(resultSet.getString("resource_id"));
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return ids;
    }
}