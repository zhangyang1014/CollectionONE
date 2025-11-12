import json
from typing import Any, Optional
from app.core.config import settings

try:
    import redis
    REDIS_AVAILABLE = True
except ImportError:
    REDIS_AVAILABLE = False


class RedisCache:
    """Redis缓存管理（可选）"""
    
    def __init__(self):
        if REDIS_AVAILABLE:
            try:
                self.redis_client = redis.Redis(
                    host=settings.REDIS_HOST,
                    port=settings.REDIS_PORT,
                    db=settings.REDIS_DB,
                    decode_responses=True
                )
                self.redis_client.ping()
            except Exception:
                self.redis_client = None
        else:
            self.redis_client = None
    
    def get(self, key: str) -> Optional[Any]:
        """获取缓存"""
        if not self.redis_client:
            return None
        try:
            value = self.redis_client.get(key)
            if value:
                return json.loads(value)
        except Exception:
            pass
        return None
    
    def set(self, key: str, value: Any, expire: int = 3600) -> bool:
        """设置缓存"""
        if not self.redis_client:
            return False
        try:
            return self.redis_client.setex(
                key,
                expire,
                json.dumps(value, ensure_ascii=False)
            )
        except Exception:
            return False
    
    def delete(self, key: str) -> bool:
        """删除缓存"""
        if not self.redis_client:
            return False
        try:
            return self.redis_client.delete(key) > 0
        except Exception:
            return False
    
    def delete_pattern(self, pattern: str) -> int:
        """删除匹配模式的所有键"""
        if not self.redis_client:
            return 0
        try:
            keys = self.redis_client.keys(pattern)
            if keys:
                return self.redis_client.delete(*keys)
        except Exception:
            pass
        return 0


cache = RedisCache()

