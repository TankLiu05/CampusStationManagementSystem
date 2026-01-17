<template>
  <UserLayout>
    <div class="pickup-code">
    <div class="page-header">
      <h1>取件码</h1>
      <p>查看您的包裹取件码</p>
    </div>

    <!-- 当前可用取件码 -->
    <div class="current-codes">
      <h2>当前可用取件码</h2>
      <div v-if="availableCodes.length === 0" class="empty-state">
        <div class="empty-icon"><img src="@/assets/icons/12.png" alt="暂无取件码" /></div>
        <p>暂无可用取件码</p>
      </div>
      
      <div v-else class="codes-grid">
        <div class="code-card" v-for="code in availableCodes" :key="code.id">
          <div class="code-header">
            <span class="code-company">{{ code.company }}</span>
            <span class="code-expire">剩余 {{ code.remainDays }} 天</span>
          </div>
          <div class="code-main">
            <div class="code-number">{{ code.pickupCode }}</div>
            <button class="copy-btn" @click="copyCode(code.pickupCode)"><img src="@/assets/icons/8.png" alt="复制" style="width: 16px; height: 16px; vertical-align: middle; margin-right: 4px;" /> 复制</button>
          </div>
          <div class="code-info">
            <div class="info-item">
              <span class="label">快递单号：</span>
              <span class="value">{{ code.trackingNumber }}</span>
            </div>
            <div class="info-item">
              <span class="label">存放位置：</span>
              <span class="value">{{ code.location }}</span>
            </div>
            <div class="info-item">
              <span class="label">到达时间：</span>
              <span class="value">{{ code.arrivalTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用说明 -->
    <div class="usage-guide">
      <h2>使用说明</h2>
      <div class="guide-content">
        <div class="guide-item">
          <div class="guide-number">1</div>
          <div class="guide-text">
            <h3>获取取件码</h3>
            <p>包裹到达驿站后，系统会自动生成取件码并发送通知</p>
          </div>
        </div>
        <div class="guide-item">
          <div class="guide-number">2</div>
          <div class="guide-text">
            <h3>前往驿站</h3>
            <p>携带取件码前往校园驿站，根据位置提示找到对应货架</p>
          </div>
        </div>
        <div class="guide-item">
          <div class="guide-number">3</div>
          <div class="guide-text">
            <h3>完成取件</h3>
            <p>向工作人员出示取件码或在自助取件机上输入取件码</p>
          </div>
        </div>
      </div>
    </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'

interface PickupCode {
  id: string
  pickupCode: string
  company: string
  trackingNumber: string
  location: string
  arrivalTime: string
  remainDays: number
}

const availableCodes = ref<PickupCode[]>([])
// TODO: 从后端获取取件码数据

const copyCode = (code: string) => {
  navigator.clipboard.writeText(code).then(() => {
    alert('取件码已复制到剪贴板')
  }).catch(err => {
    console.error('复制失败:', err)
  })
}
</script>

<style scoped>
.pickup-code {
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.current-codes {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.current-codes h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.codes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.code-card {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
}

.code-card:hover {
  border-color: #666;
  box-shadow: 0 4px 12px rgba(102, 102, 102, 0.15);
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.code-company {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.code-expire {
  font-size: 13px;
  color: #fa8c16;
  background: #fff7e6;
  padding: 4px 10px;
  border-radius: 10px;
}

.code-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #666 0%, #555 100%);
  border-radius: 10px;
}

.code-number {
  font-size: 32px;
  font-weight: 700;
  color: white;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
}

.copy-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.code-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  font-size: 13px;
}

.label {
  color: #999;
}

.value {
  color: #333;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 60px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

.usage-guide {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.usage-guide h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.guide-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.guide-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.guide-number {
  width: 25px;
  height: 25px;
  background: linear-gradient(135deg, #666 0%, #555 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
}

.guide-text h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.guide-text p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
