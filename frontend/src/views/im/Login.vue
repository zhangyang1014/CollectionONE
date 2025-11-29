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
        <el-form-item prop="tenantId" label="">
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

        <el-form-item prop="collectorId" label="">
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

        <el-form-item prop="password" label="">
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

        <el-form-item prop="captcha" label="">
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

        <el-form-item label="">
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

        <el-form-item label="">
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
import { ref, reactive, watch, onMounted, onUnmounted, nextTick } from 'vue'
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

// 监听collectorId变化，确保始终是字符串类型
watch(() => loginForm.collectorId, (newVal) => {
  if (Array.isArray(newVal)) {
    const firstItem = newVal[0]
    if (typeof firstItem === 'string') {
      loginForm.collectorId = firstItem
    } else if (firstItem && typeof firstItem === 'object' && 'fieldValue' in firstItem) {
      loginForm.collectorId = String(firstItem.fieldValue || '')
    } else {
      loginForm.collectorId = ''
    }
  } else if (typeof newVal !== 'string' && newVal !== null && newVal !== undefined) {
    if (typeof newVal === 'object' && 'fieldValue' in newVal) {
      loginForm.collectorId = String((newVal as any).fieldValue || '')
    } else {
      loginForm.collectorId = String(newVal || '')
    }
  }
}, { immediate: true })

