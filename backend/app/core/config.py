try:
    from pydantic_settings import BaseSettings, SettingsConfigDict
    PYDANTIC_V2 = True
except ImportError:
    from pydantic import BaseSettings
    PYDANTIC_V2 = False

from typing import List
import json
import os


class Settings(BaseSettings):
    # Database
    DATABASE_URL: str = "sqlite:///./cco_test.db"
    
    # Redis
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    
    # JWT
    SECRET_KEY: str = "dev-secret-key-please-change-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # API
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "CCO System"
    
    # CORS
    BACKEND_CORS_ORIGINS: List[str] = ["http://localhost:5173", "http://localhost:3000"]
    
    if PYDANTIC_V2:
        model_config = SettingsConfigDict(
            env_file=".env",
            case_sensitive=True,
            extra='ignore'
        )
    else:
        class Config:
            env_file = ".env"
            case_sensitive = True
            
            @classmethod
            def parse_env_var(cls, field_name: str, raw_val: str):
                if field_name == "BACKEND_CORS_ORIGINS":
                    try:
                        return json.loads(raw_val)
                    except:
                        return [raw_val]
                return raw_val


settings = Settings()

