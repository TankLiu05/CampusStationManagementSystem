<template>
  <div class="admin-login">
    <!-- 左侧图片区域 (70%) -->
    <div class="image-section">
      <img src="@/components/icons/admin.png" alt="管理员登录" />
      
      <!-- 左上角系统标题 -->
      <div class="system-title">
        <h2>校园驿站管理系统</h2>
      </div>
      
      <!-- 中心主标题和副标题 -->
      <div class="hero-content">
        <h1 class="hero-title">高效运营<br>掌控全局物流</h1>
        <p class="hero-subtitle">通过集成化控制台管理您的快递仓库、分发流程及人员权限，实时监控每一个包裹的动态</p>
      </div>
      
      <!-- 左下角功能展示 -->
      <div class="feature-status">
        <span class="status-icon"><img src="@/components/icons/day.png" alt="日期" /></span>
        <span class="status-text">{{ currentDate }} · 系统运维正常运行中</span>
      </div>
    </div>
    
    <!-- 右侧登录表单区域 (30%) -->
    <div class="form-section">
      <div class="form-wrapper">
        <h1 class="title">管理员登录</h1>
        <p class="subtitle">请输入您的管理凭据以访问后台系统</p>
        
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">管理员账号</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/components/icons/user.png" alt="用户" /></span>
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
              <span class="label-text">安全密码</span>
              <a href="#" class="forgot-link">找回密码?</a>
            </div>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/components/icons/password.png" alt="密码" /></span>
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
              <span class="checkbox-label">保持30天登录状态</span>
            </label>
          </div>
          
          <button type="submit" class="login-btn">
            进入管理系统 
          </button>
        </form>
        
        <div class="footer-info">
          <span>安全中心</span>
          <span>•</span>
          <span>系统状态</span>
          <span>•</span>
          <span>联系技术支持</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const showPassword = ref(false)

// 获取当前日期，格式：2026年01月15日
const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
})

const handleLogin = () => {
  console.log('登录信息:', loginForm)
  // TODO: 实现登录逻辑
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.admin-login {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

/* 左侧图片区域 70% */
.image-section {
  flex: 7;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 20px;
  margin: 20px 10px 20px 20px;
  position: relative;
}

.image-section img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 左上角系统标题 */
.system-title {
  position: absolute;
  top: 60px;
  left: 60px;
  z-index: 10;
}

.system-title h2 {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 中心主标题和副标题容器 */
.hero-content {
  position: absolute;
  top: 50%;
  left: 60px;
  transform: translateY(-50%);
  text-align: left;
  z-index: 10;
  max-width: 80%;
}

.hero-title {
  font-size: 60px;
  font-weight: 820;
  color: white;
  margin: 0 0 24px 0;
  line-height: 1.3;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.hero-subtitle {
  font-size: 25px;
  font-weight: 450;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  line-height: 1.8;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  max-width: 600px;
}

/* 右侧表单区域 30% */
.form-section {
  flex: 3;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: white;
  border-radius: 20px;
  margin: 20px 20px 20px 10px;
}

.form-wrapper {
  width: 100%;
  max-width: 480px;
  padding: 0 20px;
}

.title {
  font-size: 42px;
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
  background: #1a1a2e;
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



/* 左下角功能展示 */
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
  width: 20px;
  height: 20px;
  object-fit: contain; 
  
 }


.status-text {
  font-size: 18px;
  font-weight: 500;
  color: white;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-login {
    flex-direction: column;
  }
  
  .image-section {
    flex: none;
    height: 30vh;
  }
  
  .form-section {
    flex: none;
    height: 70vh;
    padding: 30px 20px;
  }
}
</style>
