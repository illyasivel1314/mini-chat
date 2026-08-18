<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 64 64" width="48" height="48">
            <rect width="64" height="64" rx="14" fill="#fff"/>
            <path d="M18 22c0-2.2 1.8-4 4-4h20c2.2 0 4 1.8 4 4v16c0 2.2-1.8 4-4 4H29l-8 6V42h-1c-2.2 0-4-1.8-4-4V22z" fill="#07C160"/>
          </svg>
        </div>
        <h1 class="login-title">Mini Chat</h1>
        <p class="login-subtitle">在线聊天，随时随地</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="success"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>

      <div class="login-hint">
        <p>演示账号：任意用户名 + 任意密码即可登录</p>
        <p>建议使用：张三、李四、王五</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await auth.login(form.username, form.password)
    router.push('/chatWindow')
  } catch (e) {
    // Error handled by interceptor
    console.log(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

.login-card {
  width: 400px;
  padding: 48px 40px 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: #07C160;
  border-radius: 16px;
  margin-bottom: 18px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 8px;
}

.login-btn {
  width: 100%;
  --el-button-bg-color: #07C160;
  --el-button-border-color: #07C160;
  --el-button-hover-bg-color: #06ad56;
  --el-button-hover-border-color: #06ad56;
  --el-button-active-bg-color: #059a4c;
  --el-button-active-border-color: #059a4c;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-hint {
  margin-top: 24px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.login-hint p {
  margin: 2px 0;
  font-size: 12px;
  color: #909399;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.login-footer a {
  color: #07C160;
  margin-left: 4px;
}
</style>
