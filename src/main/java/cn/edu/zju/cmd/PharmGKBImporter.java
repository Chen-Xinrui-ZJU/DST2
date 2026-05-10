package cn.edu.zju.cmd;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class PharmGKBImporter {

    private static final Logger log = LoggerFactory.getLogger(PharmGKBImporter.class);

    public static void main(String[] args) {
        PharmGKBImporter pharmGKBImporter = new PharmGKBImporter();
        pharmGKBImporter.importDosingGuideline();
        pharmGKBImporter.importDrug();
        pharmGKBImporter.importDrugLabel();
    }

    private void importDosingGuideline() {
        Gson gson = new Gson();
        InputStream is = getClass().getResourceAsStream("/dosingGuideline.data");
        List<String> drugLabelsContent =
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(toList());
        DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();

        drugLabelsContent.forEach(content -> {
            Map guideline = gson.fromJson(content, Map.class);
            Map data = ((Map) guideline.get("data"));
            String id = (String) data.get("id");
            String objCls = (String) data.get("objCls");
            String name = (String) data.get("name");
            boolean recommendation = (Boolean) data.get("recommendation");
            String drugId = ((String) ((List<Map>) data.get("relatedChemicals")).get(0).get("id"));
            String source = (String) data.get("source");
            String summaryMarkdown = getMarkdownHtml(data.get("summaryMarkdown"));
            String textMarkdown = getMarkdownHtml(data.get("textMarkdown"));
            String raw = gson.toJson(guideline);

            DosingGuideline dosingGuideline = new DosingGuideline(
                    id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw
            );

            if (!dosingGuidelineDao.existsById(id)) {
                dosingGuidelineDao.saveDosingGuideline(dosingGuideline);
                log.info("Saving dosing guideline: {}", id);
            } else {
                log.info("Dosing guideline exists, skipping: {}", id);
            }
        });
    }

    private void importDrug() {
        Gson gson = new Gson();
        InputStream is = getClass().getResourceAsStream("/drugs.data");
        String drugsContent =
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines()
                        .parallel()
                        .collect(Collectors.joining("\n"));

        Map drugs = gson.fromJson(drugsContent, Map.class);
        List<Map> drugList = (List<Map>) drugs.get("data");

        DrugDao drugDao = new DrugDao();

        drugList.forEach(x -> {
            Map drug = ((Map) x.get("drug"));
            String id = (String) drug.get("id");
            String name = (String) drug.get("name");
            String objCls = (String) drug.get("objCls");
            String drugUrl = (String) x.get("drugUrl");
            boolean biomarker = ((Boolean) x.get("biomarker"));
            Drug drugBean = new Drug(id, name, biomarker, drugUrl, objCls);

            if (!drugDao.existsById(id)) {
                drugDao.saveDrug(drugBean);
                log.info("Saved drug: {}", id);
            } else {
                log.info("Drug {} already exists, skip", id);
            }
        });
    }

    private void importDrugLabel() {
        Gson gson = new Gson();
        InputStream is = getClass().getResourceAsStream("/drugLabels.data");
        List<String> drugLabelsContent =
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(toList());

        DrugLabelDao drugLabelDao = new DrugLabelDao();

        drugLabelsContent.forEach(line -> {
            Map x = gson.fromJson(line, Map.class);
            String labelId = (String) x.get("id");
            log.info("Going to save label: {}", labelId);

            String name = (String) x.get("id");
            String objCls = (String) x.get("objCls");
            boolean alternateDrugAvailable = getBoolean(x.get("alternateDrugAvailable"));
            boolean dosingInformation = getBoolean(x.get("dosingInformation"));

            String prescribingMarkdown = "";
            if (x.containsKey("prescribingMarkdown")) {
                prescribingMarkdown = getMarkdownHtml(x.get("prescribingMarkdown"));
            }

            String source = (String) x.get("source");
            String textMarkdown = getMarkdownHtml(x.get("textMarkdown"));
            String summaryMarkdown = getMarkdownHtml(x.get("summaryMarkdown"));
            String raw = gson.toJson(x);
            String drugId = ((String) ((List<Map>) x.get("relatedChemicals")).get(0).get("id"));

            String efficacySummary = extractTextField(x,
                    "efficacySummary", "efficacy_summary", "efficacyMarkdown", "efficacy");
            String responseWarning = extractTextField(x,
                    "responseWarning", "response_warning", "responseMarkdown", "warning", "warningMarkdown");
            String alternativeDrug = extractTextField(x,
                    "alternativeDrug", "alternative_drug", "alternateDrug", "alternate_drug",
                    "alternativeDrugMarkdown", "alternateDrugMarkdown");

            DrugLabel drugLabelBean = new DrugLabel(
                    labelId,
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

            if (!drugLabelDao.existsById(labelId)) {
                drugLabelDao.saveDrugLabel(drugLabelBean);
                log.info("Saved: {}", labelId);
            } else {
                drugLabelDao.updateSupplementFields(labelId, efficacySummary, responseWarning, alternativeDrug);
                log.info("Label {} already exists, updated supplement fields if available", labelId);
            }
        });
    }

    private static boolean getBoolean(Object value) {
        return value != null && Boolean.TRUE.equals(value);
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

    private static String extractTextField(Map source, String... keys) {
        for (String key : keys) {
            Object value = findValueIgnoreCase(source, key);
            String text = stringifyValue(value);
            if (!isBlank(text)) {
                return text;
            }
        }
        return "";
    }

    private static Object findValueIgnoreCase(Object node, String key) {
        if (node == null) {
            return null;
        }

        if (node instanceof Map) {
            Map map = (Map) node;

            for (Object entryObj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                Object entryKey = entry.getKey();

                if (entryKey != null && key.equalsIgnoreCase(String.valueOf(entryKey))) {
                    return entry.getValue();
                }
            }

            for (Object value : map.values()) {
                Object result = findValueIgnoreCase(value, key);
                if (result != null) {
                    return result;
                }
            }
        }

        if (node instanceof List) {
            List list = (List) node;
            for (Object item : list) {
                Object result = findValueIgnoreCase(item, key);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private static String stringifyValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Map) {
            return getMarkdownHtml(value);
        }

        return String.valueOf(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}

