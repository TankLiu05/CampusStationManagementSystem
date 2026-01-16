<template>
  <div class="user-login">
    <div class="form-section">
      <div class="form-wrapper">
        <h1 class="title">欢迎回来</h1>
        <p class="subtitle">登录后即可签收快递并查看最新公告信息</p>
        
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">学号</label>
            <div class="input-wrapper">
              <span class="input-icon"><img src="@/components/icons/user.png" alt="用户" /></span>
              <input
                type="text"
                id="username"
                v-model="loginForm.username"
                placeholder="输入学号/手机号"
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
              <span class="checkbox-label">保持7天登录状态</span>
            </label>
          </div>
          
          <button type="submit" class="login-btn">
            立即登录 
          </button>
        </form>
        
        <div class="footer-info">
          <span>帮助中心</span>
          <span>•</span>
          <span>注册账号</span>
          <span>•</span>
          <span>联系客服</span>
        </div>
      </div>
    </div>
    
    <!-- 右侧图片区域 (50%) -->
    <div class="image-section">
      <img src="@/components/icons/users.png" alt="用户登录" />
      
      <!-- 顶部主标题和副标题 -->
      <div class="hero-content">
        <h1 class="hero-title">随时随地<br>掌控您的包裹</h1>
        <p class="hero-subtitle">轻松追踪您的快递包裹状态,随时随地查询取件码,享受便捷的校园物流体验</p>
      </div>
      
      <!-- 中心图片 -->
      <div class="center-image">
        <img src="@/components/icons/photo.png" alt="物流配送" />
      </div>
      
      <!-- 右下角功能展示 -->
      <div class="feature-status">
        <span class="status-icon"><img src="@/components/icons/car.png" alt="物流" /></span>
        <span class="status-text">全链路追踪.为您提供最快的物流服务</span>
      </div>
      
      <!-- 右下角人物图片 -->
      <div class="corner-man">
        <img src="@/components/icons/man.png" alt="人物" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { loginByPhone } from '@/api/sysUser'

const loginForm = reactive({
  username: '',
  password: '',
  remember: false,
})

const showPassword = ref(false)

const handleLogin = async () => {
  try {
    const user = await loginByPhone(loginForm.username, loginForm.password)
    console.log('用户登录成功:', user)
  } catch (err) {
    console.error('用户登录失败:', err)
    window.alert('登录失败，请检查账号或密码')
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
@media (max-width: 768px) {
  .user-login {
    flex-direction: column;
  }
  
  .form-section {
    flex: none;
    height: 60vh;
    padding: 30px 20px;
    order: 1;
  }
  
  .image-section {
    flex: none;
    height: 40vh;
    order: 2;
  }
}
</style>
