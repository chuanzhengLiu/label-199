<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-info">
        <div class="logo-circle">
          <el-icon :size="40" color="#2E5BBA"><Monitor /></el-icon>
        </div>
        <h1 class="system-title">公司内部资产管理系统</h1>
        <p class="system-desc">高效 · 安全 · 便捷的企业资产管家</p>
      </div>
      <div class="decoration-circle"></div>
      <div class="decoration-circle-small"></div>
    </div>
    
    <div class="login-right">
      <div class="login-form-wrapper">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号和密码</p>
        
        <el-form :model="form" @submit.prevent="handleLogin" size="large" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password 
              class="custom-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" class="submit-btn" :loading="loading">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <span>忘记密码？请联系管理员</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Lock, Monitor } from '@element-plus/icons-vue'

const router = useRouter()
const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', form.value)
    if (res.data.code === 200) {
      localStorage.setItem('user', JSON.stringify(res.data.data))
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #2E5BBA 0%, #003399 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  color: white;
  overflow: hidden;
}

.login-info {
  z-index: 2;
  text-align: center;
  animation: fadeInUp 1s ease-out;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.system-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
  letter-spacing: 2px;
}

.system-desc {
  font-size: 16px;
  opacity: 0.8;
  font-weight: 300;
}

.decoration-circle {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  top: -100px;
  left: -100px;
}

.decoration-circle-small {
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  bottom: 50px;
  right: 50px;
}

.login-right {
  flex: 1;
  background: white;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-form-wrapper {
  width: 400px;
  padding: 40px;
  animation: fadeIn 1.2s ease-out;
}

.form-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-subtitle {
  color: #999;
  margin-bottom: 40px;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 12px 15px;
  border-radius: 8px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2E5BBA inset !important;
}

.submit-btn {
  width: 100%;
  padding: 22px 0;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
  background: linear-gradient(90deg, #2E5BBA, #4a7bd8);
  border: none;
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 91, 186, 0.3);
}

.submit-btn:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-top: 20px;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  .login-right {
    flex: 1;
  }
}
</style>
