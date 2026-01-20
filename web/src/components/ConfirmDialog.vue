<template>
  <Teleport to="body">
    <div v-if="state.visible" class="confirm-overlay" @click="handleCancel">
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-header">
          <span :class="['confirm-icon', state.type]">{{ getIcon(state.type) }}</span>
          <h3 class="confirm-title">{{ state.title }}</h3>
        </div>
        <div class="confirm-body">
          <p class="confirm-message">{{ state.message }}</p>
        </div>
        <div class="confirm-footer">
          <button class="confirm-btn cancel" @click="handleCancel">
            {{ state.cancelText }}
          </button>
          <button :class="['confirm-btn', 'primary', state.type]" @click="handleConfirm">
            {{ state.confirmText }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { useConfirm } from '@/composables/useConfirm'

const { state, handleConfirm, handleCancel } = useConfirm()

const getIcon = (type: string) => {
  const icons: Record<string, string> = {
    info: '?',
    warning: '!',
    danger: '✕',
  }
  return icons[type] || '?'
}
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.confirm-dialog {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 400px;
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.confirm-header {
  padding: 28px 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
}

/* 信息类型 - 灰色 */
.confirm-icon.info {
  background: linear-gradient(135deg, #808080 0%, #666666 100%);
  color: white;
}

/* 警告类型 - 橙色 */
.confirm-icon.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

/* 危险类型 - 红色 */
.confirm-icon.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.confirm-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  text-align: center;
}

.confirm-body {
  padding: 0 24px 20px;
}

.confirm-message {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  text-align: center;
  margin: 0;
  word-break: break-word;
}

.confirm-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
}

.confirm-btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}

.confirm-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn.cancel:hover {
  background: #e8e8e8;
}

.confirm-btn.primary {
  color: white;
}

/* 信息按钮 */
.confirm-btn.primary.info {
  background: linear-gradient(135deg, #808080 0%, #666666 100%);
}

.confirm-btn.primary.info:hover {
  background: linear-gradient(135deg, #666666 0%, #555555 100%);
}

/* 警告按钮 */
.confirm-btn.primary.warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.confirm-btn.primary.warning:hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
}

/* 危险按钮 */
.confirm-btn.primary.danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.confirm-btn.primary.danger:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
}

/* 响应式适配 */
@media (max-width: 480px) {
  .confirm-dialog {
    width: 92%;
    max-width: none;
    margin: 0 16px;
  }

  .confirm-header {
    padding: 24px 20px 12px;
  }

  .confirm-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }

  .confirm-body {
    padding: 0 20px 16px;
  }

  .confirm-footer {
    padding: 12px 20px 20px;
    flex-direction: column-reverse;
  }

  .confirm-btn {
    padding: 14px 20px;
  }
}
</style>
