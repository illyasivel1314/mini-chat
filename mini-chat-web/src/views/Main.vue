<template>
  <div class="main-layout">
    <!-- Left Sidebar -->
    <aside class="sidebar">
      <!-- Current user header -->
      <div class="sidebar-header">
        <div class="user-info">
          <el-avatar :size="36" :src="auth.user?.avatar" class="user-avatar">
            {{ auth.userName.slice(0, 2) }}
          </el-avatar>
          <span class="user-name">{{ auth.userName }}</span>
        </div>
        <el-dropdown trigger="click" @command="handleUserMenu">
          <span class="menu-trigger">
            <el-icon><MoreFilled /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="createGroup">
                <el-icon><Plus /></el-icon>
                创建群聊
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- Search -->
      <div class="search-box">
        <el-input
          v-model="searchText"
          placeholder="搜索"
          size="default"
          :prefix-icon="Search"
          clearable
        />
      </div>

      <!-- Tabs -->
      <div class="sidebar-tabs">
        <div
          :class="['tab-item', { active: activeTab === 'messages' }]"
          @click="activeTab = 'messages'"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>消息</span>
        </div>
        <div
          :class="['tab-item', { active: activeTab === 'friends' }]"
          @click="activeTab = 'friends'"
        >
          <el-icon><UserFilled /></el-icon>
          <span>好友</span>
        </div>
      </div>

      <!-- Tab content -->
      <div class="sidebar-content">
        <MessageList
          v-show="activeTab === 'messages'"
          :search-text="searchText"
        />
        <FriendList
          v-show="activeTab === 'friends'"
          :search-text="searchText"
        />
      </div>
    </aside>

    <!-- Right Chat Area -->
    <main class="chat-area">
      <template v-if="chatStore.activeChat">
        <ChatWindow />
        <ChatInput />
      </template>
      <div v-else class="no-chat-placeholder">
        <div class="placeholder-icon">
          <svg viewBox="0 0 64 64" width="96" height="96">
            <rect width="64" height="64" rx="14" fill="#e8e8e8"/>
            <path d="M18 22c0-2.2 1.8-4 4-4h20c2.2 0 4 1.8 4 4v16c0 2.2-1.8 4-4 4H29l-8 6V42h-1c-2.2 0-4-1.8-4-4V22z" fill="#ccc"/>
          </svg>
        </div>
        <p class="placeholder-text">选择一条消息开始聊天</p>
      </div>
    </main>

    <!-- Create Group Dialog -->
    <el-dialog
      v-model="showCreateGroup"
      title="创建群聊"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="create-group-form">
        <div class="form-item">
          <label class="form-label">群聊名称</label>
          <el-input
            v-model="groupName"
            placeholder="请输入群聊名称"
            maxlength="30"
            show-word-limit
          />
        </div>
        <div class="form-item">
          <label class="form-label">
            选择好友
            <span class="selected-count">已选 {{ selectedFriendIds.length }} 人</span>
          </label>
          <div class="friend-select-list">
            <div
              v-for="friend in friendStore.friends"
              :key="friend.id"
              :class="['friend-select-item', { checked: selectedFriendIds.includes(friend.id) }]"
              @click="toggleFriendSelect(friend.id)"
            >
              <el-checkbox
                :model-value="selectedFriendIds.includes(friend.id)"
                @click.stop="toggleFriendSelect(friend.id)"
              />
              <el-avatar :size="36" :src="friend.avatar" shape="square">
                {{ friend.name.slice(0, 2) }}
              </el-avatar>
              <span class="friend-select-name">{{ friend.name }}</span>
            </div>
            <div v-if="friendStore.friends.length === 0" class="no-friends-tip">
              暂无好友，请先添加好友
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCreateGroup = false">取消</el-button>
        <el-button
          type="success"
          :disabled="!groupName.trim() || selectedFriendIds.length === 0"
          :loading="creatingGroup"
          @click="handleCreateGroup"
        >
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, MoreFilled, SwitchButton, ChatDotRound, UserFilled, Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth.js'
import { useChatStore } from '@/stores/chat.js'
import { useFriendStore } from '@/stores/friend.js'
import { useWebSocket } from '@/utils/webSocket';
import MessageList from '@/components/MessageList.vue'
import FriendList from '@/components/FriendList.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import ChatInput from '@/components/ChatInput.vue'

