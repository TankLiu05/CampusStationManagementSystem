<template>
  <Teleport to="body">
    <div v-if="state.visible" class="confirm-overlay" @click="handleCancel">
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-header">
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

// 图标已移除
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
  padding: 24px 24px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
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

/* 信息按钮 - 莫兰迪灰蓝色 */
.confirm-btn.primary.info {
  background: #8D9B9F;
}

.confirm-btn.primary.info:hover {
  background: #7A8A8E;
}

/* 警告按钮 - 莫兰迪棕橙色 */
.confirm-btn.primary.warning {
  background: #C9A68C;
}

.confirm-btn.primary.warning:hover {
  background: #B8957B;
}

/* 危险按钮 - 莫兰迪红色 */
.confirm-btn.primary.danger {
  background: #C17C74;
}

.confirm-btn.primary.danger:hover {
  background: #B06B63;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .confirm-dialog {
    width: 92%;
    max-width: none;
    margin: 0 16px;
  }

  .confirm-header {
    padding: 20px 20px 10px;
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
