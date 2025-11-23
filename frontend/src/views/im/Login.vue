<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="48"><ChatDotRound /></el-icon>
        </div>
        <h1 class="title">CCO-IM 催收工作台</h1>
        <p class="subtitle">Collection Operator Instant Messaging</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="tenantId">
          <el-input
            v-model="loginForm.tenantId"
            placeholder="请输入机构ID"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><OfficeBuilding /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="collectorId">
          <el-input
            v-model="loginForm.collectorId"
            placeholder="请输入催员ID"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              size="large"
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><PictureFilled /></el-icon>
              </template>
            </el-input>
            <div class="captcha-image" @click="refreshCaptcha">
              <canvas ref="captchaCanvas" width="120" height="40"></canvas>
              <div class="captcha-refresh">
                <el-icon><Refresh /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <!-- 人脸识别区域 -->
          <div class="face-recognition-section">
            <div class="face-camera-container">
              <video
                ref="videoRef"
                autoplay
                playsinline
                class="face-video"
                v-show="showCamera && !capturedImage"
              ></video>
              <canvas
                ref="canvasRef"
                class="face-canvas"
                v-show="false"
              ></canvas>
              <div
                v-if="capturedImage"
                class="face-preview"
              >
                <img :src="capturedImage" alt="人脸照片" />
                <el-button
                  type="danger"
                  size="small"
                  :icon="Refresh"
                  @click="retakePhoto"
                  class="retake-btn"
                >
                  重拍
                </el-button>
              </div>
              <div
                v-if="!showCamera && !capturedImage"
                class="face-placeholder"
                @click="startCamera"
              >
                <el-icon :size="48"><Camera /></el-icon>
                <p>点击开始人脸识别</p>
                <p v-if="!checkCameraSupport()" class="camera-warning">
                  ⚠️ 浏览器不支持摄像头，可跳过人脸识别直接登录
                </p>
              </div>
            </div>
            <div class="face-status">
              <el-text v-if="faceDetecting" type="info" size="small">
                <el-icon class="is-loading"><Loading /></el-icon>
                正在识别人脸...
              </el-text>
              <el-text v-else-if="faceId" type="success" size="small">
                <el-icon><Check /></el-icon>
                人脸识别成功 (ID: {{ faceId }})
              </el-text>
              <el-text v-else-if="faceError" type="danger" size="small">
                <el-icon><Close /></el-icon>
                {{ faceError }}
              </el-text>
              <el-text v-else type="info" size="small">
                请先完成人脸识别（开发测试模式：可跳过）
              </el-text>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 提示信息 -->
      <div class="login-footer">
        <el-alert
          title="测试账号"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="test-accounts">
              <p><strong>BTQ:</strong> 催员ID: BTQ001, 密码: 123456</p>
              <p><strong>BTSK:</strong> 催员ID: BTSK001, 密码: 123456</p>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="copyright">
      <p>CCO Collection System © 2025</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  OfficeBuilding,
  User,
  Lock,
  PictureFilled,
  Refresh,
  Camera,
  Loading,
  Check,
  Close
} from '@element-plus/icons-vue'
import { useImUserStore } from '@/stores/imUser'
import { detectFace, uploadLoginFace } from '@/api/im'

const router = useRouter()
const route = useRoute()
const imUserStore = useImUserStore()
const loginFormRef = ref()
const captchaCanvas = ref<HTMLCanvasElement>()
const videoRef = ref<HTMLVideoElement>()
const canvasRef = ref<HTMLCanvasElement>()
const loading = ref(false)
let captchaText = ''

// 人脸识别相关
const showCamera = ref(false)
const capturedImage = ref('')
const faceId = ref('')
const faceDetecting = ref(false)
const faceError = ref('')
let stream: MediaStream | null = null

// 登录表单
const loginForm = reactive({
  tenantId: '',
  collectorId: '',
  password: '',
  captcha: ''
})

