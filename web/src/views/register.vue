<template>
  <div class="register-container">
    <section class="register-panel">
      <div class="panel-card">
        <h2 class="panel-title">创建账户</h2>
        <form class="register-form" @submit.prevent="onSubmit">
          <label class="form-item">
            <span>手机号</span>
            <input v-model.trim="phone" type="tel" placeholder="请输入手机号" required />
          </label>
          <label class="form-item">
            <span>密码</span>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </label>
          <label class="form-item">
            <span>确认密码</span>
            <input v-model="confirm" type="password" placeholder="请再次输入密码" required />
          </label>
          <div class="form-actions">
            <button type="submit" class="btn primary" :disabled="loading">
              {{ loading ? '创建中...' : '创建账户' }}
            </button>
          </div>
          <p v-if="error" class="form-error">{{ error }}</p>
          <p v-if="success" class="form-success">{{ success }}</p>
          <div class="form-foot">
            <RouterLink to="/login" class="link">返回登录</RouterLink>
          </div>
        </form>
      </div>
    </section>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { createUser } from '@/api/sysUser'

const phone = ref('')
const password = ref('')
const confirm = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

async function onSubmit() {
  error.value = ''
  success.value = ''
  if (!phone.value || !password.value || !confirm.value) {
    error.value = '请完整填写所有字段'
    return
  }
  if (password.value !== confirm.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    // 创建普通用户，服务端将默认用户名使用手机号并强制 role=USER
    await createUser({ phone: phone.value, password: password.value })
    success.value = '账户创建成功，正在跳转登录...'
    setTimeout(() => { window.location.href = '/login' }, 800)
  } catch (e: any) {
    error.value = e?.message || '创建失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fb;
}
.register-panel {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
}
.panel-card {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(47, 48, 53, 0.12);
  padding: 2rem 2rem 1.75rem;
}
.panel-title { margin: 0 0 1rem; font-size: 1.4rem; font-weight: 600; color: #1f2937; }
.register-form { display: flex; flex-direction: column; gap: 1rem; }
.form-item span { display: inline-block; margin-bottom: 0.5rem; font-size: 0.92rem; color: #374151; }
.form-item input { width: 100%; height: 40px; border: 1px solid #e5e7eb; border-radius: 10px; padding: 0 12px; font-size: 0.95rem; outline: none; }
.form-item input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15); }
.form-actions { margin-top: 0.5rem; }
.btn { width: 100%; height: 40px; border: none; border-radius: 10px; font-weight: 600; cursor: pointer; }
.btn.primary { background: #4f46e5; color: #fff; }
.btn.primary:disabled { opacity: 0.6; cursor: not-allowed; }
.form-error { margin-top: 0.5rem; color: #dc2626; font-size: 0.9rem; }
.form-success { margin-top: 0.5rem; color: #16a34a; font-size: 0.9rem; }
.form-foot { margin-top: 0.75rem; font-size: 0.92rem; }
.form-foot .link { color: #4f46e5; }
.form-foot .link:hover { text-decoration: underline; }
</style>