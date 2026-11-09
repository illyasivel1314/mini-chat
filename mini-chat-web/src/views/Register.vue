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
        <h1 class="login-title">创建账号</h1>
        <p class="login-subtitle">注册 Mini Chat 账号，开始聊天</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入姓名（2-20 字符）"
            size="large"
            :prefix-icon="UserFilled"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码（6-20 位）"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入工号（可选）"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="position">
          <el-select
            v-model="form.position"
            placeholder="请选择岗位"
            size="large"
            style="width: 100%"
          >
            <el-option label="负责人" value="0" />
            <el-option label="Java 开发" value="1" />
            <el-option label="前端开发" value="2" />
            <el-option label="C 端开发" value="3" />
            <el-option label="三方测试" value="4" />
            <el-option label="内部测试" value="5" />
          </el-select>
        </el-form-item>

        <el-form-item prop="duty">
          <el-select
            v-model="form.duty"
            placeholder="请选择职责"
            size="large"
            style="width: 100%"
          >
            <el-option label="项目负责人" value="0" />
            <el-option label="开发员" value="1" />
            <el-option label="测试员" value="2" />
          </el-select>
        </el-form-item>

        <el-form-item prop="detail">
          <el-input
            v-model="form.detail"
            type="textarea"
            placeholder="详细信息（可选）"
            :rows="2"
          />
        </el-form-item>

        <el-form-item prop="positionDescription">
          <el-input
            v-model="form.positionDescription"
            type="textarea"
            placeholder="请简述您的职责（可选）"
            :rows="3"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="success"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>已有账号？</span>
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '',
  name: '',
  password: '',
  confirmPassword: '',
  account: '',
  position: '',
  duty: '',
  detail: '',
  positionDescription: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度需为 2-20 字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  account: [{ max: 20, message: '工号长度不能超过 20 字符', trigger: 'blur' }],
  position: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  duty: [{ required: true, message: '请选择职责', trigger: 'change' }]
}

// Re-validate confirmPassword when password changes
watch(() => form.password, () => {
  if (form.confirmPassword) {
    formRef.value?.validateField('confirmPassword')
  }
})

async function handleRegister() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await auth.register(form)
    ElMessage.success('注册成功，正在进入聊天页')
    router.push('/chatWindow')
  } catch (e) {
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
  width: 440px;
  padding: 40px 40px 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: #07C160;
  border-radius: 14px;
  margin-bottom: 14px;
}

.login-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px;
}

.login-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 4px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
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
  margin-top: 4px;
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