// 表单验证规则
const loginRules = {
  tenantId: [
    { required: true, message: '请输入机构ID', trigger: 'blur' }
  ],
  collectorId: [
    { required: true, message: '请输入催员ID', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

// 生成验证码
const generateCaptcha = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let text = ''
  for (let i = 0; i < 4; i++) {
    text += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return text
}

// 绘制验证码
const drawCaptcha = () => {
  if (!captchaCanvas.value) return
  
  const canvas = captchaCanvas.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  captchaText = generateCaptcha()

  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 背景
  ctx.fillStyle = '#f5f5f5'
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  // 绘制干扰线
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = `rgba(37, 211, 102, ${Math.random() * 0.3 + 0.1})`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }

  // 绘制验证码文字
  ctx.font = 'bold 24px Arial'
  ctx.textBaseline = 'middle'
  
  for (let i = 0; i < captchaText.length; i++) {
    ctx.fillStyle = '#25D366'
    ctx.save()
    const x = 20 + i * 25
    const y = 20
    const angle = (Math.random() - 0.5) * 0.4
    ctx.translate(x, y)
    ctx.rotate(angle)
    ctx.fillText(captchaText[i], 0, 0)
    ctx.restore()
  }

  // 绘制干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(37, 211, 102, ${Math.random() * 0.5})`
    ctx.beginPath()
    ctx.arc(
      Math.random() * canvas.width,
      Math.random() * canvas.height,
      1,
      0,
      2 * Math.PI
    )
    ctx.fill()
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  drawCaptcha()
}

// 检测是否支持摄像头API
const checkCameraSupport = () => {
  return !!(navigator.mediaDevices && navigator.mediaDevices.getUserMedia)
}

// 启动摄像头
const startCamera = async () => {
  try {
    faceError.value = ''
    
    // 检查浏览器支持
    if (!checkCameraSupport()) {
      throw new Error('浏览器不支持摄像头API，请使用现代浏览器（Chrome、Firefox、Safari等）')
    }
    
    stream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user' // 前置摄像头
      }
    })
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
      showCamera.value = true
      
      // 监听视频加载完成
      videoRef.value.addEventListener('loadedmetadata', () => {
        setupAutoCapture()
      }, { once: true })
    }
  } catch (error: any) {
    console.error('启动摄像头失败:', error)
    
    // 根据错误类型提供更友好的提示
    let errorMessage = '无法访问摄像头，请检查权限设置'
    
    if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      errorMessage = '摄像头权限被拒绝，请在浏览器设置中允许摄像头权限'
    } else if (error.name === 'NotFoundError') {
      errorMessage = '未找到摄像头设备'
    } else if (error.name === 'NotReadableError') {
      errorMessage = '摄像头被其他应用占用，请关闭其他应用后重试'
    } else if (error.message) {
      errorMessage = error.message
    }
    
    faceError.value = errorMessage
    
    // 开发测试模式：不阻止用户继续，只显示警告
    ElMessage.warning({
      message: errorMessage + '（开发测试模式：可以跳过人脸识别直接登录）',
      duration: 5000
    })
  }
}

// 停止摄像头
const stopCamera = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  showCamera.value = false
}

// 拍照
const capturePhoto = async () => {
  if (!videoRef.value || !canvasRef.value) return
  
  try {
    const video = videoRef.value
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    
    if (!ctx) return
    
    // 设置canvas尺寸
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    
    // 绘制当前视频帧到canvas
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    // 转换为base64
    const imageData = canvas.toDataURL('image/jpeg', 0.8)
    capturedImage.value = imageData
    
    // 停止摄像头
    stopCamera()
    
    // 开始人脸检测
    await detectFaceFromImage(imageData)
  } catch (error) {
    console.error('拍照失败:', error)
    ElMessage.error('拍照失败，请重试')
  }
}

