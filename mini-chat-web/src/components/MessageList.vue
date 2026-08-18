<template>
  <div class="message-list">
    <div v-if="filteredList.length === 0" class="empty-list">
      <p>暂无消息</p>
    </div>
    <div
      v-for="conv in filteredList"
      :key="conv.show_id"
      :class="['message-item', { active: chatStore.activeShowId === conv.show_id }]"
      @click="openChat(conv)"
    >
      <div class="item-avatar">
        <el-badge :value="conv.unread" :hidden="conv.unread === 0" :max="99">
          <el-avatar :size="44" :src="conv.avatar" shape="square">
            {{ conv.name.slice(0, 2) }}
          </el-avatar>
        </el-badge>
      </div>
      <div class="item-body">
        <div class="item-top">
          <span class="item-name">
            <el-icon class="group-icon"><UserFilled /></el-icon>
            {{ conv.name }}
          </span>
          <span class="item-time">{{ formatTime(conv.lastTime) }}</span>
        </div>
        <div class="item-bottom">
          <span class="item-preview">{{ conv.lastContent }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat.js'

const props = defineProps({
  searchText: { type: String, default: '' }
})

const chatStore = useChatStore()

const filteredList = computed(() => {
  if (!props.searchText) return chatStore.conversations
  const kw = props.searchText.toLowerCase()
  return chatStore.conversations.filter(c =>
    c.name.toLowerCase().includes(kw)
  )
})

function openChat(conv) {
  chatStore.setActiveChat(conv.receiveId, conv.chatType, conv.name, conv.avatar)
}

function formatTime(ts) {
  if (!ts) return ''
  const now = new Date()
  const d = new Date(ts)
  const diffDays = Math.floor((now - d) / 86400000)

  if (diffDays === 0) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[d.getDay()]
  } else {
    return `${d.getMonth() + 1}/${d.getDate()}`
  }
}
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
  background: #f0f0f0;
}

.message-list::-webkit-scrollbar {
  width: 4px;
}
.message-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

.empty-list {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  font-size: 14px;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  cursor: pointer;
  transition: background 0.15s;
}

.message-item:hover {
  background: #e5e5e5;
}

.message-item.active {
  background: #d9d9d9;
}

.item-avatar {
  flex-shrink: 0;
}

.item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-icon {
  font-size: 13px;
  color: #909399;
}

.item-time {
  font-size: 11px;
  color: #b0b0b0;
  flex-shrink: 0;
  margin-left: 8px;
}

.item-bottom {
  display: flex;
  align-items: center;
}

.item-preview {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

/* Badge styling override */
:deep(.el-badge__content) {
  background-color: #f56c6c;
  font-size: 10px;
}
</style>
