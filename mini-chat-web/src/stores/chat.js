import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import request from '../utils/request'
import { useWebSocket } from '../utils/webSocket';
import avatarUri from '../utils/emoji'
import { ElMessage } from 'element-plus'
import { Md5 } from 'ts-md5'

// 会话ID解析（与后端 ConversationService.resolveConversationId 规则一致）
function resolveConversationId(chatType, senderId, receiverId) {
  if (chatType === 1) return receiverId        // 群聊 = groupId
  if (chatType === 3) return Md5.hashStr('self|' + senderId)
  if (chatType === 2) return Md5.hashStr('system|' + receiverId)
  const [a, b] = senderId < receiverId ? [senderId, receiverId] : [receiverId, senderId]
  return Md5.hashStr('p|' + a + '|' + b)
}

export const useChatStore = defineStore('chat', () => {
  // 聊天对象列表
  const conversations = ref([])
  // 聊天消息列表
  const messages = ref([])
  // 当前页数
  const currentPage = ref(1)
  // 是否还有更多消息可加载
  const hasMore = ref(true)
  // 是否正在加载更多消息
  const loadingMore = ref(false)
  // 当前激活的聊天对象的 show_id
  const activeShowId = ref(null)
  // 当前激活的聊天对象信息 { show_id, type, name, avatar }
  const activeChat = ref(null)  // { show_id, type, name, avatar }

  // 获取当前激活聊天对象的消息列表
  const activeMessages = computed(() => messages.value)

  // 获取当前激活聊天对象的会话信息
  const activeConversation = computed(() =>
    conversations.value.find(c => c.show_id === activeShowId.value)
  )

  /**
   * Load older messages (infinite scroll upward).
   * Prepends older messages and maintains scroll position via callback.
   */
  async function loadMoreMessages() {
    if (!hasMore.value || loadingMore.value || !activeChat.value) return

    loadingMore.value = true
    const nextPage = currentPage.value + 1

    try {
      const chat = activeChat.value
      const olderMessages = chat.type == 0
        ? await request.get('/chat/private', { params: { userId: chat.show_id, page: nextPage - 1 } })
        : await request.get('/chat/group', { params: { groupId: chat.show_id, page: nextPage - 1} })

      if (olderMessages.empty || olderMessages.content?.length === 0) {
        hasMore.value = false
      } else {
        // Prepend older messages (they come in chronological order; newest first from API)
        olderMessages.content.reverse()  // Reverse to maintain chronological order
        messages.value = [...olderMessages.content, ...messages.value]
        currentPage.value = nextPage
      }
    } catch (e) {
      console.error('Failed to load more messages:', e)
    } finally {
      loadingMore.value = false
    }
  }

  async function loadConversations() {
    conversations.value = await request.post('/chat/message/friend', { })
  }

  async function loadGroups() {
    return await request.get('/auth/group/list', { })
  }

  // ---- Group operations ----

  async function createGroup(name, memberIds) {
    return await request.post('/auth/group/create', { name, userIdList: memberIds, avatar: avatarUri("群" + name, Math.floor(Math.random() * 1000)) })
  }

  async function getGroupDetail(groupId) {
    return await request.get('/auth/group/detail', { params: { groupId } })
  }

  async function getGroupMembers(groupId) {
    return await request.get('/auth/group/members', { params: { groupId } })
  }

  async function addGroupMembers(groupId, memberIds) {
    return await request.post('/auth/group/addMembers', { groupId, userIdList: memberIds })
  }

  async function updateGroupInfo(groupId, data) {
    return await request.post('/auth/group/update', { groupId, ...data })
  }

  async function leaveGroup(groupId) {
    return await request.post('/auth/group/leave', { groupId })
  }

  async function markMessagesAsRead(userId) {
    return await request.get('/chat/message/read', { params: { userId } })
  }

  async function setActiveChat(showId, type, name, avatar) {
    const auth = useAuthStore()
    activeShowId.value = showId
    activeChat.value = {
      show_id: showId,
      type,
      name,
      avatar,
      conversationId: resolveConversationId(type, auth.userId, showId)
    }
    currentPage.value = 0
    hasMore.value = true
    loadingMore.value = false
    messages.value = []
    // 将消息标记为已读
    const conversation = conversations.value.find(c => c.receiveId === activeShowId.value);
    if (conversation && conversation.unread > 0) {
      conversation.unread = 0;  // Reset unread count for this conversation
      markMessagesAsRead(showId)  // Mark messages as read for this chat
    }
    await loadMoreMessages()
  }

  async function sendMessage(type, content) {
    const auth = useAuthStore()
    if (!activeChat.value) return

    const chat = activeChat.value

    let message = {
      receiverId: chat.show_id,
      chatType: chat.type,
      contentType: type,
      content:JSON.stringify(content)
    }
    // Send via WebSocket
    useWebSocket().sendMessage(message)

    return message
  }

  function refreshMessages() {
    if (activeChat.value) {
      currentPage.value = 0
      loadingMore.value = false
      hasMore.value = true
      messages.value = []
      loadMoreMessages()
    }
  }

  /**
   * 统一消息入口：私聊/群聊消息均经 /user/queue/private 推送，按 conversationId 归属会话
   */
  async function uploadMessage(message) {
    // 撤回消息：原地替换而不是追加
    if (message.recalled) {
      replaceIfRecalled(message)
      await loadConversations()
      return
    }
    // 左侧会话列表也需要更新最新消息
    await loadConversations()
    const auth = useAuthStore()
    // 根据会话ID识别消息归属于哪个会话
    if (activeChat.value && message.conversationId === activeChat.value.conversationId) {
      // 发给我的私聊消息（当前会话已激活）→ 标记已读
      if (message.chatType === 0 && message.senderId !== auth.userId) {
        markMessagesAsRead(activeChat.value.show_id)
      }
      messages.value.push(message)
    }
  }

  /**
   * 系统消息（广播）：仅处理发给自己的，刷新会话并弹提示
   */
  async function uploadSystemMessage(message) {
    const auth = useAuthStore()
    if (message.receiverId !== auth.userId) return
    await loadConversations()
    const text = parseSystemText(message.content)
    if (text) ElMessage.info(text)
  }

  function parseSystemText(content) {
    try {
      return JSON.parse(content).content || ''
    } catch {
      return ''
    }
  }

  /**
   * 撤回消息（仅本人、2 分钟内）
   */
  async function recallMessage(messageId) {
    const dto = await request.post('/chat/message/recall', null, { params: { messageId } })
    replaceIfRecalled(dto)
    return dto
  }

  /**
   * 将撤回消息原地替换（若存在于当前会话）
   */
  function replaceIfRecalled(message) {
    if (!message.recalled) return false
    const idx = messages.value.findIndex(m => m.id === message.id)
    if (idx !== -1) {
      messages.value[idx] = { ...messages.value[idx], ...message, recalled: true }
      return true
    }
    return false
  }

  async function uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res;
  }


  return {
    conversations,
    messages,
    currentPage,
    hasMore,
    loadingMore,
    activeShowId,
    activeChat,
    activeMessages,
    activeConversation,
    uploadFile,
    loadConversations,
    loadGroups,
    createGroup,
    getGroupDetail,
    getGroupMembers,
    addGroupMembers,
    updateGroupInfo,
    leaveGroup,
    setActiveChat,
    loadMoreMessages,
    sendMessage,
    refreshMessages,
    uploadMessage,
    uploadSystemMessage,
    markMessagesAsRead,
    recallMessage
  }
})
