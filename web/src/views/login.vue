<template>
  <div class="login-container">
    <section class="login-hero">
      <div class="hero-overlay">
        <h1 class="hero-title">校园驿站管理系统</h1>
        <p class="hero-subtitle">高效管理站点与包裹，提升校园物流体验</p>
        <div class="hero-art">
          <div class="circle c1" />
          <div class="circle c2" />
          <div class="circle c3" />
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="panel-card">
        <h2 class="panel-title">登录</h2>
        <form class="login-form" @submit.prevent="onSubmit">
          <label class="form-item">
            <span>手机号</span>
            <input v-model.trim="phone" type="tel" placeholder="请输入手机号" required />
          </label>

          <label class="form-item">
            <span>密码</span>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </label>

          <div class="form-actions">
            <button type="submit" class="btn primary" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>

          <p v-if="error" class="form-error">{{ error }}</p>
          <div class="form-foot">
            <span>还没有账户？</span>
            <RouterLink class="link" to="/register">创建账号</RouterLink>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { loginByPhone } from '@/api/sysUser'
const phone = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  if (!phone.value || !password.value) {
    error.value = '请输入手机号和密码'
    return
  }
  loading.value = true
  try {
    const user = await loginByPhone(phone.value, password.value)
    // TODO: 存储用户信息与登录态（如 token），当前返回用户对象
    console.log('登录成功', user)
    window.location.href = '/'
  } catch (e: any) {
    error.value = e?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fb;
}

.login-hero {
  flex: 0 0 70%;
  position: relative;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding: 6rem 5rem;
}

.hero-title {
  font-size: 2.4rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.hero-subtitle {
  margin-top: 0.75rem;
  font-size: 1rem;
  opacity: 0.92;
}

.hero-art {
  position: absolute;
  right: 6rem;
  bottom: 6rem;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.25;
}

.c1 { width: 160px; height: 160px; background: #fff; filter: blur(2px); }
.c2 { width: 90px; height: 90px; background: #fde68a; left: -40px; top: -20px; }
.c3 { width: 120px; height: 120px; background: #a7f3d0; left: 60px; top: 60px; }

.login-panel {
  flex: 0 0 30%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
}

.panel-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(47, 48, 53, 0.12);
  padding: 2rem 2rem 1.75rem;
}

.panel-title {
  margin: 0 0 1rem;
  font-size: 1.4rem;
  font-weight: 600;
  color: #1f2937;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-item span {
  display: inline-block;
  margin-bottom: 0.5rem;
  font-size: 0.92rem;
  color: #374151;
}

.form-item input {
  width: 100%;
  height: 40px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 12px;
  font-size: 0.95rem;
  outline: none;
}

.form-item input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.form-actions {
  margin-top: 0.5rem;
}

.btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}
.btn.primary {
  background: #4f46e5;
  color: #fff;
}
.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-error {
  margin-top: 0.5rem;
  color: #dc2626;
  font-size: 0.9rem;
}

.form-foot {
  margin-top: 0.75rem;
  font-size: 0.92rem;
  color: #6b7280;
}
.form-foot .link {
  margin-left: 0.35rem;
  color: #4f46e5;
}
.form-foot .link:hover {
  text-decoration: underline;
}

@media (max-width: 980px) {
  .login-hero { display: none; }
  .login-panel { flex: 1 1 auto; }
}
</style>