<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          :class="['toast', `toast-${t.type}`]"
          @click="removeToast(t.id)"
        >
          <span class="toast-message">{{ t.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'

const { toasts, removeToast } = useToast()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04);
  font-size: 14px;
  font-weight: 500;
  min-width: 280px;
  max-width: 400px;
  pointer-events: auto;
  cursor: pointer;
}

.toast:hover {
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.12),
    0 3px 6px rgba(0, 0, 0, 0.06);
}

.toast-message {
  flex: 1;
  word-break: break-word;
  line-height: 1.5;
}

/* 成功样式 - 绿色主题 */
.toast-success {
  background: linear-gradient(to right, #d1fae5, #ffffff);
}

.toast-success .toast-message {
  color: #065f46;
}

/* 错误样式 - 红色主题 */
.toast-error {
  background: linear-gradient(to right, #fee2e2, #ffffff);
}

.toast-error .toast-message {
  color: #991b1b;
}

/* 警告样式 - 橙色主题 */
.toast-warning {
  background: linear-gradient(to right, #fef3c7, #ffffff);
}

.toast-warning .toast-message {
  color: #92400e;
}

/* 信息样式 - 灰色主题 */
.toast-info {
  background: linear-gradient(to right, #e5e5e5, #ffffff);
}

.toast-info .toast-message {
  color: #333333;
}

/* 即时显示，无动画 */
.toast-enter-active,
.toast-leave-active,
.toast-move {
  transition: none;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .toast-container {
    top: 16px;
    right: 16px;
    left: 16px;
  }

  .toast {
    min-width: auto;
    max-width: none;
    padding: 12px 16px;
    font-size: 13px;
  }
}
</style>
