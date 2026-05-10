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
                String sql = "insert into drug_label (id,name,obj_cls,alternate_drug_available,dosing_information," +
                        "prescribing_markdown,source,text_markdown,summary_markdown,raw,drug_id," +
                        "efficacy_summary,response_warning,alternative_drug) " +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
                preparedStatement.setString(12, drugLabel.getEfficacySummary());
                preparedStatement.setString(13, drugLabel.getResponseWarning());
                preparedStatement.setString(14, drugLabel.getAlternativeDrug());
                preparedStatement.execute();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    // 新增：只更新三个新字段的方法
    public void updateNewFields(DrugLabel drugLabel) {
        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "UPDATE drug_label SET efficacy_summary = ?, response_warning = ?, alternative_drug = ? WHERE id = ?");
                ps.setString(1, drugLabel.getEfficacySummary());
                ps.setString(2, drugLabel.getResponseWarning());
                ps.setString(3, drugLabel.getAlternativeDrug());
                ps.setString(4, drugLabel.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.info("", e);
            }
        });
    }

    public List<DrugLabel> findAll() {
        List<DrugLabel> drugLabels = new ArrayList<>();
        DBUtils.execSQL(connection -> {
            try {
                String sql = "select id, name, obj_cls, alternate_drug_available, dosing_information, " +
                        "prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id, " +
                        "efficacy_summary, response_warning, alternative_drug from drug_label";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String obj_cls = resultSet.getString("obj_cls");
                    boolean alternate_drug_available = resultSet.getBoolean("alternate_drug_available");
                    boolean dosing_information = resultSet.getBoolean("dosing_information");
                    String prescribing_markdown = resultSet.getString("prescribing_markdown");
                    String source = resultSet.getString("source");
                    String text_markdown = resultSet.getString("text_markdown");
                    String summary_markdown = resultSet.getString("summary_markdown");
                    String raw = resultSet.getString("raw");
                    String drug_id = resultSet.getString("drug_id");
                    String efficacy_summary = resultSet.getString("efficacy_summary");
                    String response_warning = resultSet.getString("response_warning");
                    String alternative_drug = resultSet.getString("alternative_drug");

                    DrugLabel drugLabel = new DrugLabel(id, name, obj_cls, alternate_drug_available,
                            dosing_information, prescribing_markdown, source, text_markdown,
                            summary_markdown, raw, drug_id, efficacy_summary, response_warning, alternative_drug);
                    drugLabels.add(drugLabel);
                }
            } catch (SQLException e) {
                log.info("", e);
            }
        });
        return drugLabels;
    }

    public List<DrugLabel> findByKeyword(String keyword) {
        List<DrugLabel> drugLabels = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                String sql =
                        "select id, name, obj_cls, alternate_drug_available, dosing_information, " +
                                "prescribing_markdown, source, text_markdown, summary_markdown, raw, drug_id, " +
                                "efficacy_summary, response_warning, alternative_drug " +
                                "from drug_label " +
                                "where id like ? " +
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

                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                String likeKeyword = "%" + keyword + "%";
                for (int i = 1; i <= 14; i++) {
                    preparedStatement.setString(i, likeKeyword);
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    DrugLabel drugLabel = new DrugLabel(
                            resultSet.getString("id"),
                            resultSet.getString("name"),
                            resultSet.getString("obj_cls"),
                            resultSet.getBoolean("alternate_drug_available"),
                            resultSet.getBoolean("dosing_information"),
                            resultSet.getString("prescribing_markdown"),
                            resultSet.getString("source"),
                            resultSet.getString("text_markdown"),
                            resultSet.getString("summary_markdown"),
                            resultSet.getString("raw"),
                            resultSet.getString("drug_id"),
                            resultSet.getString("efficacy_summary"),
                            resultSet.getString("response_warning"),
                            resultSet.getString("alternative_drug")
                    );

                    drugLabels.add(drugLabel);
                }

            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return drugLabels;
    }
}