from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from app.core.config import settings
import traceback

app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json"
)

# CORS配置 - 必须在最前面，确保所有响应都包含CORS头
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
)

# 全局异常处理，确保错误响应也包含CORS头
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """全局异常处理器，确保错误响应也包含CORS头"""
    error_detail = str(exc)
    if hasattr(exc, 'detail'):
        error_detail = exc.detail
    
    # 打印错误详情到控制台（用于调试）
    print(f"❌ 错误: {error_detail}")
    print(f"📍 路径: {request.url.path}")
    traceback.print_exc()
    
    return JSONResponse(
        status_code=500,
        content={
            "detail": error_detail,
            "path": request.url.path,
            "error_type": type(exc).__name__
        }
)


@app.get("/")
async def root():
    return {"message": "CCO System API", "version": "1.0.0"}


@app.get("/health")
async def health_check():
    return {"status": "healthy"}


# 导入API路由
from app.api import (
    field_groups, standard_fields, custom_fields,
    tenants, cases, channel, auth, agencies, team_groups, teams, agency_working_hours,
    notification_config, public_notification, notification_template, field_display, queue,
    # 权限管理API已迁移到Java后端
    # 数据看板API
    communications, ptp, quality_inspections, performance, analytics, alerts, idle_monitor,
    # Infinity外呼系统API
    infinity_config, infinity_extension, infinity_call,
    # 还款渠道和还款码API
    payment_channels, payment_codes
)
from app.api import im_auth, im_face

# 注册API路由
app.include_router(field_groups.router, prefix=settings.API_V1_STR)
app.include_router(standard_fields.router, prefix=settings.API_V1_STR)
app.include_router(custom_fields.router, prefix=settings.API_V1_STR)
app.include_router(tenants.router, prefix=settings.API_V1_STR)
app.include_router(cases.router, prefix=settings.API_V1_STR)
app.include_router(channel.router, prefix=settings.API_V1_STR)
app.include_router(auth.router, prefix=settings.API_V1_STR)
app.include_router(agencies.router, prefix=settings.API_V1_STR)
app.include_router(queue.router, prefix=settings.API_V1_STR)
app.include_router(team_groups.router, prefix=settings.API_V1_STR)
app.include_router(teams.router, prefix=settings.API_V1_STR)
app.include_router(agency_working_hours.router, prefix=settings.API_V1_STR)
app.include_router(notification_config.router, prefix=settings.API_V1_STR)
app.include_router(public_notification.router, prefix=settings.API_V1_STR)
app.include_router(notification_template.router, prefix=settings.API_V1_STR)
app.include_router(field_display.router, prefix=settings.API_V1_STR)
# app.include_router(permissions.router)  # 权限管理路由已迁移到Java后端
app.include_router(im_auth.router, prefix=settings.API_V1_STR)  # IM端认证路由
app.include_router(im_face.router, prefix=settings.API_V1_STR)  # IM端人脸识别路由

# 数据看板API路由（这些路由已经在router定义中包含了/api/v1前缀）
app.include_router(communications.router)
app.include_router(ptp.router)
app.include_router(quality_inspections.router)
app.include_router(performance.router)
app.include_router(analytics.router)
app.include_router(alerts.router)
app.include_router(idle_monitor.router, prefix=f"{settings.API_V1_STR}/idle-monitor", tags=["空闲催员监控"])

# Infinity外呼系统API路由
app.include_router(infinity_config.router, prefix=settings.API_V1_STR)
app.include_router(infinity_extension.router, prefix=settings.API_V1_STR)
app.include_router(infinity_call.router, prefix=settings.API_V1_STR)

# 还款渠道和还款码API路由
app.include_router(payment_channels.router)  # 管理控台-还款渠道配置
app.include_router(payment_codes.router)  # IM端-还款码管理