// 从图片检测人脸（开发测试模式：失败不影响登录）
const detectFaceFromImage = async (imageData: string) => {
  try {
    faceDetecting.value = true
    faceError.value = ''
    
    // 直接使用base64字符串调用人脸检测API
    // imRequest 的响应拦截器已经返回了 response.data，所以这里直接是 { face_id: string }
    const response = await detectFace(imageData) as { face_id?: string }
    
    if (response?.face_id) {
      faceId.value = response.face_id
      ElMessage.success('人脸识别成功')
    } else {
      // 开发测试模式：不抛出错误，静默处理
      console.warn('⚠️ 人脸检测未返回face_id，但不影响登录（开发测试模式）')
      faceId.value = ''
    }
  } catch (error: any) {
    // 开发测试模式：人脸检测失败不影响登录，只记录日志
    console.warn('⚠️ 人脸检测失败，但不影响登录（开发测试模式）:', error.message)
    faceError.value = ''
    faceId.value = ''
    // 不清空照片，允许用户继续尝试或直接登录
  } finally {
    faceDetecting.value = false
  }
}

// 重拍照片
const retakePhoto = () => {
  capturedImage.value = ''
  faceId.value = ''
  faceError.value = ''
  startCamera()
}

// 监听视频流，自动拍照（当用户正对摄像头时）
const setupAutoCapture = () => {
  if (!videoRef.value) return
  
  // 3秒后自动拍照
  setTimeout(() => {
    if (showCamera.value && !capturedImage.value) {
      capturePhoto()
    }
  }, 3000)
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    
    // 验证码校验
    if (loginForm.captcha.toUpperCase() !== captchaText.toUpperCase()) {
      ElMessage.error('验证码错误')
      refreshCaptcha()
      loginForm.captcha = ''
      return
    }

    // 人脸识别为可选项（开发测试模式）
    // 如果没有人脸识别，给出提示但仍允许登录
    if (!faceId.value || !capturedImage.value) {
      console.log('⚠️ 跳过人脸识别（开发测试模式）')
    }

    loading.value = true

    // 调用登录API
    await imUserStore.login({
      tenantId: loginForm.tenantId,
      collectorId: loginForm.collectorId,
      password: loginForm.password
    })

    // 登录成功后上传人脸记录（如果有的话）
    if (capturedImage.value && faceId.value) {
      try {
        await uploadLoginFace({
          collector_id: loginForm.collectorId,
          tenant_id: loginForm.tenantId,
          face_image: capturedImage.value,
          face_id: faceId.value,
          login_time: new Date().toISOString()
        })
      } catch (error) {
        console.error('上传人脸记录失败:', error)
        // 不影响登录流程，仅记录错误
      }
    }

    ElMessage.success('登录成功')
    
    // 停止摄像头
    stopCamera()
    
    // 确保状态已更新，使用 nextTick 等待 Vue 响应式更新完成
    await nextTick()
    
    // 再次确认登录状态（从 localStorage 恢复，确保路由守卫能读取到）
    if (typeof imUserStore.initFromStorage === 'function') {
      imUserStore.initFromStorage()
    }
    
    console.log('[Login] 登录成功，准备跳转，当前状态:', {
      isLoggedIn: imUserStore.isLoggedIn,
      hasToken: !!imUserStore.token,
      hasUser: !!imUserStore.user,
      token: imUserStore.token?.substring(0, 20) + '...',
      user: imUserStore.user
    })
    
    // 再次检查 localStorage
    const storedToken = localStorage.getItem('im_token')
    const storedUser = localStorage.getItem('im_user')
    console.log('[Login] localStorage 检查:', {
      hasStoredToken: !!storedToken,
      hasStoredUser: !!storedUser,
      storedTokenLength: storedToken?.length || 0
    })
    
    // 跳转到工作台
    console.log('[Login] 开始跳转到 /im/workspace')
    router.push('/im/workspace').then(() => {
      console.log('[Login] 路由跳转成功')
    }).catch((error) => {
      console.error('[Login] 路由跳转失败:', error)
    })
  } catch (error: any) {
    console.error('登录失败:', error)
    if (error !== false) { // 不是表单验证错误
      ElMessage.error(error.message || '登录失败，请检查账号密码')
      refreshCaptcha()
      loginForm.captcha = ''
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  drawCaptcha()
  
  // 检测URL参数，如果是模拟登录，自动填充表单并登录
  const collectorId = route.query.collectorId as string
  const tenantId = route.query.tenantId as string
  const simulate = route.query.simulate as string
  
  if (simulate === 'true' && collectorId && tenantId) {
    // 自动填充表单
    loginForm.collectorId = collectorId
    loginForm.tenantId = tenantId
    // 使用默认密码（模拟登录）
    loginForm.password = '123456'
    
    // 延迟自动登录，等待验证码绘制完成
    setTimeout(() => {
      // 自动填充验证码（模拟登录时跳过验证码验证）
      loginForm.captcha = captchaText
      // 模拟登录时跳过人脸识别
      faceId.value = 'MOCK_FACE_ID'
      capturedImage.value = 'data:image/jpeg;base64,/9j/4AAQSkZJRg=='
      // 自动执行登录
      handleSimulateLogin()
    }, 500)
  }
})

