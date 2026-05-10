package cn.edu.zju.dao;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<DrugLabel> findAll() {
        List<DrugLabel> drugLabels = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        getSelectSql() + " from drug_label"
                );

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = buildDrugLabel(resultSet);
                    drugLabels.add(drugLabel);
                }

            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugLabels;
    }

    public List<DrugLabel> findByKeyword(String keyword) {
        return findByKeywordWithFilter(keyword, "all");
    }

    public List<DrugLabel> findByKeywordWithFilter(String keyword, String filter) {
        List<DrugLabel> drugLabels = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                boolean searchAll = filter == null || filter.trim().isEmpty() || "all".equals(filter);

                String wherePart;

                if ("id".equals(filter)) {
                    wherePart = " where id like ?";
                } else if ("name".equals(filter)) {
                    wherePart = " where name like ?";
                } else if ("source".equals(filter)) {
                    wherePart = " where source like ?";
                } else if ("drug_id".equals(filter)) {
                    wherePart = " where drug_id like ?";
                } else if ("dosing_information".equals(filter)) {
                    wherePart = " where cast(dosing_information as char) like ?";
                } else if ("summary_markdown".equals(filter)) {
                    wherePart = " where summary_markdown like ?";
                } else if ("efficacy_summary".equals(filter)) {
                    wherePart = " where efficacy_summary like ?";
                } else if ("response_warning".equals(filter)) {
                    wherePart = " where response_warning like ?";
                } else if ("alternative_drug".equals(filter)) {
                    wherePart = " where alternative_drug like ?";
                } else {
                    searchAll = true;
                    wherePart =
                            " where id like ? " +
                                    "or name like ? " +
                                    "or obj_cls like ? " +
                                    "or cast(alternate_drug_available as char) like ? " +
                                    "or cast(dosing_information as char) like ? " +
                                    "or prescribing_markdown like ? " +
                                    "or source like ? " +
                                    "or text_markdown like ? " +
                                    "or summary_markdown like ? " +
                                    "or raw like ? " +
                                    "or drug_id like ? " +
                                    "or efficacy_summary like ? " +
                                    "or response_warning like ? " +
                                    "or alternative_drug like ?";
                }

                PreparedStatement preparedStatement = connection.prepareStatement(
                        getSelectSql() + " from drug_label " + wherePart
                );

                String likeKeyword = "%" + keyword + "%";

                if (searchAll) {
                    for (int i = 1; i <= 14; i++) {
                        preparedStatement.setString(i, likeKeyword);
                    }
                } else {
                    preparedStatement.setString(1, likeKeyword);
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = buildDrugLabel(resultSet);
                    drugLabels.add(drugLabel);
                }

            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugLabels;
    }

    private String getSelectSql() {
        return "select id, name, obj_cls, alternate_drug_available, dosing_information, " +
                "prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id, " +
                "efficacy_summary, response_warning, alternative_drug";
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