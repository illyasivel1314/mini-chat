<template>
  <div class="chat-window-wrapper">
    <!-- Header -->
    <div class="chat-header">
      <div class="header-info">
        <el-avatar :size="36" :src="chatStore.activeChat?.avatar" shape="square">
          {{ chatStore.activeChat?.name?.slice(0, 2) }}
        </el-avatar>
        <span class="header-name">{{ chatStore.activeChat?.name }}</span>
        <el-tag v-if="chatStore.activeChat?.type === 1" size="small" type="info">
          群聊 · {{ memberCount }}人
        </el-tag>
      </div>
      <div class="header-actions">
        <el-tooltip content="刷新消息" placement="bottom">
          <el-button :icon="Refresh" text circle size="small" @click="chatStore.refreshMessages()" />
        </el-tooltip>
        <el-tooltip v-if="chatStore.activeChat?.type === 1" content="群聊信息" placement="bottom">
          <el-button :icon="InfoFilled" text circle size="small" @click="openGroupDrawer" />
        </el-tooltip>
      </div>
    </div>

    <!-- Messages container with infinite scroll -->
    <div
      ref="msgContainer"
      class="messages-container"
      @scroll="onScroll"
    >
      <!-- Loading indicator at top -->
      <div v-if="loadingHistory" class="loading-more">
        <el-icon class="loading-icon" :size="16"><Loading /></el-icon>
        <span>加载历史消息...</span>
      </div>
      <div v-else-if="!chatStore.hasMore && chatStore.activeMessages.length > 0" class="loading-more no-more">
        <span>— 没有更多消息了 —</span>
      </div>

      <!-- Empty state -->
      <div v-if="chatStore.activeMessages.length === 0 && !loadingHistory" class="empty-chat">
        <p>暂无消息，发送第一条消息吧</p>
      </div>

      <!-- Message list -->
      <div v-else>
        <!-- Date separator and messages -->
        <template v-for="(msg, index) in chatStore.activeMessages" :key="msg.id || index">
          <!-- Date separator -->
          <div v-if="showDateSep(index, msg)" class="date-separator">
            <span>{{ formatDate(msg.timestamp) }}</span>
          </div>

          <div :class="['message-row', messageClass(msg)]">
            <!-- System message -->
            <div v-if="msg.chatType === 2" class="system-msg">
              <span>{{ resolveTextContent(msg) }}</span>
            </div>

            <!-- Regular message -->
            <template v-else>
              <!-- Other user's avatar (left) -->
              <div v-if="!isSelfMessage(msg)" class="msg-avatar">
                <el-avatar :size="34" :src="resolveSenderAvatar(msg)" shape="square">
                  {{ resolveSenderName(msg).slice(0, 1) }}
                </el-avatar>
              </div>

              <!-- Message content wrapper -->
              <div :class="['msg-content-wrapper', isSelfMessage(msg) ? 'wrapper-self' : 'wrapper-other']">
                <!-- Sender name (group chat, above content) -->
                <div
                  v-if="chatStore.activeChat?.type === 1 && !isSelfMessage(msg)"
                  class="msg-sender-name"
                >
                  {{ resolveSenderName(msg) }}
                </div>
                <!-- Timestamp above content -->
                <div class="bubble-time">
                  {{ formatTime(msg.timestamp) }}
                </div>
                <!-- Message bubble -->
                <div :class="['msg-bubble', bubbleClass(msg)]">
                  <!-- Text message -->
                  <div v-if="resolveContentType(msg) === 0" class="bubble-text">
                    {{ resolveTextContent(msg) }}
                  </div>

                  <!-- Image message -->
                  <div v-else-if="resolveContentType(msg) === 1" class="bubble-image" @click="previewImage(getMsgContent(msg).fileAddress)">
                    <img :src="getMsgContent(msg).fileAddress" alt="图片" loading="lazy" />
                  </div>

                  <!-- File message (click to download) -->
                  <div v-else-if="resolveContentType(msg) === 2" class="bubble-file" @click="downloadFile(getMsgContent(msg))">
                    <FileIcon :extension="getMsgContent(msg).fileExtension" :size="40" />
                    <div class="file-info">
                      <span class="file-name" :title="getMsgContent(msg).fileName">
                        {{ getMsgContent(msg).fileName }}
                      </span>
                      <span class="file-size">{{ formatFileSize(getMsgContent(msg).fileSize) }}</span>
                    </div>
                  </div>

                  <!-- Sticker / Emoji message -->
                  <div v-else-if="resolveContentType(msg) === 3" class="bubble-sticker">
                    <img
                      :src="getMsgContent(msg).fileAddress"
                      :alt="getMsgContent(msg).emojiName || '表情'"
                      :title="getMsgContent(msg).emojiName || ''"
                      loading="lazy"
                      class="sticker-img"
                      @click="previewImage(getMsgContent(msg).fileAddress)"
                    />
                  </div>
                  <!-- Fallback -->
                  <div v-else class="bubble-text">{{ resolveLegacyContent(msg) }}</div>
                </div>
              </div>

              <!-- Self avatar (right) -->
              <div v-if="isSelfMessage(msg)" class="msg-avatar self-avatar-spacer">
                <el-avatar :size="34" :src="auth.user?.avatar" shape="square">
                  {{ auth.userName.slice(0, 2) }}
                </el-avatar>
              </div>
            </template>
          </div>
        </template>
      </div>

      <!-- Scroll to bottom hint -->
      <div v-if="showScrollHint" class="scroll-hint" @click="scrollToBottom">
        <el-icon><ArrowDown /></el-icon>
        <span>回到底部</span>
      </div>
    </div>

    <!-- Image Preview Dialog -->
    <el-dialog v-model="imageVisible" :close-on-click-modal="true" width="auto" class="image-dialog">
      <img :src="previewUrl" alt="预览" style="max-width: 80vw; max-height: 80vh;" />
    </el-dialog>

    <!-- Group Info Drawer -->
    <el-drawer
      v-model="showGroupDrawer"
      title="群聊信息"
      direction="rtl"
      size="400px"
      destroy-on-close
    >
      <template v-if="groupDetail">
        <div class="group-drawer-content">
          <!-- Group Name -->
          <div class="group-section">
            <label class="section-label">群聊名称</label>
            <div class="section-row">
              <el-input
                v-if="isGroupCreator && editingName"
                v-model="editName"
                size="default"
                maxlength="30"
                @blur="saveGroupName"
                @keyup.enter="saveGroupName"
                ref="nameInputRef"
              />
              <span v-else class="section-value">{{ groupDetail.name }}</span>
              <el-button
                v-if="isGroupCreator && !editingName"
                text
                size="small"
                type="primary"
                @click="startEditName"
              >
                修改
              </el-button>
            </div>
          </div>

          <!-- Group Announcement -->
          <div class="group-section">
            <label class="section-label">群公告</label>
            <div class="section-row">
              <template v-if="isGroupCreator && editingAnnouncement">
                <el-input
                  v-model="editAnnouncement"
                  type="textarea"
                  :rows="3"
                  maxlength="200"
                  show-word-limit
                  @blur="saveGroupAnnouncement"
                />
              </template>
              <span v-else class="section-value text-wrap">{{ groupDetail.announcement || '暂无群公告' }}</span>
              <el-button
                v-if="isGroupCreator && !editingAnnouncement"
                text
                size="small"
                type="primary"
                @click="startEditAnnouncement"
              >
                修改
              </el-button>
            </div>
          </div>

          <!-- Group Remark -->
          <div class="group-section">
            <label class="section-label">备注</label>
            <div class="section-row">
              <el-input
                v-if="isGroupCreator && editingRemark"
                v-model="editRemark"
                size="default"
                maxlength="50"
                @blur="saveGroupRemark"
                @keyup.enter="saveGroupRemark"
              />
              <span v-else class="section-value">{{ groupDetail.remark || '暂无备注' }}</span>
              <el-button
                v-if="isGroupCreator && !editingRemark"
                text
                size="small"
                type="primary"
                @click="startEditRemark"
              >
                修改
              </el-button>
            </div>
          </div>

          <!-- Group Members -->
          <div class="group-section">
            <label class="section-label">
              群成员
              <span class="member-count">共 {{ groupDetail.members?.length || 0 }} 人</span>
            </label>
            <div class="member-grid">
              <div
                v-for="member in displayedMembers"
                :key="member.id"
                class="member-item"
              >
                <el-avatar :size="44" :src="member.avatar" shape="square">
                  {{ (member.name || member.account).slice(0, 2) }}
                </el-avatar>
                <span class="member-name" :title="member.name || member.account">
                  {{ member.name || member.account }}
                </span>
                <span v-if="member.id === groupDetail.creatorId" class="creator-badge">群主</span>
              </div>
              <!-- Add member entry -->
              <div class="member-item member-add" @click="showAddMember = true">
                <div class="add-member-icon">
                  <el-icon :size="20"><Plus /></el-icon>
                </div>
                <span class="member-name">添加</span>
              </div>
            </div>
            <el-button
              v-if="groupDetail.members?.length > 16"
              text
              size="small"
              type="primary"
              class="view-more-btn"
              @click="showAllMembers = !showAllMembers"
            >
              {{ showAllMembers ? '收起' : '查看更多' }}
            </el-button>
          </div>

          <!-- Exit Group -->
          <div class="group-section">
            <el-button
              type="danger"
              plain
              class="exit-group-btn"
              @click="handleLeaveGroup"
            >
              退出群聊
            </el-button>
          </div>
        </div>
      </template>
      <div v-else class="drawer-loading">
        <el-icon class="loading-icon" :size="20"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </el-drawer>

    <!-- Add Member Dialog -->
    <el-dialog
      v-model="showAddMember"
      title="添加群成员"
      width="460px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="add-member-list">
        <div
          v-for="friend in addableFriends"
          :key="friend.id"
          class="add-member-item"
        >
          <el-checkbox
            :model-value="selectedAddIds.includes(friend.id)"
            :disabled="existingMemberIds.includes(friend.id)"
            @change="toggleAddMember(friend.id)"
          />
          <el-avatar :size="36" :src="friend.avatar" shape="square">
            {{ friend.name.slice(0, 2) }}
          </el-avatar>
          <span class="add-member-name">{{ friend.name }}</span>
          <span v-if="existingMemberIds.includes(friend.id)" class="already-in-tag">已在群</span>
        </div>
        <div v-if="addableFriends.length === 0" class="no-friends-tip">
          没有可添加的好友
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddMember = false">取消</el-button>
        <el-button
          type="success"
          :disabled="newMemberIds.length === 0"
          :loading="addingMembers"
          @click="handleAddMembers"
        >
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, computed } from 'vue'
import { Refresh, ArrowDown, Loading, InfoFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth.js'
import { useChatStore } from '@/stores/chat.js'
import { useFriendStore } from '@/stores/friend.js'
import { formatFileSize } from '@/utils/fileIcon.js'
import FileIcon from '@/components/FileIcon.vue'

const auth = useAuthStore()
const chatStore = useChatStore()
const friendStore = useFriendStore()

const msgContainer = ref(null)
const showScrollHint = ref(false)
const imageVisible = ref(false)
const previewUrl = ref('')
const loadingHistory = ref(false)
const prevScrollHeight = ref(0)

const memberCount = computed(() => {
  // if (chatStore.activeChat?.type !== 1) return 0;
  // return groupDetail.value.members?.length || 0
  return 0;
})

// ---- Group Drawer State ----
const showGroupDrawer = ref(false)
const groupDetail = ref(null)
const showAllMembers = ref(false)
const nameInputRef = ref(null)

// Editing states
const editingName = ref(false)
const editName = ref('')
const editingAnnouncement = ref(false)
const editAnnouncement = ref('')
const editingRemark = ref(false)
const editRemark = ref('')

// Add member state
const showAddMember = ref(false)
const selectedAddIds = ref([])
const addingMembers = ref(false)

const isGroupCreator = computed(() => {
  if (!groupDetail.value) return false
  return groupDetail.value.creatorId === auth.userId
})

const displayedMembers = computed(() => {
  if (!groupDetail.value?.members) return []
  if (showAllMembers.value) return groupDetail.value.members
  return groupDetail.value.members.slice(0, 16)
})

const existingMemberIds = computed(() => {
  if (!groupDetail.value?.members) return []
  return groupDetail.value.members.map(m => m.id)
})

const newMemberIds = computed(() => {
  return selectedAddIds.value.filter(id => !existingMemberIds.value.includes(id))
})

const addableFriends = computed(() => {
  return friendStore.friends
})

// ---- Group Drawer Methods ----

async function openGroupDrawer() {
  showGroupDrawer.value = true
  showAllMembers.value = false
  await loadGroupDetail()
}

async function loadGroupDetail() {
  try {
    const groupId = chatStore.activeChat?.show_id
    if (!groupId) return
    groupDetail.value = await chatStore.getGroupDetail(groupId)
  } catch (e) {
    ElMessage.error('加载群聊信息失败')
  }
}

function startEditName() {
  editName.value = groupDetail.value?.name || ''
  editingName.value = true
  nextTick(() => {
    nameInputRef.value?.focus()
  })
}

async function saveGroupName() {
  if (!editName.value.trim()) {
    editName.value = groupDetail.value?.name || ''
    editingName.value = false
    return
  }
  try {
    await chatStore.updateGroupInfo(chatStore.activeChat.show_id, { name: editName.value.trim() })
    groupDetail.value.name = editName.value.trim()
    // Update active chat name
    if (chatStore.activeChat) {
      chatStore.activeChat.name = editName.value.trim()
    }
    ElMessage.success('群名称已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    editingName.value = false
  }
}

function startEditAnnouncement() {
  editAnnouncement.value = groupDetail.value?.announcement || ''
  editingAnnouncement.value = true
}

async function saveGroupAnnouncement() {
  try {
    await chatStore.updateGroupInfo(chatStore.activeChat.show_id, { announcement: editAnnouncement.value })
    groupDetail.value.announcement = editAnnouncement.value
    ElMessage.success('群公告已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    editingAnnouncement.value = false
  }
}

function startEditRemark() {
  editRemark.value = groupDetail.value?.remark || ''
  editingRemark.value = true
}

async function saveGroupRemark() {
  try {
    await chatStore.updateGroupInfo(chatStore.activeChat.show_id, { remark: editRemark.value })
    groupDetail.value.remark = editRemark.value
    ElMessage.success('备注已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    editingRemark.value = false
  }
}

function toggleAddMember(friendId) {
  const idx = selectedAddIds.value.indexOf(friendId)
  if (idx === -1) {
    selectedAddIds.value.push(friendId)
  } else {
    selectedAddIds.value.splice(idx, 1)
  }
}

async function handleAddMembers() {
  if (newMemberIds.value.length === 0) return
  addingMembers.value = true
  try {
    await chatStore.addGroupMembers(chatStore.activeChat.show_id, newMemberIds.value)
    ElMessage.success('成员添加成功')
    showAddMember.value = false
    selectedAddIds.value = []
    await loadGroupDetail()
  } catch (e) {
    ElMessage.error('添加成员失败')
  } finally {
    addingMembers.value = false
  }
}

async function handleLeaveGroup() {
  try {
    await ElMessageBox.confirm('确定要退出该群聊吗？', '退出群聊', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chatStore.leaveGroup(chatStore.activeChat.show_id)
    ElMessage.success('已退出群聊')
    showGroupDrawer.value = false
    chatStore.activeShowId = null
    chatStore.activeChat = null
    await chatStore.loadConversations()
  } catch (e) {
    // User cancelled or error
    if (e !== 'cancel') {
      ElMessage.error('退出群聊失败')
    }
  }
}

// Track if user has scrolled up
let userScrolledUp = false

// ---- Content type resolution (handles both new and legacy formats) ----

function resolveContentType(msg) {
  return msg.contentType || 0
}

function getMsgContent(msg) {
  let content = JSON.parse(msg.content || '{}')
  switch (msg.contentType) {
    case 0: // Text
      return { content: content.content }
    case 1: // Image
      return { fileAddress: content.fileAddress }
    case 2: // File
      return { fileAddress: content.fileAddress, fileName: content.fileName, fileExtension: content.fileExtension, fileSize: content.fileSize }
    case 3: // Sticker / Emoji
      return { fileAddress: content.fileAddress, emojiName: content.emojiName }
    default:
      return content
  }
}

function resolveTextContent(msg) {
  const c = getMsgContent(msg)
  return c.content || ''
}

function resolveLegacyContent(msg) {
  // For backward compatibility with old string-based content
  if (typeof msg.content === 'string') return msg.content
  const c = getMsgContent(msg)
  return c.content || c.fileAddress || ''
}

function isSelfMessage(msg) {
  // New format uses senderId, legacy uses send_id
  const senderId = msg.senderId || msg.send_id
  return senderId === auth.userId
}

function resolveSenderName(msg) {
  if (msg.senderName) return msg.senderName
  // Legacy fallback
  const senderId = msg.senderId || msg.send_id
  const friend = friendStore.friends.find(f => f.id === senderId)
  return friend?.name || '未知用户'
}

function resolveSenderAvatar(msg) {
  if (msg.senderAvatar) return msg.senderAvatar
  // Legacy fallback
  const senderId = msg.senderId || msg.send_id
  const friend = friendStore.friends.find(f => f.id === senderId)
  return friend?.avatar || ''
}

// ---- Scroll logic ----

function onScroll() {
  if (!msgContainer.value) return
  const el = msgContainer.value

  // Detect if at top → load more
  if (el.scrollTop <= 50 && chatStore.hasMore && !loadingHistory.value) {
    loadMore()
  }

  // Detect if near bottom
  const threshold = 100
  userScrolledUp = el.scrollHeight - el.scrollTop - el.clientHeight > threshold
  showScrollHint.value = userScrolledUp
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
      showScrollHint.value = false
      userScrolledUp = false
    }
  })
}

async function loadMore() {
  if (loadingHistory.value || !chatStore.hasMore) return
  loadingHistory.value = true

  // Record current scroll height before loading
  const el = msgContainer.value
  if (el) {
    prevScrollHeight.value = el.scrollHeight
  }

  await chatStore.loadMoreMessages()

  // After messages are prepended, maintain scroll position
  await nextTick()
  if (el) {
    const newScrollHeight = el.scrollHeight
    el.scrollTop = newScrollHeight - prevScrollHeight.value
  }

  loadingHistory.value = false
}

// ---- Message display helpers ----

function messageClass(msg) {
  if (msg.chatType === 2) return 'row-system'
  return isSelfMessage(msg) ? 'row-self' : 'row-other'
}

function bubbleClass(msg) {
  const contentType = resolveContentType(msg)
  // Image, file, sticker: use neutral background (same for sender and receiver)
  if (contentType === 1 || contentType === 2 || contentType === 3) {
    return 'bubble-neutral'
  }
  // Text and other: use directional backgrounds
  return isSelfMessage(msg) ? 'bubble-self' : 'bubble-other'
}

function previewImage(url) {
  previewUrl.value = url
  imageVisible.value = true
}

function downloadFile(fileContent) {
  if (fileContent.fileAddress) {
    // Create a temporary link to trigger download
    const link = document.createElement('a')
    link.href = fileContent.fileAddress
    link.download = fileContent.fileName || 'download'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

// ---- Date & Time formatting ----

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function formatDate(ts) {
  if (!ts) return ''
  const now = new Date()
  const d = new Date(ts)
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDay = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const diffDays = Math.floor((today - msgDay) / 86400000)

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[d.getDay()]
  }
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function showDateSep(index, msg) {
  if (index === 0) return true
  const prev = chatStore.activeMessages[index - 1]
  if (!prev || !prev.timestamp || !msg.timestamp) return false
  const prevDate = new Date(prev.timestamp).toDateString()
  const curDate = new Date(msg.timestamp).toDateString()
  return prevDate !== curDate
}

// ---- Auto-scroll on new messages ----

watch(() => chatStore.activeMessages.length, () => {
  if (!userScrolledUp) {
    scrollToBottom()
  }
})

// Scroll to bottom when conversation changes
watch(() => chatStore.activeShowId, () => {
  userScrolledUp = false
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-window-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* ---- Header ---- */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* ---- Messages ---- */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f5f5f5;
  position: relative;
}

.messages-container::-webkit-scrollbar {
  width: 4px;
}
.messages-container::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

/* ---- Loading / no-more indicators ---- */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 0;
  color: #909399;
  font-size: 12px;
}

.loading-more.no-more {
  color: #c0c4cc;
}

.loading-icon {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ---- Empty state ---- */
.empty-chat {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
}

/* ---- Date separator ---- */
.date-separator {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.date-separator span {
  display: inline-block;
  padding: 3px 12px;
  font-size: 11px;
  color: #b0b0b0;
  background: #e8e8e8;
  border-radius: 3px;
}

/* ---- Message row ---- */
.message-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
}

.row-self {
  justify-content: flex-end;
}

.row-other {
  justify-content: flex-start;
}

.row-system {
  justify-content: center;
}

/* ---- System message ---- */
.system-msg {
  text-align: center;
  max-width: 80%;
}

.system-msg span {
  display: inline-block;
  padding: 4px 14px;
  font-size: 12px;
  color: #b0b0b0;
  background: #e8e8e8;
  border-radius: 4px;
}

/* ---- Avatar ---- */
.msg-avatar {
  flex-shrink: 0;
  margin: 0 10px;
}

.self-avatar-spacer {
  margin-right: 0;
}

/* ---- Message content wrapper ---- */
.msg-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 55%;
}

.wrapper-self {
  align-items: flex-end;
}

.wrapper-other {
  align-items: flex-start;
}

/* ---- Sender name (group chat, above content) ---- */
.msg-sender-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
  padding: 0 4px;
}

/* ---- Bubble timestamp (above content) ---- */
.bubble-time {
  font-size: 10px;
  color: #b0b0b0;
  margin-bottom: 4px;
  padding: 0 4px;
}

/* ---- Bubble ---- */
.msg-bubble {
  padding: 10px 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  position: relative;
}

.bubble-self {
  background: #95EC69;
  border-radius: 8px 2px 8px 8px;
  color: #000;
}

.bubble-other {
  background: #fff;
  border-radius: 2px 8px 8px 8px;
  color: #303133;
}

.bubble-neutral {
  background: transparent;
  border-radius: 4px;
  padding: 0;
}

.bubble-text {
  white-space: pre-wrap;
}

.bubble-emoji {
  font-size: 40px;
  line-height: 1.2;
}

.bubble-image {
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  max-width: 240px;
}

.bubble-image img {
  width: 100%;
  height: auto;
  display: block;
}

/* ---- Sticker (emoji/sticker content type) ---- */
.bubble-sticker {
  cursor: pointer;
  max-width: 160px;
  border-radius: 4px;
  overflow: hidden;
}

.bubble-sticker .sticker-img {
  width: 100%;
  height: auto;
  display: block;
}

/* ---- File bubble ---- */
.bubble-file {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 2px;
  cursor: pointer;
  user-select: none;
  background: #ffffff;
}

.bubble-file:hover {
  opacity: 0.85;
}

.file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.file-size {
  font-size: 11px;
  color: #909399;
}

/* ---- Scroll hint ---- */
.scroll-hint {
  position: sticky;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  z-index: 10;
}

.scroll-hint:hover {
  background: #f0f0f0;
}

/* ---- Image dialog ---- */
:deep(.image-dialog .el-dialog__body) {
  padding: 12px;
}

/* ---- Group Drawer ---- */
.drawer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}

.group-drawer-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 20px;
}

.group-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.member-count {
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.section-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.section-row .el-input,
.section-row .el-textarea {
  flex: 1;
}

.section-value {
  font-size: 14px;
  color: #606266;
  flex: 1;
  line-height: 1.6;
}

.section-value.text-wrap {
  white-space: pre-wrap;
  word-break: break-word;
}

/* ---- Member Grid ---- */
.member-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 8px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  position: relative;
}

.member-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
  width: 100%;
}

.creator-badge {
  position: absolute;
  top: -2px;
  right: 4px;
  font-size: 10px;
  color: #fff;
  background: #07C160;
  padding: 1px 4px;
  border-radius: 3px;
  line-height: 1.4;
}

.member-add {
  cursor: pointer;
}

.add-member-icon {
  width: 44px;
  height: 44px;
  border-radius: 4px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  transition: background 0.2s;
}

.member-add:hover .add-member-icon {
  background: #e0e0e0;
  color: #07C160;
}

.view-more-btn {
  align-self: center;
  margin-top: 4px;
}

/* ---- Exit Group ---- */
.exit-group-btn {
  width: 100%;
}

/* ---- Add Member Dialog ---- */
.add-member-list {
  max-height: 360px;
  overflow-y: auto;
}

.add-member-list::-webkit-scrollbar {
  width: 4px;
}
.add-member-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

.add-member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 4px;
}

.add-member-name {
  font-size: 14px;
  color: #303133;
  flex: 1;
}

.already-in-tag {
  font-size: 11px;
  color: #909399;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 3px;
}

.no-friends-tip {
  padding: 40px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
