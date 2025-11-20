"""IM端人脸识别API"""
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from pydantic import BaseModel
from datetime import datetime
import hashlib
import base64
from app.core.database import get_db

router = APIRouter(prefix="/im/face", tags=["IM端人脸识别"])


class FaceDetectResponse(BaseModel):
    """人脸检测响应"""
    code: int = 200
    message: str = "检测成功"
    face_id: str


class LoginFaceRecordRequest(BaseModel):
    """登录人脸记录请求"""
    collector_id: str
    tenant_id: str
    face_image: str  # base64 图片
    face_id: str
    login_time: str


class LoginFaceRecordResponse(BaseModel):
    """登录人脸记录响应"""
    code: int = 200
    message: str = "上传成功"


@router.post("/detect")
async def detect_face(
    image: UploadFile = File(..., description="人脸图片文件")
):
    """
    人脸检测接口
    接收图片文件，返回人脸ID
    当前实现为模拟版本，返回基于时间戳和图片哈希的人脸ID
    """
    try:
        # 读取图片内容
        image_content = await image.read()
        
        # 生成图片哈希（用于生成唯一的人脸ID）
        image_hash = hashlib.md5(image_content).hexdigest()[:8]
        
        # 生成人脸ID（格式：FACE_YYYYMMDD_HHMMSS_HASH）
        # 在实际应用中，这里应该调用第三方人脸识别服务
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        face_id = f"FACE_{timestamp}_{image_hash}"
        
        # 返回格式与前端期望一致：{ face_id: string }
        # 响应拦截器会处理 code/message，这里直接返回 face_id
        return {
            "face_id": face_id
        }
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"人脸检测失败: {str(e)}"
        )


@router.post("/login-record", response_model=LoginFaceRecordResponse)
async def upload_login_face_record(
    record_data: LoginFaceRecordRequest,
    db: Session = Depends(get_db)
):
    """
    上传登录人脸记录
    保存催员登录时的人脸识别记录
    当前实现仅记录日志，后续可扩展为保存到数据库
    """
    try:
        # 验证数据
        if not record_data.collector_id or not record_data.tenant_id:
            raise HTTPException(
                status_code=400,
                detail="催员ID和机构ID不能为空"
            )
        
        if not record_data.face_id:
            raise HTTPException(
                status_code=400,
                detail="人脸ID不能为空"
            )
        
        # 记录日志（后续可扩展为保存到数据库）
        print(f"📸 登录人脸记录:")
        print(f"  - 催员ID: {record_data.collector_id}")
        print(f"  - 机构ID: {record_data.tenant_id}")
        print(f"  - 人脸ID: {record_data.face_id}")
        print(f"  - 登录时间: {record_data.login_time}")
        print(f"  - 图片大小: {len(record_data.face_image)} bytes (base64)")
        
        # TODO: 后续实现数据库保存逻辑
        # 可以创建 collector_login_face_records 表来存储这些记录
        
        return {
            "code": 200,
            "message": "上传成功"
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"上传登录记录失败: {str(e)}"
        )

