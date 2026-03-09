import json
import os

# 文件路径配置
input_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/src/main/resources/drugs.data'
output_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/drugs_insert.sql'

# 读取 JSON 文件
print("正在读取文件...")
with open(input_file, 'r', encoding='utf-8') as f:
    data = json.load(f)

# 打开输出文件
with open(output_file, 'w', encoding='utf-8') as out:
    out.write("-- 自动生成的 SQL 插入语句\n")
    out.write("USE biomed;\n\n")
    
    count = 0
    # 遍历 data 数组
    for item in data['data']:
        # 获取 drug 对象
        drug = item.get('drug', {})
        
        # 提取字段
        drug_id = drug.get('id', '')
        drug_name = drug.get('name', '')
        obj_cls = drug.get('objcls', 'Chemical')
        
        # 处理 biomarker（可能是字符串 "true"/"false" 或布尔值）
        biomarker_val = item.get('biomarker', False)
        if isinstance(biomarker_val, str):
            biomarker = 1 if biomarker_val.lower() == 'true' else 0
        else:
            biomarker = 1 if biomarker_val else 0
        
        # 生成 drug_url
        drug_url = f"https://www.pharmgkb.org/chemical/{drug_id}"
        
        # 生成 INSERT 语句（处理特殊字符）
        drug_name_escaped = drug_name.replace("'", "''")
        
        sql = f"INSERT IGNORE INTO drug (id, name, obj_cls, biomarker, drug_url) VALUES ('{drug_id}', '{drug_name_escaped}', '{obj_cls}', {biomarker}, '{drug_url}');\n"
        out.write(sql)
        count += 1
        
        # 每100条打印一次进度
        if count % 100 == 0:
            print(f"已处理 {count} 条...")

print(f"转换完成！共生成 {count} 条 SQL 语句")
print(f"输出文件：{output_file}")