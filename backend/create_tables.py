"""
创建数据库表的脚本
运行此脚本将创建所有表
"""
from app.core.database import Base, engine
from app.models import *

def create_tables():
    """创建所有表"""
    print("开始创建数据库表...")
    Base.metadata.create_all(bind=engine)
    print("数据库表创建完成！")


if __name__ == "__main__":
    create_tables()

