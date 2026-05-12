package cn.edu.zju.dao;

import cn.edu.zju.bean.DrugLabel;
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

public class DrugLabelDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelDao.class);

    public boolean existsById(String id) {
        return super.existsById(id, "drug_label");
    }

    public void saveDrugLabel(DrugLabel drugLabel) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "insert into drug_label (" +
                                "id, name, obj_cls, alternate_drug_available, dosing_information, " +
                                "prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id, " +
                                "efficacy_summary, response_warning, alternative_drug" +
                                ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                );

                preparedStatement.setString(1, drugLabel.getId());
                preparedStatement.setString(2, drugLabel.getName());
                preparedStatement.setString(3, drugLabel.getObjCls());
                preparedStatement.setBoolean(4, drugLabel.isAlternateDrugAvailable());
                preparedStatement.setBoolean(5, drugLabel.isDosingInformation());
                preparedStatement.setString(6, drugLabel.getPrescribingMarkdown());
                preparedStatement.setString(7, drugLabel.getSource());
                preparedStatement.setString(8, drugLabel.getTextMarkdown());
                preparedStatement.setString(9, drugLabel.getSummaryMarkdown());
                preparedStatement.setString(10, drugLabel.getRaw());
                preparedStatement.setString(11, drugLabel.getDrugId());
                preparedStatement.setString(12, normalize(drugLabel.getEfficacySummary()));
                preparedStatement.setString(13, normalize(drugLabel.getResponseWarning()));
                preparedStatement.setString(14, normalize(drugLabel.getAlternativeDrug()));

                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public void updateSupplementFields(String id,
                                       String efficacySummary,
                                       String responseWarning,
                                       String alternativeDrug) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update drug_label " +
                                "set efficacy_summary = ?, " +
                                "response_warning = ?, " +
                                "alternative_drug = ? " +
                                "where id = ?"
                );

                preparedStatement.setString(1, normalize(efficacySummary));
                preparedStatement.setString(2, normalize(responseWarning));
                preparedStatement.setString(3, normalize(alternativeDrug));
                preparedStatement.setString(4, id);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public List findAll() {
        return findAllWithFavoriteIds(new HashSet());
    }

    public List findAllWithFavoriteIds(Set favoriteIds) {
        List drugLabels = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        getSelectSql() + " from drug_label"
                );

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = buildDrugLabel(resultSet);
                    drugLabel.setFavorited(favoriteIds != null && favoriteIds.contains(drugLabel.getId()));
                    drugLabels.add(drugLabel);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugLabels;
    }

    public List findByKeyword(String keyword) {
        return findByKeywordWithFilter(keyword, "all");
    }

    public List findByKeywordWithFilter(String keyword, String filter) {
        return findByKeywordWithFilterAndFavoriteIds(keyword, filter, new HashSet());
    }

    public List findByKeywordWithFilterAndFavoriteIds(String keyword, String filter, Set favoriteIds) {
        List drugLabels = new ArrayList<>();

        final String safeKeyword;
        if (keyword == null) {
            safeKeyword = "";
        } else {
            safeKeyword = keyword.trim().toLowerCase();
        }

        final String safeFilter;
        if (filter == null || filter.trim().isEmpty()) {
            safeFilter = "all";
        } else {
            safeFilter = filter.trim();
        }

        DBUtils.execSQL(connection -> {
            try {
                boolean searchAll = "all".equals(safeFilter);
                String wherePart;

                if ("id".equals(safeFilter)) {
                    wherePart = " where lower(id) like ?";
                } else if ("name".equals(safeFilter)) {
                    wherePart = " where lower(name) like ?";
                } else if ("source".equals(safeFilter)) {
                    wherePart = " where lower(source) like ?";
                } else if ("drug_id".equals(safeFilter)) {
                    wherePart = " where lower(drug_id) like ?";
                } else if ("dosing_information".equals(safeFilter)) {
                    wherePart = " where lower(cast(dosing_information as char)) like ?";
                } else if ("summary_markdown".equals(safeFilter)) {
                    wherePart = " where lower(summary_markdown) like ?";
                } else if ("efficacy_summary".equals(safeFilter)) {
                    wherePart = " where lower(efficacy_summary) like ?";
                } else if ("response_warning".equals(safeFilter)) {
                    wherePart = " where lower(response_warning) like ?";
                } else if ("alternative_drug".equals(safeFilter)) {
                    wherePart = " where lower(alternative_drug) like ?";
                } else {
                    /*
                     * User-facing All fields search.
                     * Only search visible or user-meaningful fields.
                     * Hidden raw/text_markdown/prescribing_markdown are excluded,
                     * because they may cause confusing matches that users cannot see.
                     */
                    searchAll = true;
                    wherePart = " where lower(id) like ? " +
                            "or lower(name) like ? " +
                            "or lower(source) like ? " +
                            "or lower(drug_id) like ? " +
                            "or lower(cast(dosing_information as char)) like ? " +
                            "or lower(summary_markdown) like ? " +
                            "or lower(efficacy_summary) like ? " +
                            "or lower(response_warning) like ? " +
                            "or lower(alternative_drug) like ?";
                }

                PreparedStatement preparedStatement = connection.prepareStatement(
                        getSelectSql() + " from drug_label " + wherePart
                );

                String likeKeyword = "%" + safeKeyword + "%";

                if (searchAll) {
                    for (int i = 1; i <= 9; i++) {
                        preparedStatement.setString(i, likeKeyword);
                    }
                } else {
                    preparedStatement.setString(1, likeKeyword);
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = buildDrugLabel(resultSet);
                    drugLabel.setFavorited(favoriteIds != null && favoriteIds.contains(drugLabel.getId()));
                    drugLabels.add(drugLabel);
                }
            } catch (SQLException e) {
                log.info("Drug label search failed.", e);
            }
        });

        return drugLabels;
    }

    public List findFavoriteDrugLabels(String userId) {
        List drugLabels = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        getSelectSql("dl") +
                                " from drug_label dl " +
                                "inner join favorites f on dl.id = f.resource_id " +
                                "where f.user_id = ? and f.resource_type = ? " +
                                "order by dl.id"
                );

                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, "drug_label");

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = buildDrugLabel(resultSet);
                    drugLabel.setFavorited(true);
                    drugLabels.add(drugLabel);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugLabels;
    }

    private String getSelectSql() {
        return getSelectSql(null);
    }

    private String getSelectSql(String alias) {
        String prefix = "";
        if (alias != null && !alias.trim().isEmpty()) {
            prefix = alias + ".";
        }

        return "select " +
                prefix + "id, " +
                prefix + "name, " +
                prefix + "obj_cls, " +
                prefix + "alternate_drug_available, " +
                prefix + "dosing_information, " +
                prefix + "prescribing_markdown, " +
                prefix + "source, " +
                prefix + "text_markdown, " +
                prefix + "summary_markdown, " +
                prefix + "raw, " +
                prefix + "drug_id, " +
                prefix + "efficacy_summary, " +
                prefix + "response_warning, " +
                prefix + "alternative_drug";
    }

    private DrugLabel buildDrugLabel(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String name = resultSet.getString("name");
        String objCls = resultSet.getString("obj_cls");
        boolean alternateDrugAvailable = resultSet.getBoolean("alternate_drug_available");
        boolean dosingInformation = resultSet.getBoolean("dosing_information");
        String prescribingMarkdown = resultSet.getString("prescribing_markdown");
        String source = resultSet.getString("source");
        String textMarkdown = resultSet.getString("text_markdown");
        String summaryMarkdown = resultSet.getString("summary_markdown");
        String raw = resultSet.getString("raw");
        String drugId = resultSet.getString("drug_id");
        String efficacySummary = resultSet.getString("efficacy_summary");
        String responseWarning = resultSet.getString("response_warning");
        String alternativeDrug = resultSet.getString("alternative_drug");

        return new DrugLabel(
                id,
                name,
                objCls,
                alternateDrugAvailable,
                dosingInformation,
                prescribingMarkdown,
                source,
                textMarkdown,
                summaryMarkdown,
                raw,
                drugId,
                efficacySummary,
                responseWarning,
                alternativeDrug
        );
    }

    private static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        return value.trim();
    }
}