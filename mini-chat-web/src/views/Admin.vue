<template>
  <div class="admin-page">
    <!-- 登录态：未登录显示登录表单 -->
    <div v-if="!loggedIn" class="admin-login-wrap">
      <div class="admin-login-card">
        <div class="admin-login-header">
          <h1>账号管理后台</h1>
          <p>Mini Chat Admin</p>
        </div>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="管理员账号"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="管理员密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="admin-login-btn"
            :loading="loggingIn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>
      </div>
    </div>

    <!-- 已登录：用户管理 -->
    <div v-else class="admin-main">
      <div class="admin-topbar">
        <div class="admin-title">
          <h2>账号管理</h2>
          <span class="admin-badge">管理员后台</span>
        </div>
        <div class="admin-topbar-actions">
          <el-input
            v-model="keyword"
            placeholder="按手机号/姓名/工号搜索"
            :prefix-icon="Search"
            clearable
            style="width: 280px"
            @keyup.enter="loadUsers"
            @clear="loadUsers"
          />
          <el-button type="primary" @click="loadUsers">查询</el-button>
          <el-button @click="handleLogout">退出</el-button>
        </div>
      </div>

      <div class="admin-table-wrap">
        <el-table :data="users" v-loading="loading" stripe>
          <el-table-column label="用户" min-width="180">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="32" :src="row.avatar" shape="square">
                  {{ (row.name || '').slice(0, 2) }}
                </el-avatar>
                <span class="user-cell-name">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="130" />
          <el-table-column prop="account" label="工号" min-width="100">
            <template #default="{ row }">{{ row.account || '—' }}</template>
          </el-table-column>
          <el-table-column label="岗位" min-width="100">
            <template #default="{ row }">{{ positionText(row.position) }}</template>
          </el-table-column>
          <el-table-column label="职责" min-width="100">
            <template #default="{ row }">{{ dutyText(row.duty) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.deleted ? 'danger' : 'success'" size="small">
                {{ row.deleted ? '已注销' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="handleReset(row)">
                重置密码
              </el-button>
              <el-button
                v-if="!row.deleted"
                size="small"
                text
                type="danger"
                @click="handleDelete(row)"
              >
                注销
              </el-button>
              <el-button
                v-else
                size="small"
                text
                type="success"
                @click="handleRestore(row)"
              >
                恢复
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!loading && users.length === 0" class="admin-empty">
          暂无账号
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Search } from '@element-plus/icons-vue'
import adminRequest from '@/utils/adminRequest.js'

const loggedIn = ref(!!localStorage.getItem('adminToken'))
const loggingIn = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}

const keyword = ref('')
const users = ref([])
const loading = ref(false)

const POSITION_MAP = {
  0: '负责人', 1: 'Java 开发', 2: '前端开发',
  3: 'C 端开发', 4: '三方测试', 5: '内部测试'
}
const DUTY_MAP = { 0: '项目负责人', 1: '开发员', 2: '测试员' }

const positionText = (v) => POSITION_MAP[v] ?? '未知'
const dutyText = (v) => DUTY_MAP[v] ?? '未知'

async function handleLogin() {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  loggingIn.value = true
  try {
    const token = await adminRequest.post('/admin/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    localStorage.setItem('adminToken', token)
    loggedIn.value = true
    ElMessage.success('登录成功')
    loadUsers()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loggingIn.value = false
  }
}

function handleLogout() {
  localStorage.removeItem('adminToken')
  loggedIn.value = false
  users.value = []
  loginForm.password = ''
}

async function loadUsers() {
  loading.value = true
  try {
    users.value = await adminRequest.get('/admin/user/list', {
      params: { keyword: keyword.value }
    })
  } catch (e) {
    users.value = []
  } finally {
    loading.value = false
  }
}

async function handleReset(row) {
  try {
    await ElMessageBox.confirm(
      `确定将「${row.name}」的密码重置为 123456 吗？`,
      '重置密码',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await adminRequest.post('/admin/user/reset', null, { params: { userId: row.id } })
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要注销「${row.name}」的账号吗？注销后该账号将无法登录。`,
      '注销账号',
      { confirmButtonText: '确定注销', cancelButtonText: '取消', type: 'warning' }
    )
    await adminRequest.post('/admin/user/delete', null, { params: { userId: row.id } })
    ElMessage.success('已注销')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('注销失败')
    }
  }
}

async function handleRestore(row) {
  try {
    await ElMessageBox.confirm(
      `确定要恢复「${row.name}」的账号吗？`,
      '恢复账号',
      { confirmButtonText: '确定恢复', cancelButtonText: '取消', type: 'info' }
    )
    await adminRequest.post('/admin/user/restore', null, { params: { userId: row.id } })
    ElMessage.success('已恢复')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('恢复失败')
    }
  }
}

onMounted(() => {
  if (loggedIn.value) {
    loadUsers()
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f5f6f8;
}

/* ---- 登录 ---- */
.admin-login-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b4a6f 100%);
}

.admin-login-card {
  width: 380px;
  padding: 40px 36px 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.25);
}

.admin-login-header {
  text-align: center;
  margin-bottom: 28px;
}

.admin-login-header h1 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 6px;
}

.admin-login-header p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.admin-login-btn {
  width: 100%;
  margin-top: 4px;
}

/* ---- 主界面 ---- */
.admin-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.admin-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-title h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.admin-badge {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.admin-topbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-table-wrap {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-cell-name {
  font-size: 14px;
  color: #303133;
}

.admin-empty {
  padding: 60px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
