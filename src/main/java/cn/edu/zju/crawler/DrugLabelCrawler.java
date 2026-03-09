package cn.edu.zju.crawler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class DrugLabelCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelCrawler.class);

    public static final String URL_DRUG_LABEL = "https://api.pharmgkb.org/v1/data/label?source=fda";
    public static final String URL_DRUG_LABEL_DETAIL = "https://api.pharmgkb.org/v1/data/label/%s?view=base";
    private Path drugsPath = new File("drugs.data").toPath();
    private Path drugLabelsPath = new File("drugLabels.data").toPath();

    public void doCrawlerDrug() {
        String content = this.getURLContent(URL_DRUG_LABEL);

        try {
            // 修改1: writeString → write
            Files.write(drugsPath, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doCrawlerDrugLabel() {
        try {
            if (Files.exists(drugLabelsPath)) {
                try {
                    Files.delete(drugLabelsPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Files.createFile(drugLabelsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 修改2: readString → readAllBytes + new String
            byte[] drugBytes = Files.readAllBytes(drugsPath);
            String drugContent = new String(drugBytes, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Map drugs = gson.fromJson(drugContent, Map.class);
            List<Map> data = (List<Map>) drugs.get("data");
            data.stream().forEach(x -> {
                log.info("{}", x);
                String id = (String) (x.get("id"));

                String content = this.getURLContent(String.format(URL_DRUG_LABEL_DETAIL, id));
                Map result = gson.fromJson(content, Map.class);
                Map drugLabel = (Map) result.get("data");
                log.info("Fetch label of drug {}", id);
                try {
                    // 修改3: 两个 writeString → write
                    Files.write(drugLabelsPath, gson.toJson(drugLabel).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(drugLabelsPath, "\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}