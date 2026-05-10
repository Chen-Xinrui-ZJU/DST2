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
            String summaryMarkdown = ((String) ((Map) data.get("summaryMarkdown")).get("html"));
            String textMarkdown = ((String) ((Map) data.get("textMarkdown")).get("html"));
            String raw = gson.toJson(guideline);
            DosingGuideline dosingGuideline = new DosingGuideline(id, objCls, name, recommendation, drugId, source, summaryMarkdown, textMarkdown, raw);
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
                new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().parallel().collect(Collectors.joining("\n"));

        Map drugs = gson.fromJson(drugsContent, Map.class);
        List<Map> drugList = (List<Map>) drugs.get("data");

        DrugDao drugDao = new DrugDao();

        drugList.stream().forEach(x -> {
            log.info("{}", x);
            Map drug = ((Map) x.get("drug"));
            String id = (String) drug.get("id");
            String name = (String) drug.get("name");
            String objCls = (String) drug.get("objCls");
            String drugUrl = (String) x.get("drugUrl");
            boolean biomarker = ((Boolean) x.get("biomarker"));
            Drug drugBean = new Drug(id, name, biomarker, drugUrl, objCls);

            drugDao.saveDrug(drugBean);
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
            log.info("Processing label: {}", labelId);

            // 提取 name（可能没有，用 id 代替）
            String name = (String) x.get("name");
            if (name == null) name = labelId;
            String objCls = (String) x.get("objCls");
            boolean alternateDrugAvailable = x.get("alternateDrugAvailable") != null && (Boolean) x.get("alternateDrugAvailable");
            boolean dosingInformation = x.get("dosingInformation") != null && (Boolean) x.get("dosingInformation");

            String prescribingMarkdown = "";
            if (x.containsKey("prescribingMarkdown") && x.get("prescribingMarkdown") instanceof Map) {
                prescribingMarkdown = (String) ((Map) x.get("prescribingMarkdown")).get("html");
                if (prescribingMarkdown == null) prescribingMarkdown = "";
            }
            String source = (String) x.get("source");
            String textMarkdown = "";
            if (x.containsKey("textMarkdown") && x.get("textMarkdown") instanceof Map) {
                textMarkdown = (String) ((Map) x.get("textMarkdown")).get("html");
            }
            String summaryMarkdown = "";
            if (x.containsKey("summaryMarkdown") && x.get("summaryMarkdown") instanceof Map) {
                summaryMarkdown = (String) ((Map) x.get("summaryMarkdown")).get("html");
            }
            String raw = gson.toJson(x);
            String drugId = null;
            if (x.containsKey("relatedChemicals") && x.get("relatedChemicals") instanceof List && !((List) x.get("relatedChemicals")).isEmpty()) {
                drugId = (String) ((Map) ((List) x.get("relatedChemicals")).get(0)).get("id");
            }

            // 提取新字段（爬虫增强版生成）
            String efficacySummary = (String) x.get("efficacy_summary");
            String responseWarning = (String) x.get("response_warning");
            String alternativeDrug = (String) x.get("alternative_drug");

            DrugLabel drugLabelBean = new DrugLabel(labelId, name, objCls, alternateDrugAvailable, dosingInformation,
                    prescribingMarkdown, source, textMarkdown, summaryMarkdown, raw, drugId,
                    efficacySummary, responseWarning, alternativeDrug);

            if (drugLabelDao.existsById(labelId)) {
                // 存在则只更新三个新字段
                drugLabelDao.updateNewFields(drugLabelBean);
                log.info("Updated new fields for label: {}", labelId);
            } else {
                drugLabelDao.saveDrugLabel(drugLabelBean);
                log.info("Inserted label: {}", labelId);
            }
        });
    }
}