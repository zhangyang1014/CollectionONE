#!/usr/bin/env python3
"""
测试 MySQL 连接并创建数据库
"""
import pymysql
import sys
from getpass import getpass

def test_connection():
    """测试 MySQL 连接"""
    print("=" * 60)
    print("MySQL 连接测试")
    print("=" * 60)
    
    # 获取连接信息
    print("\n请输入 MySQL 连接信息:")
    host = input("主机 (默认: localhost): ").strip() or "localhost"
    port = input("端口 (默认: 3306): ").strip() or "3306"
    user = input("用户名 (默认: root): ").strip() or "root"
    password = getpass("密码 (如果没有密码,直接按回车): ")
    
    try:
        port = int(port)
    except ValueError:
        print("错误: 端口必须是数字")
        return False
    
    # 测试连接
    print(f"\n正在连接到 MySQL ({user}@{host}:{port})...")
    
    try:
        # 连接到 MySQL (不指定数据库)
        connection = pymysql.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            charset='utf8mb4'
        )
        
        print("✓ 连接成功!")
        
        # 创建数据库和用户
        with connection.cursor() as cursor:
            print("\n正在创建数据库和用户...")
            
            # 创建数据库
            cursor.execute("""
                CREATE DATABASE IF NOT EXISTS cco_system 
                CHARACTER SET utf8mb4 
                COLLATE utf8mb4_unicode_ci
            """)
            print("✓ 数据库 'cco_system' 创建成功")
            
            # 创建用户
            try:
                cursor.execute("""
                    CREATE USER IF NOT EXISTS 'cco_user'@'localhost' 
                    IDENTIFIED BY 'cco_password'
                """)
                print("✓ 用户 'cco_user' 创建成功")
            except pymysql.err.OperationalError as e:
                if "Operation CREATE USER failed" in str(e):
                    print("⚠️  用户 'cco_user' 已存在")
                else:
                    raise
            
            # 授权
            cursor.execute("""
                GRANT ALL PRIVILEGES ON cco_system.* TO 'cco_user'@'localhost'
            """)
            cursor.execute("FLUSH PRIVILEGES")
            print("✓ 权限授予成功")
            
            # 验证数据库
            cursor.execute("SHOW DATABASES LIKE 'cco_system'")
            result = cursor.fetchone()
            if result:
                print(f"✓ 验证: 数据库 '{result[0]}' 存在")
            
            # 验证用户
            cursor.execute("""
                SELECT User, Host FROM mysql.user 
                WHERE User = 'cco_user'
            """)
            result = cursor.fetchone()
            if result:
                print(f"✓ 验证: 用户 '{result[0]}'@'{result[1]}' 存在")
        
        connection.commit()
        connection.close()
        
        print("\n" + "=" * 60)
        print("✓ MySQL 设置完成!")
        print("=" * 60)
        print("\n数据库连接信息:")
        print(f"  主机: {host}")
        print(f"  端口: {port}")
        print(f"  数据库: cco_system")
        print(f"  用户: cco_user")
        print(f"  密码: cco_password")
        print("\n连接字符串:")
        print(f"  DATABASE_URL=mysql+pymysql://cco_user:cco_password@{host}:{port}/cco_system?charset=utf8mb4")
        
        # 创建 .env 文件
        print("\n正在创建 .env 文件...")
        with open('.env', 'w', encoding='utf-8') as f:
            f.write(f"""# 数据库配置 - MySQL
DATABASE_URL=mysql+pymysql://cco_user:cco_password@{host}:{port}/cco_system?charset=utf8mb4

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0

# JWT 配置
SECRET_KEY=dev-secret-key-please-change-in-production
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# API 配置
API_V1_STR=/api/v1
PROJECT_NAME=CCO System

# CORS 配置
BACKEND_CORS_ORIGINS=["http://localhost:5173", "http://localhost:3000"]
""")
        print("✓ .env 文件创建成功")
        
        print("\n下一步:")
        print("  1. 运行数据迁移: python3 migrate_to_mysql.py")
        print("  2. 重启后端服务: bash restart_backend.sh")
        
        return True
        
    except pymysql.err.OperationalError as e:
        print(f"\n✗ 连接失败: {e}")
        print("\n可能的原因:")
        print("  1. MySQL 服务未启动")
        print("  2. 用户名或密码错误")
        print("  3. 主机或端口配置错误")
        print("\n请检查:")
        print("  - MySQL 服务状态: brew services list | grep mysql")
        print("  - 启动 MySQL: brew services start mysql")
        return False
        
    except Exception as e:
        print(f"\n✗ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = test_connection()
    sys.exit(0 if success else 1)

