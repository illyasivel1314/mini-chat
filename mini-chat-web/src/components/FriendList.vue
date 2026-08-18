<template>
  <div class="friend-list">
    <div v-if="filteredGroups.length === 0" class="empty-list">
      <p>暂无好友</p>
    </div>
    <template v-for="group in filteredGroups" :key="group.letter">
      <div class="letter-header">{{ group.letter }}</div>
      <div
        v-for="friend in group.items"
        :key="friend.id"
        class="friend-item"
        @click="startChat(friend)"
      >
        <el-avatar :size="40" :src="friend.avatar" shape="square">
          {{ friend.name.slice(0, 2) }}
        </el-avatar>
        <span class="friend-name">{{ friend.name }}</span>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useFriendStore } from '@/stores/friend.js'
import { useChatStore } from '@/stores/chat.js'

const props = defineProps({
  searchText: { type: String, default: '' }
})

const friendStore = useFriendStore()
const chatStore = useChatStore()

const filteredGroups = computed(() => {
  if (!props.searchText) return friendStore.groupedFriends
  const kw = props.searchText.toLowerCase()
  return friendStore.groupedFriends
    .map(group => ({
      ...group,
      items: group.items.filter(f =>
        f.name.toLowerCase().includes(kw)
      )
    }))
    .filter(group => group.items.length > 0)
})

function startChat(friend) {
  chatStore.setActiveChat(friend.id, 0, friend.name, friend.avatar)
}
</script>

<style scoped>
.friend-list {
  height: 100%;
  overflow-y: auto;
  background: #f0f0f0;
}

.friend-list::-webkit-scrollbar {
  width: 4px;
}
.friend-list::-webkit-scrollbar-thumb {
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

.letter-header {
  padding: 6px 18px;
  font-size: 12px;
  color: #909399;
  background: #e8e8e8;
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 1;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  cursor: pointer;
  transition: background 0.15s;
}

.friend-item:hover {
  background: #e5e5e5;
}

.friend-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