const router = useRouter()
const auth = useAuthStore()
const chatStore = useChatStore()
const friendStore = useFriendStore()
const webSocketStore = useWebSocket()

const activeTab = ref('messages')
const searchText = ref('')

// ---- Create Group ----
const showCreateGroup = ref(false)
const groupName = ref('')
const selectedFriendIds = ref([])
const creatingGroup = ref(false)

function toggleFriendSelect(friendId) {
  const idx = selectedFriendIds.value.indexOf(friendId)
  if (idx === -1) {
    selectedFriendIds.value.push(friendId)
  } else {
    selectedFriendIds.value.splice(idx, 1)
  }
}

async function handleCreateGroup() {
  if (!groupName.value.trim() || selectedFriendIds.value.length === 0) return
  creatingGroup.value = true
  try {
    await chatStore.createGroup(groupName.value.trim(), selectedFriendIds.value)
    ElMessage.success('群聊创建成功')
    showCreateGroup.value = false
    groupName.value = ''
    selectedFriendIds.value = []
    // Refresh conversations to show the new group
    await chatStore.loadConversations()
  } catch (e) {
    ElMessage.error('创建群聊失败')
  } finally {
    creatingGroup.value = false
  }
}

onMounted(() => {
  chatStore.loadConversations()
  friendStore.loadFriends()
  webSocketStore.connect()
})

function handleUserMenu(command) {
  if (command === 'createGroup') {
    showCreateGroup.value = true
  } else if (command === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

/* ---- Sidebar ---- */
.sidebar {
  width: 320px;
  min-width: 320px;
  background: #f0f0f0;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  background: #f0f0f0;
  border-bottom: 1px solid #e0e0e0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  flex-shrink: 0;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.menu-trigger {
  cursor: pointer;
  font-size: 18px;
  color: #606266;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.menu-trigger:hover {
  background: #e0e0e0;
}

/* ---- Search ---- */
.search-box {
  padding: 12px 18px;
  background: #f0f0f0;
}

.search-box :deep(.el-input__wrapper) {
  background: #e0e0e0;
  border-radius: 6px;
  box-shadow: none;
}

.search-box :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #07C160 inset;
}

/* ---- Tabs ---- */
.sidebar-tabs {
  display: flex;
  background: #f0f0f0;
  border-bottom: 1px solid #e0e0e0;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 0;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #07C160;
}

.tab-item.active {
  color: #07C160;
  border-bottom-color: #07C160;
  font-weight: 500;
}

/* ---- Sidebar content ---- */
.sidebar-content {
  flex: 1;
  overflow: hidden;
}

/* ---- Chat Area ---- */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  min-width: 0;
}

.no-chat-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.placeholder-icon {
  margin-bottom: 20px;
  opacity: 0.5;
}

.placeholder-text {
  font-size: 15px;
  color: #909399;
}

/* ---- Create Group Dialog ---- */
.create-group-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.selected-count {
  font-size: 12px;
  font-weight: 400;
  color: #07C160;
}

.friend-select-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 4px 0;
  background: #fff;
}

.friend-select-list::-webkit-scrollbar {
  width: 4px;
}
.friend-select-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

.friend-select-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.friend-select-item:hover {
  background: #f5f5f5;
}

.friend-select-item.checked {
  background: #f0faf4;
}

.friend-select-name {
  font-size: 14px;
  color: #303133;
}

.no-friends-tip {
  padding: 40px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
