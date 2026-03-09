import json
import mysql.connector
from mysql.connector import Error

# ==================== 配置区域 ====================
# 文件路径
data_file = 'F:/1Course_Material/DST/Practical2/haining_biomed/src/main/resources/drugLabels.data'

# 数据库连接配置
db_config = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'Cxr20060403',
    'database': 'biomed',
    'charset': 'utf8mb4'
}

# 每批插入的数据条数
batch_size = 100
# =================================================

def connect_to_mysql():
    """连接数据库"""
    try:
        connection = mysql.connector.connect(**db_config)
        if connection.is_connected():
            print("✅ 成功连接到数据库")
            return connection
    except Error as e:
        print(f"❌ 连接数据库失败: {e}")
        return None

def clear_table(cursor):
    """清空表"""
    try:
        cursor.execute("TRUNCATE TABLE drug_label")
        print("✅ 已清空 drug_label 表")
    except Error as e:
        print(f"❌ 清空表失败: {e}")

def import_data():
    """导入数据主函数"""
    connection = connect_to_mysql()
    if not connection:
        return
    
    cursor = connection.cursor()
    
    # 询问是否清空表
    choice = input("是否清空 drug_label 表？(y/n): ").strip().lower()
    if choice == 'y':
        clear_table(cursor)
    else:
        print("保留现有数据，继续导入...")
    
    # SQL 插入语句（使用 INSERT IGNORE 跳过重复）
    sql = """
        INSERT IGNORE INTO drug_label (
            id, name, obj_cls, alternate_drug_available, dosing_information,
            prescribing_markdown, text_markdown, summary_markdown, source, drug_id, raw
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """
    
    total = 0
    success = 0
    batch = []
    
    print("\n开始读取 drugLabels.data 文件...")
    
    try:
        with open(data_file, 'r', encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue
                
                try:
                    item = json.loads(line)
                    
                    # 提取字段
                    id = item.get('id', '')
                    name = item.get('name', '')
                    obj_cls = item.get('objCls', '')
                    
                    # 布尔值转 0/1
                    alternate_drug_available = 1 if item.get('alternateDrugAvailable') else 0
                    dosing_information = 1 if item.get('dosingInformation') else 0
                    
                    # 提取 Markdown 内容
                    prescribing_markdown = ''
                    if item.get('prescribingMarkdown'):
                        prescribing_markdown = item['prescribingMarkdown'].get('html', '')
                    
                    text_markdown = ''
                    if item.get('textMarkdown'):
                        text_markdown = item['textMarkdown'].get('html', '')
                    
                    summary_markdown = ''
                    if item.get('summaryMarkdown'):
                        summary_markdown = item['summaryMarkdown'].get('html', '')
                    
                    source = item.get('source', '')
                    
                    # 提取 drug_id
                    drug_id = ''
                    related_chemicals = item.get('relatedChemicals', [])
                    if related_chemicals and len(related_chemicals) > 0:
                        drug_id = related_chemicals[0].get('id', '')
                    
                    # 整行 JSON 存到 raw 字段
                    raw = json.dumps(item, ensure_ascii=False)
                    
                    batch.append((
                        id, name, obj_cls, alternate_drug_available, dosing_information,
                        prescribing_markdown, text_markdown, summary_markdown, source, drug_id, raw
                    ))
                    
                    total += 1
                    
                    # 每 batch_size 条执行一次插入
                    if len(batch) >= batch_size:
                        cursor.executemany(sql, batch)
                        connection.commit()
                        success += len(batch)
                        print(f"已导入 {success} 条...")
                        batch = []
                        
                except json.JSONDecodeError as e:
                    print(f"第 {line_num} 行 JSON 解析失败: {e}")
                    continue
                except Exception as e:
                    print(f"第 {line_num} 行处理失败: {e}")
                    continue
        
        # 插入剩余的数据
        if batch:
            cursor.executemany(sql, batch)
            connection.commit()
            success += len(batch)
            
        print(f"\n🎉 导入完成！")
        print(f"总读取行数: {total}")
        print(f"成功导入: {success} 条")
        
        # 验证导入结果
        cursor.execute("SELECT COUNT(*) FROM drug_label")
        count = cursor.fetchone()[0]
        print(f"📊 drug_label 表现在有 {count} 条数据")
        
        # 显示前3条示例
        cursor.execute("SELECT id, name, drug_id FROM drug_label LIMIT 3")
        rows = cursor.fetchall()
        print("\n前3条数据示例：")
        for row in rows:
            print(f"  {row}")
            
    except FileNotFoundError:
        print(f"❌ 找不到文件: {data_file}")
    except Error as e:
        print(f"❌ 数据库错误: {e}")
        connection.rollback()
    finally:
        cursor.close()
        connection.close()
        print("数据库连接已关闭")

if __name__ == "__main__":
    print("=" * 60)
    print("drugLabels.data 数据导入工具（匹配现有表结构）")
    print("=" * 60)
    import_data()