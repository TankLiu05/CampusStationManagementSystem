<template>
  <div class="user-login">
    <div class="form-section">
      <div class="form-wrapper">
        <h1 class="title">{{ isRegister ? '创建账号' : '欢迎回来' }}</h1>
        <p class="subtitle">
          {{ isRegister ? '注册后即可开始使用校园驿站系统' : '登录后即可签收快递并查看最新公告信息' }}
        </p>
        
        <!-- 登录表单 -->
        <form v-if="!isRegister" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">账号</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/user.png" alt="用户" /></span>
              <input
                type="text"
                id="username"
                v-model="loginForm.username"
                placeholder="输入账号"
                required
              />
            </div>
          </div>
          
          <div class="form-group">
            <div class="label-row">
              <span class="label-text">密码</span>
              <a href="#" class="forgot-link">忘记密码?</a>
            </div>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/password.png" alt="密码" /></span>
              <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="loginForm.password"
                placeholder="输入密码"
                required
              />
              <span class="toggle-password" @click="showPassword = !showPassword">
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
              </span>
            </div>
          </div>
          
          <div class="form-group remember">
            <label class="checkbox-wrapper">
              <input type="checkbox" v-model="loginForm.remember" />
              <span class="checkbox-label">保持7天登录状态</span>
            </label>
          </div>
          
          <button type="submit" class="login-btn">
            立即登录 
          </button>
        </form>
        
        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="reg-username">账号</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/user.png" alt="用户" /></span>
              <input
                type="text"
                id="reg-username"
                v-model="registerForm.username"
                placeholder="输入账号"
                required
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="reg-password">密码</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/password.png" alt="密码" /></span>
              <input
                :type="showPassword ? 'text' : 'password'"
                id="reg-password"
                v-model="registerForm.password"
                placeholder="设置密码（6位以上）"
                required
                minlength="6"
              />
              <span class="toggle-password" @click="showPassword = !showPassword">
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
              </span>
            </div>
          </div>
          
          <div class="form-group">
            <label for="reg-phone">手机号（可选）</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/phone.png" alt="手机" /></span>
              <input
                type="tel"
                id="reg-phone"
                v-model="registerForm.phone"
                placeholder="输入手机号"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label for="reg-email">邮箱（可选）</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/assets/icons/email.png" alt="邮箱" /></span>
              <input
                type="email"
                id="reg-email"
                v-model="registerForm.email"
                placeholder="输入邮箱地址"
              />
            </div>
          </div>
          
          <button type="submit" class="login-btn">
            立即注册
          </button>
        </form>
        
        <div class="footer-info">
          <span>•</span>
          <span class="toggle-mode" @click="isRegister = !isRegister">
            {{ isRegister ? '返回登录' : '注册账号' }}
          </span>
          <span>•</span>
        </div>
      </div>
    </div>
    
    <!-- 右侧图片区域 (50%) -->
    <div class="image-section">
      <img src="@/assets/icons/users.png" alt="用户登录" />
          
      <!-- 顶部主标题和副标题 -->
      <div class="hero-content">
        <h1 class="hero-title">随时随地<br>掌控您的包裹</h1>
        <p class="hero-subtitle">轻松追踪您的快递包裹状态,随时随地查询取件码,享受便捷的校园物流体验</p>
      </div>
          
      <!-- 中心图片 -->
      <div class="center-image">
        <img src="@/assets/icons/photo.png" alt="物流配送" />
      </div>
          
      <!-- 右下角功能展示 -->
      <div class="feature-status">
        <span class="status-icon"><img src="@/assets/icons/car.png" alt="物流" /></span>
        <span class="status-text">全链路追踪.为您提供最快的物流服务</span>
      </div>
          
      <!-- 右下角人物图片 -->
      <div class="corner-man">
        <img src="@/assets/icons/man.png" alt="人物" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/sysUser'
import { request } from '@/utls/request'
import { clearRoleCache } from '@/router'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const isRegister = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false,
})

const registerForm = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
})

const showPassword = ref(false)
const { success, error } = useToast()

const handleLogin = async () => {
  console.log('开始登录...')
  // 登录前清除旧的角色缓存
  clearRoleCache()
  
  try {
    const res = await login(loginForm.username, loginForm.password)
    console.log('登录响应:', JSON.stringify(res))
    
    // 处理后端可能返回的 ApiResponse 格式
    const resAny = res as any
    if (resAny && typeof resAny === 'object' && 'success' in resAny) {
      if (!resAny.success) {
        error(resAny.message || '登录失败')
        return
      }
    }
    
    // 登录成功，直接跳转到用户首页，路由守卫会自动判断角色并重定向
    console.log('登录成功，跳转中...')
    router.push('/user/home')
  } catch (err: any) {
    console.error('登录失败:', err)
    let msg = '登录失败，请检查账号或密码'
    if (err && err.message) {
      msg = err.message
    }
    error(msg)
  }
}

