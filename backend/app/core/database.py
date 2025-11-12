from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker
from app.core.config import settings

# SQLite 需要特殊配置
connect_args = {}
engine_kwargs = {
    "echo": False
}

if settings.DATABASE_URL.startswith("sqlite"):
    connect_args = {"check_same_thread": False}
    # SQLite不需要连接池配置
    engine_kwargs.update({
        "connect_args": connect_args,
        "pool_pre_ping": False,
    })
else:
    # 非SQLite数据库使用连接池配置
    engine_kwargs.update({
        "pool_pre_ping": True,
        "pool_recycle": 3600,
    })

engine = create_engine(
    settings.DATABASE_URL,
    **engine_kwargs
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db():
    """数据库会话依赖"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