// 表单验证规则
const loginRules = {
  tenantId: [
    { required: true, message: '请输入机构ID', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        // 确保值是字符串，不是数组
        if (Array.isArray(value)) {
          callback(new Error('机构ID不能是数组'))
        } else if (typeof value !== 'string' && value !== '') {
          callback(new Error('机构ID格式错误'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  collectorId: [
    { required: true, message: '请输入催员ID', trigger: 'blur' },
    { 
      validator: (rule: any, value: any, callback: any) => {
        // 处理数组类型（可能是URL参数或验证错误对象）
        if (Array.isArray(value)) {
          const firstItem = value[0]
          if (typeof firstItem === 'string' && firstItem.trim()) {
            // 字符串数组，使用第一个元素
            callback()
          } else if (firstItem && typeof firstItem === 'object' && 'fieldValue' in firstItem) {
            // 验证错误对象数组，检查fieldValue
            if (firstItem.fieldValue && String(firstItem.fieldValue).trim()) {
              callback()
            } else {
              callback(new Error('请输入催员ID'))
            }
          } else {
            callback(new Error('请输入催员ID'))
          }
        } else if (typeof value !== 'string') {
          // 非字符串类型，尝试转换
          if (value && typeof value === 'object' && 'fieldValue' in value) {
            // 验证错误对象
            if (value.fieldValue && String(value.fieldValue).trim()) {
              callback()
            } else {
              callback(new Error('请输入催员ID'))
            }
          } else if (!value || String(value).trim() === '') {
            callback(new Error('请输入催员ID'))
          } else {
            // 其他类型，尝试转换为字符串
            callback()
          }
        } else if (!value || value.trim() === '') {
          callback(new Error('请输入催员ID'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [
    // 暂时禁用验证码校验，允许为空（测试模式）
    // { required: true, message: '请输入验证码', trigger: 'blur' },
    // { len: 4, message: '验证码为4位', trigger: 'blur' }
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
    // 确保表单值是字符串，不是数组（防止URL参数导致的问题）
    // 同时处理可能的验证错误对象被错误赋值的情况
    if (Array.isArray(loginForm.tenantId)) {
      loginForm.tenantId = (typeof loginForm.tenantId[0] === 'string' ? loginForm.tenantId[0] : '') || ''
    } else if (typeof loginForm.tenantId !== 'string') {
      loginForm.tenantId = String(loginForm.tenantId || '')
    }
    
    if (Array.isArray(loginForm.collectorId)) {
      // 如果数组元素是对象（验证错误对象），忽略它
      const firstItem = loginForm.collectorId[0]
      if (typeof firstItem === 'string') {
        loginForm.collectorId = firstItem
      } else if (firstItem && typeof firstItem === 'object' && firstItem.fieldValue !== undefined) {
        // 如果是验证错误对象，使用fieldValue或清空
        loginForm.collectorId = String(firstItem.fieldValue || '')
      } else {
        loginForm.collectorId = ''
      }
    } else if (typeof loginForm.collectorId !== 'string') {
      // 如果不是字符串也不是数组，转换为字符串
      if (loginForm.collectorId && typeof loginForm.collectorId === 'object' && 'fieldValue' in loginForm.collectorId) {
        loginForm.collectorId = String((loginForm.collectorId as any).fieldValue || '')
      } else {
        loginForm.collectorId = String(loginForm.collectorId || '')
      }
    }
    
    if (Array.isArray(loginForm.password)) {
      loginForm.password = (typeof loginForm.password[0] === 'string' ? loginForm.password[0] : '') || ''
    } else if (typeof loginForm.password !== 'string') {
      loginForm.password = String(loginForm.password || '')
    }
    
    await loginFormRef.value.validate()
    
    // 暂时禁用验证码校验（测试模式）
    // if (loginForm.captcha.toUpperCase() !== captchaText.toUpperCase()) {
    //   ElMessage.error('验证码错误')
    //   refreshCaptcha()
    //   loginForm.captcha = ''
    //   return
    // }

    // 人脸识别为可选项（开发测试模式）
    // 如果没有人脸识别，给出提示但仍允许登录
    if (!faceId.value || !capturedImage.value) {
      console.log('⚠️ 跳过人脸识别（开发测试模式）')
    }

    loading.value = true

    // 确保传递给API的值是字符串
    const tenantId = Array.isArray(loginForm.tenantId) ? loginForm.tenantId[0] : String(loginForm.tenantId || '')
    const collectorId = Array.isArray(loginForm.collectorId) ? loginForm.collectorId[0] : String(loginForm.collectorId || '')
    const password = Array.isArray(loginForm.password) ? loginForm.password[0] : String(loginForm.password || '')

    // 调用登录API
    await imUserStore.login({
      tenantId: tenantId,
      collectorId: collectorId,
      password: password
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
    
    // 再次等待一个tick，确保状态完全同步
    await nextTick()
    
    // 验证状态是否已正确设置
    const storedToken = localStorage.getItem('im_token')
    const storedUser = localStorage.getItem('im_user')
    
    console.log('[Login] 登录成功，准备跳转，当前状态:', {
      isLoggedIn: imUserStore.isLoggedIn,
      hasToken: !!imUserStore.token,
      hasUser: !!imUserStore.user,
      token: imUserStore.token?.substring(0, 20) + '...',
      user: imUserStore.user,
      localStorageToken: !!storedToken,
      localStorageUser: !!storedUser,
      storedTokenLength: storedToken?.length || 0
    })
    
    // 如果状态未正确设置，再次尝试同步
    if (!imUserStore.isLoggedIn && storedToken && storedUser) {
      console.warn('[Login] ⚠️ 状态未同步，再次尝试恢复')
      if (typeof imUserStore.initFromStorage === 'function') {
        imUserStore.initFromStorage()
        await nextTick()
      }
    }
    
    // 最终验证：确保localStorage有数据
    if (!storedToken || !storedUser) {
      console.error('[Login] ❌ localStorage数据丢失，无法跳转')
      ElMessage.error('登录状态保存失败，请重新登录')
      return
    }
    
    // 跳转到工作台（使用replace避免返回登录页）
    console.log('[Login] 开始跳转到 /im/workspace')
    router.replace('/im/workspace').then(() => {
      console.log('[Login] 路由跳转成功')
    }).catch((error) => {
      console.error('[Login] 路由跳转失败:', error)
      // 如果跳转失败，尝试使用push
      router.push('/im/workspace')
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
  // 处理URL参数可能是数组的情况（Vue Router会将同名参数转为数组）
  const getQueryParam = (param: string | string[] | undefined): string => {
    if (!param) return ''
    if (Array.isArray(param)) {
      return param[0] || ''
    }
    return param
  }
  
  const collectorId = getQueryParam(route.query.collectorId)
  const tenantId = getQueryParam(route.query.tenantId)
  const simulate = getQueryParam(route.query.simulate)
  
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

