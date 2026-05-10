package cn.edu.zju.dao;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrugDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DrugDao.class);

    public boolean existsById(String id) {
        return super.existsById(id, "drug");
    }

    public void saveDrug(Drug drug) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into drug (id, name, obj_cls, biomarker, drug_url) values (?,?,?,?,?)"
                );

                preparedStatement.setString(1, drug.getId());
                preparedStatement.setString(2, drug.getName());
                preparedStatement.setString(3, drug.getObjCls());
                preparedStatement.setBoolean(4, drug.isBiomarker());
                preparedStatement.setString(5, drug.getDrugUrl());

                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public List<Drug> findAll() {
        return findAllWithFavoriteIds(new HashSet<>());
    }

    public List<Drug> findAllWithFavoriteIds(Set<String> favoriteIds) {
        List<Drug> drugs = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id, name, obj_cls, drug_url, biomarker from drug"
                );

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Drug drug = buildDrug(resultSet);
                    drug.setFavorited(favoriteIds.contains(drug.getId()));
                    drugs.add(drug);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugs;
    }

    public List<Drug> findByKeywordWithFavoriteIds(String keyword, Set<String> favoriteIds) {
        return findByKeywordWithFilterAndFavoriteIds(keyword, "all", favoriteIds);
    }

    public List<Drug> findByKeywordWithFilterAndFavoriteIds(String keyword, String filter, Set<String> favoriteIds) {
        List<Drug> drugs = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                boolean searchAll = filter == null || filter.trim().isEmpty() || "all".equals(filter);

                String sql;

                if ("id".equals(filter)) {
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug where id like ?";
                } else if ("name".equals(filter)) {
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug where name like ?";
                } else if ("obj_cls".equals(filter)) {
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug where obj_cls like ?";
                } else if ("drug_url".equals(filter)) {
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug where drug_url like ?";
                } else if ("biomarker".equals(filter)) {
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug where cast(biomarker as char) like ?";
                } else {
                    searchAll = true;
                    sql = "select id, name, obj_cls, drug_url, biomarker from drug " +
                            "where id like ? " +
                            "or name like ? " +
                            "or obj_cls like ? " +
                            "or drug_url like ? " +
                            "or cast(biomarker as char) like ?";
                }

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                String likeKeyword = "%" + keyword + "%";

                if (searchAll) {
                    for (int i = 1; i <= 5; i++) {
                        preparedStatement.setString(i, likeKeyword);
                    }
                } else {
                    preparedStatement.setString(1, likeKeyword);
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Drug drug = buildDrug(resultSet);
                    drug.setFavorited(favoriteIds.contains(drug.getId()));
                    drugs.add(drug);
                }

            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugs;
    }

    public List<Drug> findFavoriteDrugs(String userId) {
        List<Drug> drugs = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select d.id, d.name, d.obj_cls, d.drug_url, d.biomarker " +
                                "from drug d " +
                                "inner join favorites f on d.id = f.resource_id " +
                                "where f.user_id = ? and f.resource_type = ? " +
                                "order by f.created_at desc"
                );

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, "drug");

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Drug drug = buildDrug(resultSet);
                    drug.setFavorited(true);
                    drugs.add(drug);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugs;
    }

    private Drug buildDrug(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String name = resultSet.getString("name");
        String objCls = resultSet.getString("obj_cls");
        String drugUrl = resultSet.getString("drug_url");
        boolean biomarker = resultSet.getBoolean("biomarker");

        return new Drug(id, name, biomarker, drugUrl, objCls);
    }
}