package cn.edu.zju.bean;

public class DrugLabel {

    private String id;
    private String name;
    private String objCls;
    private boolean alternateDrugAvailable;
    private boolean dosingInformation;
    private String prescribingMarkdown;
    private String source;
    private String textMarkdown;
    private String summaryMarkdown;
    private String raw;
    private String drugId;
    // 新增三个字段
    private String efficacySummary;
    private String responseWarning;
    private String alternativeDrug;

    public DrugLabel() {
    }

    // 旧版11参数构造器（兼容已有代码，新字段设为 null）
    public DrugLabel(String id, String name, String objCls, boolean alternateDrugAvailable,
                     boolean dosingInformation, String prescribingMarkdown, String source,
                     String textMarkdown, String summaryMarkdown, String raw, String drugId) {
        this(id, name, objCls, alternateDrugAvailable, dosingInformation, prescribingMarkdown,
                source, textMarkdown, summaryMarkdown, raw, drugId,
                null, null, null);
    }

    // 全参数构造器（包含新字段）
    public DrugLabel(String id, String name, String objCls, boolean alternateDrugAvailable,
                     boolean dosingInformation, String prescribingMarkdown, String source,
                     String textMarkdown, String summaryMarkdown, String raw, String drugId,
                     String efficacySummary, String responseWarning, String alternativeDrug) {
        this.id = id;
        this.name = name;
        this.objCls = objCls;
        this.alternateDrugAvailable = alternateDrugAvailable;
        this.dosingInformation = dosingInformation;
        this.prescribingMarkdown = prescribingMarkdown;
        this.source = source;
        this.textMarkdown = textMarkdown;
        this.summaryMarkdown = summaryMarkdown;
        this.raw = raw;
        this.drugId = drugId;
        this.efficacySummary = efficacySummary;
        this.responseWarning = responseWarning;
        this.alternativeDrug = alternativeDrug;
    }

    // Getter / Setter（原有 + 新增）
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getObjCls() { return objCls; }
    public void setObjCls(String objCls) { this.objCls = objCls; }

    public boolean isAlternateDrugAvailable() { return alternateDrugAvailable; }
    public void setAlternateDrugAvailable(boolean alternateDrugAvailable) { this.alternateDrugAvailable = alternateDrugAvailable; }

    public boolean isDosingInformation() { return dosingInformation; }
    public void setDosingInformation(boolean dosingInformation) { this.dosingInformation = dosingInformation; }

    public String getPrescribingMarkdown() { return prescribingMarkdown; }
    public void setPrescribingMarkdown(String prescribingMarkdown) { this.prescribingMarkdown = prescribingMarkdown; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTextMarkdown() { return textMarkdown; }
    public void setTextMarkdown(String textMarkdown) { this.textMarkdown = textMarkdown; }

    public String getSummaryMarkdown() { return summaryMarkdown; }
    public void setSummaryMarkdown(String summaryMarkdown) { this.summaryMarkdown = summaryMarkdown; }

    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }

    public String getDrugId() { return drugId; }
    public void setDrugId(String drugId) { this.drugId = drugId; }

    public String getEfficacySummary() { return efficacySummary; }
    public void setEfficacySummary(String efficacySummary) { this.efficacySummary = efficacySummary; }

    public String getResponseWarning() { return responseWarning; }
    public void setResponseWarning(String responseWarning) { this.responseWarning = responseWarning; }

    public String getAlternativeDrug() { return alternativeDrug; }
    public void setAlternativeDrug(String alternativeDrug) { this.alternativeDrug = alternativeDrug; }
}