const handleRegister = async () => {
  try {
    const user = await register(registerForm)
    console.log('用户注册成功:', user)
    success('注册成功！请登录')
    // 注册成功后切换到登录界面
    isRegister.value = false
    // 清空注册表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.phone = ''
    registerForm.email = ''
  } catch (err) {
    console.error('用户注册失败:', err)
    error('注册失败，请检查信息或稍后再试')
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.user-login {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

/* 左侧表单区域 50% */
.form-section {
  flex: 5;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: #f5f5f5;
  border-radius: 20px;
  margin: 20px 0 20px 20px;
}

/* 右侧图片区域 50% */
.image-section {
  flex: 5;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 20px;
  margin: 20px 20px 20px 0;
  position: relative;
}

.image-section img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.system-title h2 {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 顶部主标题和副标题容器 */
.hero-content {
  position: absolute;
  top: 200px;
  left: 60px;
  text-align: left;
  z-index: 10;
  max-width: 80%;
}

.hero-title {
  font-size: 48px;
  font-weight: 820;
  color: white;
  margin: 0 0 20px 0;
  line-height: 1.3;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 20px;
  font-weight: 450;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  line-height: 1.8;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  max-width: 600px;
}

/* 中心图片 */
.center-image {
  position: absolute;
  top: 550px;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 5;
  width: 650px;
  height: 650px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.center-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.form-wrapper {
  width: 100%;
  max-width: 480px;
  padding: 0 20px;
}

.title {
  font-size: 50px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  text-align: center;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 17px;
  color: #999;
  text-align: center;
  margin-bottom: 48px;
  line-height: 1.6;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 17px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.label-text {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}

.forgot-link {
  font-size: 16px;
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover {
  text-decoration: underline;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
  color: #aaa;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-icon img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.input-wrapper input {
  width: 100%;
  padding: 18px 16px 18px 48px;
  font-size: 16px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  outline: none;
  background: #f8f9fa;
  transition: all 0.2s;
}

.input-wrapper input::placeholder {
  color: #bbb;
}

.toggle-password {
  position: absolute;
  right: 16px;
  font-size: 18px;
  color: #aaa;
  cursor: pointer;
  user-select: none;
}

.form-group.remember {
  margin-bottom: 28px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  cursor: pointer;
  flex-shrink: 0;
  vertical-align: middle;
}

.checkbox-label {
  font-size: 16px;
  color: #666;
  line-height: 18px;
}

.login-btn {
  width: 100%;
  padding: 18px;
  font-size: 18px;
  font-weight: 600;
  color: white;
  background: #10b981;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.footer-info {
  margin-top: 40px;
  text-align: center;
  font-size: 14px;
  color: #999;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.footer-info .toggle-mode {
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
}

.footer-info .toggle-mode:hover {
  text-decoration: underline;
}

/* 右下角功能展示 */
.feature-status {
  position: absolute;
  bottom: 60px;
  left: 60px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon {
  font-size: 10px;
  width: 30px;
  height: 30px;
  object-fit: contain; 
}

.status-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.status-text {
  font-size: 18px;
  font-weight: 500;
  color: white;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 右上角人物图片 */
.corner-man {
  position: absolute;
  top: 50px;
  right: 50px;
  z-index: 5;
  width: 240px;
  height: 240px;
}

.corner-man img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .hero-title {
    font-size: 36px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .center-image {
    width: 500px;
    height: 500px;
  }
}

@media (max-width: 992px) {
  .form-section {
    flex: 6;
  }
  
  .image-section {
    flex: 4;
  }
  
  .hero-content {
    top: 100px;
    left: 40px;
  }
  
  .hero-title {
    font-size: 28px;
  }
  
  .hero-subtitle {
    font-size: 14px;
    max-width: 400px;
  }
  
  .center-image {
    width: 350px;
    height: 350px;
    top: 400px;
  }
  
  .corner-man {
    width: 150px;
    height: 150px;
    top: 30px;
    right: 30px;
  }
  
  .feature-status {
    bottom: 30px;
    left: 30px;
  }
  
  .status-text {
    font-size: 14px;
  }
  
  .title {
    font-size: 36px;
  }
  
  .subtitle {
    font-size: 14px;
    margin-bottom: 30px;
  }
}

@media (max-width: 768px) {
  .user-login {
    flex-direction: column;
  }
  
  .form-section {
    flex: none;
    width: 100%;
    height: auto;
    min-height: 100vh;
    padding: 40px 20px;
    order: 1;
    margin: 0;
    border-radius: 0;
  }
  
  .image-section {
    display: none;
  }
  
  .form-wrapper {
    max-width: 100%;
    padding: 0 10px;
  }
  
  .title {
    font-size: 32px;
  }
  
  .subtitle {
    font-size: 14px;
    margin-bottom: 30px;
  }
  
  .input-wrapper input {
    padding: 14px 16px 14px 48px;
    font-size: 14px;
  }
  
  .login-btn {
    padding: 14px;
    font-size: 16px;
  }
  
  .footer-info {
    margin-top: 30px;
  }
}

@media (max-width: 480px) {
  .form-section {
    padding: 30px 15px;
  }
  
  .title {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 13px;
  }
  
  .form-group label,
  .label-text {
    font-size: 15px;
  }
  
  .input-wrapper input {
    padding: 12px 14px 12px 44px;
    font-size: 14px;
  }
  
  .login-btn {
    padding: 12px;
    font-size: 15px;
  }
  
  .forgot-link {
    font-size: 14px;
  }
  
  .checkbox-label {
    font-size: 14px;
  }
}
</style>
