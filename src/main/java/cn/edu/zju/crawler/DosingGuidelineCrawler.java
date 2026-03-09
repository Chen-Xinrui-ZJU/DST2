package cn.edu.zju.crawler;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class DosingGuidelineCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineCrawler.class);

    public static final String URL_BASE = "https://api.pharmgkb.org/v1/data%s";
    public static final String URL_GUIDELINES = "https://api.pharmgkb.org/v1/data/guidelineAnnotation?source=cpic";
    private Path path = new File("dosingGuideline.data").toPath();

    public void doCrawlerDosingGuidelineList() {
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = this.getURLContent(URL_GUIDELINES);
        Gson gson = new Gson();
        Map dosingGuidelines = gson.fromJson(content, Map.class);
        List<Map> data = (List<Map>) dosingGuidelines.get("data");
        data.stream().forEach(x -> {
            log.info("{}", x);
            try {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(path, "\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
