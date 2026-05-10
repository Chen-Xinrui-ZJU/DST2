package cn.edu.zju.crawler;

import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dbutils.DBUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DrugLabelCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelCrawler.class);

    public static final String URL_DRUG_LABEL_DETAIL =
            "https://api.pharmgkb.org/v1/data/label/%s?view=base";

    private DrugLabelDao drugLabelDao = new DrugLabelDao();

    public static void main(String[] args) {
        DrugLabelCrawler crawler = new DrugLabelCrawler();
        crawler.doCrawlerDrugLabel();
    }

    public void doCrawlerDrugLabel() {
        List<String> labelIds = loadLabelIds();

        log.info("Total labels to supplement: {}", labelIds.size());

        int processed = 0;
        int updated = 0;
        int skipped = 0;

        for (String labelId : labelIds) {
            processed++;

            try {
                String url = String.format(URL_DRUG_LABEL_DETAIL, labelId);
                log.info("Fetching label detail {}/{}: {}", processed, labelIds.size(), labelId);

                String content = this.getURLContent(url);

                if (isBlank(content)) {
                    skipped++;
                    log.info("Empty response for label {}, skip", labelId);
                    continue;
                }

                Gson gson = new Gson();
                Map result = gson.fromJson(content, Map.class);

                if (result == null || !result.containsKey("data")) {
                    skipped++;
                    log.info("No data field for label {}, skip", labelId);
                    continue;
                }

                Map data = (Map) result.get("data");

                if (data == null) {
                    skipped++;
                    log.info("Data is null for label {}, skip", labelId);
                    continue;
                }

                String summaryMarkdown = getMarkdownHtml(data.get("summaryMarkdown"));
                String textMarkdown = getMarkdownHtml(data.get("textMarkdown"));

                String efficacySummary = extractEfficacySummary(summaryMarkdown, textMarkdown);
                String responseWarning = extractResponseWarning(textMarkdown);
                String alternativeDrug = extractAlternativeDrug(data);

                drugLabelDao.updateSupplementFields(
                        labelId,
                        efficacySummary,
                        responseWarning,
                        alternativeDrug
                );

                if (!isBlank(efficacySummary) || !isBlank(responseWarning) || !isBlank(alternativeDrug)) {
                    updated++;
                }

                if (processed == 1) {
                    System.out.println();
                    System.out.println("===== FIRST LABEL SUPPLEMENT DEBUG =====");
                    System.out.println("Label ID: " + labelId);
                    System.out.println("Efficacy Summary empty? " + isBlank(efficacySummary));
                    System.out.println("Response Warning empty? " + isBlank(responseWarning));
                    System.out.println("Alternative Drug empty? " + isBlank(alternativeDrug));
                    System.out.println("========================================");
                    System.out.println();
                }

            } catch (Exception e) {
                skipped++;
                log.info("Failed to process label {}, skip", labelId, e);
            }
        }

        System.out.println();
        System.out.println("===== DRUG LABEL SUPPLEMENT FINISHED =====");
        System.out.println("Processed labels: " + processed);
        System.out.println("Updated labels: " + updated);
        System.out.println("Skipped labels: " + skipped);
        System.out.println("==========================================");
    }

    private List<String> loadLabelIds() {
        List<String> labelIds = new ArrayList<>();

        DBUtils.execSQL(connection -> {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select id from drug_label"
                );

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    labelIds.add(resultSet.getString("id"));
                }

            } catch (SQLException e) {
                log.info("", e);
            }
        });

        return labelIds;
    }

    private static String extractEfficacySummary(String summaryMarkdown, String textMarkdown) {
        if (!isBlank(summaryMarkdown)) {
            return summaryMarkdown;
        }

        if (!isBlank(textMarkdown)) {
            String lower = textMarkdown.toLowerCase();

            if (lower.contains("efficacy")
                    || lower.contains("effective")
                    || lower.contains("treatment")
                    || lower.contains("indicated")
                    || lower.contains("indication")
                    || lower.contains("reduce")
                    || lower.contains("response")) {
                return textMarkdown;
            }
        }

        return "";
    }

    private static String extractResponseWarning(String textMarkdown) {
        if (isBlank(textMarkdown)) {
            return "";
        }

        String lower = textMarkdown.toLowerCase();

        if (lower.contains("warning")
                || lower.contains("warnings")
                || lower.contains("precaution")
                || lower.contains("precautions")
                || lower.contains("contraindication")
                || lower.contains("contraindications")
                || lower.contains("contraindicated")
                || lower.contains("adverse reaction")
                || lower.contains("adverse reactions")
                || lower.contains("toxicity")
                || lower.contains("avoid use")
                || lower.contains("increased risk")) {
            return textMarkdown;
        }

        return "";
    }

    private static String extractAlternativeDrug(Map data) {
        Object value = data.get("alternateDrugAvailable");

        if (value != null && Boolean.TRUE.equals(value)) {
            return "Alternative drug information is available for this PharmGKB label annotation.";
        }

        return "";
    }

    private static String getMarkdownHtml(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Map) {
            Map map = (Map) value;

            Object html = map.get("html");
            if (html instanceof String) {
                return (String) html;
            }

            Object markdown = map.get("markdown");
            if (markdown instanceof String) {
                return (String) markdown;
            }

            Object text = map.get("text");
            if (text instanceof String) {
                return (String) text;
            }
        }

        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}