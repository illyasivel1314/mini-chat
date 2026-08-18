<template>
  <div class="chat-input-wrapper">
    <!-- Toolbar -->
    <div class="input-toolbar">
      <el-tooltip content="表情" placement="top">
        <el-button text circle @click="showEmoji = !showEmoji" class="emoji-btn">
          😀
        </el-button>
      </el-tooltip>
      <el-tooltip content="图片" placement="top">
        <el-button text circle @click="triggerImageUpload">
          <el-icon :size="18"><PictureFilled /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="表情包(GIF/贴图)" placement="top">
        <el-button text circle @click="showSticker = !showSticker">
          <el-icon :size="18"><VideoCameraFilled /></el-icon>
        </el-button>
      </el-tooltip>
      <el-tooltip content="文件" placement="top">
        <el-button text circle @click="triggerFileUpload">
          <el-icon :size="18"><FolderOpened /></el-icon>
        </el-button>
      </el-tooltip>

      <input
        ref="imageInput"
        type="file"
        accept="image/*"
        style="display:none"
        @change="onImageSelected"
      />
      <input
        ref="fileInput"
        type="file"
        style="display:none"
        @change="onFileSelected"
      />
    </div>

    <!-- Emoji Picker (text emoji) -->
    <div v-if="showEmoji" class="emoji-panel">
      <EmojiPicker @select="onEmojiSelect" />
    </div>

    <!-- Sticker/GIF Panel -->
    <div v-if="showSticker" class="sticker-panel">
      <div class="sticker-grid">
        <img
          v-for="(sticker, i) in sampleStickers"
          :key="i"
          :src="sticker.url"
          class="sticker-item"
          @click="onStickerSelect(sticker)"
          :alt="sticker.name"
          loading="lazy"
        />
      </div>
    </div>

    <!-- Input area -->
    <div class="input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="输入消息..."
        resize="none"
        class="msg-textarea"
        @keydown="onKeydown"
      />
      <el-button
        type="success"
        class="send-btn"
        :disabled="!inputText.trim()"
        @click="sendText"
      >
        发送(S)
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { PictureFilled, VideoCameraFilled, FolderOpened } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat.js'
import { sampleGifs } from '@/utils/emoji.js'
import EmojiPicker from './EmojiPicker.vue'

const chatStore = useChatStore()

const inputText = ref('')
const showEmoji = ref(false)
const showSticker = ref(false)
const imageInput = ref(null)
const fileInput = ref(null)

// Convert GIF list to sticker format
const sampleStickers = sampleGifs.map((url, i) => ({
  url,
  name: `GIF ${i + 1}`,
}))

function onKeydown(e) {
  // Enter sends, Shift+Enter adds newline
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendText()
  }
}

async function sendText() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  showEmoji.value = false
  // Send text message with structured content format
  await chatStore.sendMessage(0, { content: text })
}

function onEmojiSelect(emoji) {
  inputText.value += emoji
}

function onStickerSelect(sticker) {
  showSticker.value = false
  // Send sticker with structured content
  chatStore.sendMessage('sticker', {
    fileAddress: sticker.url,
    emojiName: sticker.name,
  })
}

function triggerImageUpload() {
  imageInput.value?.click()
}

function triggerFileUpload() {
  fileInput.value?.click()
}

function getFileExtension(filename) {
  if (!filename) return ''
  // Handle tar.gz specially
  if (filename.toLowerCase().endsWith('.tar.gz')) return 'tar.gz'
  const parts = filename.split('.')
  return parts.length > 1 ? parts.pop() : ''
}

async function onImageSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return
  // Create object URL for local preview
  const url = await chatStore.uploadFile(file)
  chatStore.sendMessage(1, { fileAddress: url })
  // Reset
  e.target.value = ''
}

async function onFileSelected(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // Build structured file content
  const url = await chatStore.uploadFile(file)
  const extension = getFileExtension(file.name)
  const fileContent = {
    fileAddress: url,
    fileName: file.name,
    fileExtension: extension,
    fileSize: file.size,
  }

  chatStore.sendMessage(2, fileContent)
  e.target.value = ''
}
</script>

<style scoped>
.chat-input-wrapper {
  background: #f5f5f5;
  border-top: 1px solid #e0e0e0;
  flex-shrink: 0;
}

/* ---- Toolbar ---- */
.input-toolbar {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 16px 2px;
}

.input-toolbar .el-button {
  color: #606266;
}
.input-toolbar .el-button:hover {
  color: #07C160;
  background: #e8e8e8;
}
.emoji-btn {
  font-size: 18px;
}

/* ---- Emoji panel ---- */
.emoji-panel {
  border-top: 1px solid #e8e8e8;
  background: #fff;
  max-height: 280px;
  overflow-y: auto;
}

.emoji-panel::-webkit-scrollbar { width: 4px; }
.emoji-panel::-webkit-scrollbar-thumb { background: #ccc; border-radius: 2px; }

/* ---- Sticker / GIF panel ---- */
.sticker-panel {
  border-top: 1px solid #e8e8e8;
  background: #fff;
  padding: 12px;
  max-height: 240px;
  overflow-y: auto;
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.sticker-item {
  width: 100%;
  height: 80px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.15s;
}

.sticker-item:hover {
  transform: scale(1.05);
}

/* ---- Input area ---- */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 8px 16px 16px;
}

.msg-textarea {
  flex: 1;
}

.msg-textarea :deep(.el-textarea__inner) {
  background: #fff;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.send-btn {
  height: 40px;
  padding: 0 24px;
  --el-button-bg-color: #07C160;
  --el-button-border-color: #07C160;
  --el-button-hover-bg-color: #06ad56;
  --el-button-hover-border-color: #06ad56;
  --el-button-active-bg-color: #059a4c;
  --el-button-active-border-color: #059a4c;
  font-size: 14px;
  flex-shrink: 0;
}
</style>
