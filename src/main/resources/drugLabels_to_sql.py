import json

# 文件路径
input_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/src/main/resources/drugLabels.data'
output_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/drugLabels_insert.sql'

print("正在读取 drugLabels.data...")

with open(output_file, 'w', encoding='utf-8') as out:
    # 写入文件头
    out.write("-- drugLabels 数据导入脚本\n")
    out.write("USE biomed;\n\n")
    
    count = 0
    error_count = 0
    
    # 逐行读取文件
    with open(input_file, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            if not line:  # 跳过空行
                continue
            
            try:
                # 解析 JSON
                item = json.loads(line)
                
                # 提取字段
                label_id = item.get('id', '')
                drug_id = item.get('drugId', '')
                source = item.get('source', '')
                biomarker = 1 if item.get('biomarker') else 0
                prescribing = 1 if item.get('prescribing') else 0
                url = item.get('url', '')
                
                # 生成 SQL 语句（处理特殊字符）
                label_id = label_id.replace("'", "''")
                drug_id = drug_id.replace("'", "''")
                source = source.replace("'", "''")
                url = url.replace("'", "''")
                
                sql = f"INSERT IGNORE INTO drug_label (id, drug_id, source, biomarker, prescribing, url) VALUES ('{label_id}', '{drug_id}', '{source}', {biomarker}, {prescribing}, '{url}');\n"
                out.write(sql)
                
                count += 1
                if count % 1000 == 0:
                    print(f"已处理 {count} 条...")
                    
            except json.JSONDecodeError as e:
                error_count += 1
                if error_count <= 5:  # 只打印前5个错误
                    print(f"第 {line_num} 行 JSON 解析失败: {e}")
                continue
            except Exception as e:
                error_count += 1
                if error_count <= 5:
                    print(f"第 {line_num} 行处理失败: {e}")
                continue

print(f"\n转换完成！")
print(f"成功生成: {count} 条 SQL 语句")
print(f"跳过行数: {error_count} 行")
print(f"输出文件: {output_file}")