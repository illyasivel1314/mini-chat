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
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入工号"
            size="large"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入姓名"
            size="large"
            :prefix-icon="UserFilled"
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

        <el-form-item prop="position">
          <el-select
            v-model="form.position"
            placeholder="请选择岗位"
            size="large"
            style="width: 100%"
          >
            <el-option label="前端开发工程师" value="0" />
            <el-option label="Java开发工程师" value="1" />
            <el-option label="C开发工程师" value="2" />
            <el-option label="测试工程师" value="3" />
            <el-option label="其他" value="4" />
          </el-select>
        </el-form-item>

        <el-form-item prop="positionDescription">
          <el-input
            v-model="form.positionDescription"
            type="textarea"
            placeholder="请简述您的职责"
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
import { User, Lock, UserFilled, Plus, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const fileInputRef = ref(null)
const loading = ref(false)
const avatarPreview = ref('')
const selectedFile = ref(null)

const form = reactive({
  account: '',
  name: '',
  password: '',
  confirmPassword: '',
  position: '',
  positionDescription: '',
  avatar: ''
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
  account: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  position: [{ required: true, message: '请选择岗位', trigger: 'change' }]
}

// Re-validate confirmPassword when password changes
watch(() => form.password, () => {
  if (form.confirmPassword) {
    formRef.value?.validateField('confirmPassword')
  }
})

function triggerFileInput() {
  fileInputRef.value?.click()
}


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
    ElMessage.success('注册成功')
    router.push('/login')
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

/* ---- Avatar Upload ---- */
.avatar-upload {
  display: flex;
  justify-content: center;
  cursor: pointer;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 16px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
  font-size: 13px;
  transition: border-color 0.2s, color 0.2s;
}

.avatar-placeholder:hover {
  border-color: #07C160;
  color: #07C160;
}

.avatar-preview-wrapper {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.avatar-preview {
  display: block;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}

.avatar-preview-wrapper:hover .avatar-mask {
  opacity: 1;
}

.upload-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.upload-tip.success {
  color: #07C160;
}

/* ---- Footer ---- */
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