// 组件卸载时清理
onUnmounted(() => {
  stopCamera()
})

// 模拟登录（跳过验证码验证）
const handleSimulateLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 跳过验证码校验（模拟登录）
    loading.value = true

    // 调用登录API
    await imUserStore.login({
      tenantId: loginForm.tenantId,
      collectorId: loginForm.collectorId,
      password: loginForm.password
    })

    ElMessage.success('模拟登录成功')
    
    // 跳转到工作台
    router.push('/im/workspace')
  } catch (error: any) {
    console.error('模拟登录失败:', error)
    ElMessage.error(error.message || '模拟登录失败，请检查账号密码')
    refreshCaptcha()
    loginForm.captcha = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5e9 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(37, 211, 102, 0.15);
  padding: 40px 32px;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #25D366 0%, #20ba5a 100%);
  border-radius: 20px;
  color: white;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #25D366 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #25D366 inset !important;
}

.login-form :deep(.el-input__prefix) {
  color: #25D366;
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-image {
  position: relative;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  transition: transform 0.2s;
}

.captcha-image:hover {
  transform: scale(1.02);
}

.captcha-image:hover .captcha-refresh {
  opacity: 1;
}

.captcha-refresh {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(37, 211, 102, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: white;
  font-size: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #25D366 0%, #20ba5a 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
  transition: all 0.3s;
}

.login-button:hover {
  background: linear-gradient(135deg, #20ba5a 0%, #1da851 100%);
  box-shadow: 0 6px 16px rgba(37, 211, 102, 0.4);
  transform: translateY(-2px);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 24px;
}

.login-footer :deep(.el-alert) {
  background-color: #f0f9ff;
  border: 1px solid #d1e7ff;
}

.login-footer :deep(.el-alert__icon) {
  color: #25D366;
}

.test-accounts {
  font-size: 13px;
  line-height: 1.8;
}

.test-accounts p {
  margin: 4px 0;
  color: #606266;
}

.test-accounts strong {
  color: #25D366;
  font-weight: 600;
}

.copyright {
  margin-top: 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.copyright p {
  margin: 0;
}

/* 人脸识别区域样式 */
.face-recognition-section {
  width: 100%;
  margin-bottom: 20px;
}

.face-camera-container {
  position: relative;
  width: 100%;
  max-width: 320px;
  height: 240px;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  border: 2px solid #e4e7ed;
}

.face-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 镜像显示 */
}

.face-canvas {
  display: none;
}

.face-preview {
  position: relative;
  width: 100%;
  height: 100%;
}

.face-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 镜像显示 */
}

.retake-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
}

.face-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.face-placeholder:hover {
  background: #ecf5ff;
  color: #409eff;
}

.face-placeholder p {
  margin: 10px 0 0 0;
  font-size: 14px;
}

.face-placeholder .camera-warning {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}

.face-status {
  text-align: center;
  margin-top: 10px;
  min-height: 24px;
}

.face-status .el-icon {
  margin-right: 5px;
}

.face-status .is-loading {
  animation: rotating 1s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

