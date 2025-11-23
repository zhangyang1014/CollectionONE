<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="48"><Setting /></el-icon>
        </div>
        <h1 class="title">CCO 管理控台</h1>
        <p class="subtitle">Collection Control Office</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="loginId">
          <el-input
            v-model="loginForm.loginId"
            placeholder="请输入登录ID"
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

      <!-- 底部链接 -->
      <div class="login-footer">
        <div class="footer-links">
          <el-link type="primary" underline="never" @click="handleForgotPassword">
            忘记密码
          </el-link>
          <el-divider direction="vertical" />
          <el-link type="primary" underline="never" @click="goToImLogin">
            切换到IM端登录
          </el-link>
        </div>
      </div>
    </div>

    <!-- 版权信息 -->
    <div class="copyright">
      <p>CCO Collection System © 2025</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting,
  User,
  Lock,
  PictureFilled,
  Refresh
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { adminLogin } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const captchaCanvas = ref<HTMLCanvasElement>()
const loading = ref(false)
let captchaText = ''

// 登录表单
const loginForm = reactive({
  loginId: '',
  password: '',
  captcha: ''
})

// 表单验证规则
const loginRules = {
  loginId: [
    { required: true, message: '请输入登录ID', trigger: 'blur' }
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
    ctx.strokeStyle = `rgba(64, 158, 255, ${Math.random() * 0.3 + 0.1})`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }

  // 绘制验证码文字
  ctx.font = 'bold 24px Arial'
  ctx.textBaseline = 'middle'
  
  for (let i = 0; i < captchaText.length; i++) {
    ctx.fillStyle = '#409eff'
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
    ctx.fillStyle = `rgba(64, 158, 255, ${Math.random() * 0.5})`
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

    loading.value = true

    // 调用登录API（request工具已经返回response.data）
    const response: any = await adminLogin({
      loginId: loginForm.loginId,
      password: loginForm.password
    })

    // 处理响应数据
    if (response.code === 200 && response.data) {
      // 保存用户信息和token
      const userInfo = response.data.user
      const token = response.data.token
      
      // 先保存到localStorage（确保路由守卫能立即读取到）
      userStore.setToken(token)
      userStore.setUserInfo(userInfo)

      ElMessage.success('登录成功')
      
      // 确保状态已更新，使用 nextTick 等待 Vue 响应式更新完成
      await nextTick()
      
      // 再次确认登录状态（从 localStorage 恢复，确保路由守卫能读取到）
      if (typeof userStore.initFromStorage === 'function') {
        userStore.initFromStorage()
      }
      
      console.log('[Admin Login] 登录成功，准备跳转，当前状态:', {
        hasToken: !!userStore.token,
        hasUserInfo: !!userStore.userInfo
      })
      
      // 跳转到工作台
      router.push('/dashboard').then(() => {
        console.log('[Admin Login] 路由跳转成功')
      }).catch((error) => {
        console.error('[Admin Login] 路由跳转失败:', error)
      })
    } else {
      throw new Error(response.message || '登录失败')
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    if (error !== false) { // 不是表单验证错误
      const errorMessage = error.response?.data?.detail || error.response?.data?.message || error.message || '登录失败，请检查账号密码'
      ElMessage.error(errorMessage)
      refreshCaptcha()
      loginForm.captcha = ''
    }
  } finally {
    loading.value = false
  }
}

// 忘记密码
const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能暂未实现')
}

// 切换到IM端登录
const goToImLogin = () => {
  window.location.href = '/im/login'
}

onMounted(() => {
  drawCaptcha()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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
  box-shadow: 0 0 0 1px #409eff inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset !important;
}

.login-form :deep(.el-input__prefix) {
  color: #409eff;
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
  border: 1px solid #e4e7ed;
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
  background: rgba(64, 158, 255, 0.8);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

.login-button:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-2px);
}

.login-button:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.copyright {
  margin-top: 24px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.copyright p {
  margin: 0;
}
</style>

