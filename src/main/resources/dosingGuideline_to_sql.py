import json
import re

# ==================== 配置区域 ====================
input_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/src/main/resources/dosingGuideline.data'
output_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/dosingGuideline_insert_fixed.sql'
# =================================================

def clean_text(text):
    """清洗文本，移除非法字符"""
    if not isinstance(text, str):
        return text
    # 替换 smart quotes 和其他特殊字符
    text = text.replace('\x93', '"')  # 左双引号
    text = text.replace('\x94', '"')  # 右双引号
    text = text.replace('\x91', "'")  # 左单引号
    text = text.replace('\x92', "'")  # 右单引号
    text = text.replace('\x96', '-')  # 短破折号
    text = text.replace('\x97', '-')  # 长破折号
    text = text.replace('\x85', '...')  # 省略号
    # 移除其他控制字符
    text = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f\x7f-\x9f]', '', text)
    return text

print("开始读取 dosingGuideline.data 文件...")

with open(output_file, 'w', encoding='utf-8') as out:
    # 写入文件头
    out.write("-- dosing_guideline 数据导入脚本（清洗版）\n")
    out.write("USE biomed;\n\n")
    
    count = 0
    error_count = 0
    line_num = 0
    duplicate_count = 0
    too_long_count = 0
    
    with open(input_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            line_num += 1
            
            if not line:  # 跳过空行
                continue
            
            try:
                # 解析当前行的 JSON
                item = json.loads(line)
                
                # 提取 data 字段（如果存在）
                if 'data' in item:
                    item = item['data']
                
                # --- 提取字段，完全匹配表结构 ---
                
                # id: 必填字段
                guideline_id = item.get('id', '')
                
                # obj_cls: 对象类型
                obj_cls = clean_text(item.get('objCls', ''))
                
                # name: 名称（截断到100字符）
                name = clean_text(item.get('name', ''))
                if len(name) > 100:
                    name = name[:97] + '...'
                    too_long_count += 1
                
                # recommendation: 是否有推荐
                recommendation = 1 if item.get('recommendation') else 1  # 默认设为1
                
                # drug_id: 药物ID（从 relatedChemicals 提取）
                drug_id = ''
                related_chemicals = item.get('relatedChemicals', [])
                if related_chemicals and len(related_chemicals) > 0:
                    drug_id = related_chemicals[0].get('id', '')
                
                # source: 来源
                source = clean_text(item.get('source', ''))
                if not source:
                    # 尝试从 guidelineGenes 里找 source
                    guideline_genes = item.get('guidelineGenes', [])
                    if guideline_genes and len(guideline_genes) > 0:
                        gene_info = guideline_genes[0]
                        gene_obj = gene_info.get('gene', {})
                        if gene_obj and 'object' in gene_obj:
                            gene_obj_data = gene_obj['object']
                            source = clean_text(gene_obj_data.get('source', ''))
                
                # summary_markdown: 摘要
                summary_markdown = ''
                if item.get('summaryMarkdown'):
                    summary_markdown = clean_text(item['summaryMarkdown'].get('html', ''))
                
                # text_markdown: 文本内容
                text_markdown = ''
                if item.get('textMarkdown'):
                    text_markdown = clean_text(item['textMarkdown'].get('html', ''))
                
                # raw: 完整 JSON（清洗后）
                raw = clean_text(json.dumps(item, ensure_ascii=False))
                
                # --- 生成 INSERT IGNORE 语句 ---
                sql = f"""
INSERT IGNORE INTO dosing_guideline (
    id, obj_cls, name, recommendation, drug_id, source, summary_markdown, text_markdown, raw
) VALUES (
    '{guideline_id}',
    '{obj_cls.replace("'", "''")}',
    '{name.replace("'", "''")}',
    {recommendation},
    '{drug_id}',
    '{source.replace("'", "''")}',
    '{summary_markdown.replace("'", "''")}',
    '{text_markdown.replace("'", "''")}',
    '{raw.replace("'", "''")}'
);
"""
                out.write(sql)
                count += 1
                
                if count % 10 == 0:
                    print(f"已处理 {count} 条...")
                    
            except json.JSONDecodeError as e:
                error_count += 1
                print(f"第 {line_num} 行 JSON 解析失败: {e}")
                continue
            except Exception as e:
                error_count += 1
                print(f"第 {line_num} 行处理失败: {e}")
                continue

print(f"\n🎉 转换完成！")
print(f"成功生成: {count} 条 SQL 语句")
print(f"失败: {error_count} 条")
print(f"截断的 name: {too_long_count} 条")
print(f"输出文件: {output_file}")