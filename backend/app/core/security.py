from datetime import datetime, timedelta
from typing import Any, Union
from jose import jwt
from passlib.context import CryptContext
from app.core.config import settings
import base64
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.backends import default_backend

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# 生成加密密钥（基于SECRET_KEY）
def _get_fernet_key() -> bytes:
    """从SECRET_KEY生成Fernet密钥"""
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=b'cco_payment_salt',  # 固定盐值
        iterations=100000,
        backend=default_backend()
    )
    key = base64.urlsafe_b64encode(kdf.derive(settings.SECRET_KEY.encode()))
    return key

_fernet = Fernet(_get_fernet_key())


def create_access_token(subject: Union[str, Any], expires_delta: timedelta = None) -> str:
    """创建JWT访问令牌"""
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(
            minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES
        )
    to_encode = {"exp": expire, "sub": str(subject)}
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """验证密码"""
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """获取密码哈希"""
    return pwd_context.hash(password)


def encrypt_data(data: str) -> str:
    """
    加密敏感数据（如API密钥）
    
    Args:
        data: 要加密的数据
        
    Returns:
        加密后的Base64字符串
    """
    if not data:
        return data
    encrypted = _fernet.encrypt(data.encode())
    return base64.urlsafe_b64encode(encrypted).decode()


def decrypt_data(encrypted_data: str) -> str:
    """
    解密敏感数据
    
    Args:
        encrypted_data: 加密的Base64字符串
        
    Returns:
        解密后的原始数据
    """
    if not encrypted_data:
        return encrypted_data
    try:
        decoded = base64.urlsafe_b64decode(encrypted_data.encode())
        decrypted = _fernet.decrypt(decoded)
        return decrypted.decode()
    except Exception:
        # 如果解密失败，可能是未加密的数据，直接返回
        return encrypted_data

