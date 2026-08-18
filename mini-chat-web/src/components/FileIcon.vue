<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 48 48"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    class="file-icon-svg"
  >
    <!-- File body with fold corner -->
    <path
      d="M10 6C10 4.89543 10.8954 4 12 4H30L38 12V42C38 43.1046 37.1046 44 36 44H12C10.8954 44 10 43.1046 10 42V6Z"
      :fill="bgColor"
    />
    <!-- Fold corner -->
    <path d="M30 4V12H38L30 4Z" fill="rgba(0,0,0,0.12)" />

    <!-- Icon glyph based on type -->
    <template v-if="iconType === 'txt'">
      <text x="24" y="32" text-anchor="middle" fill="#fff" font-size="11" font-weight="600" font-family="sans-serif">TXT</text>
    </template>
    <template v-else-if="iconType === 'word'">
      <text x="24" y="32" text-anchor="middle" fill="#fff" font-size="13" font-weight="700" font-family="sans-serif">W</text>
    </template>
    <template v-else-if="iconType === 'excel'">
      <text x="24" y="32" text-anchor="middle" fill="#fff" font-size="13" font-weight="700" font-family="sans-serif">X</text>
    </template>
    <template v-else-if="iconType === 'ppt'">
      <!-- Play button triangle -->
      <polygon points="20,20 20,36 34,28" fill="#fff" />
    </template>
    <template v-else-if="iconType === 'rar'">
      <!-- Stacked books / archive boxes -->
      <rect x="16" y="18" width="16" height="4" rx="1" fill="rgba(255,255,255,0.8)" />
      <rect x="16" y="24" width="16" height="4" rx="1" fill="rgba(255,255,255,0.6)" />
      <rect x="16" y="30" width="16" height="4" rx="1" fill="rgba(255,255,255,0.4)" />
    </template>
    <template v-else-if="iconType === 'tar'">
      <!-- Compressed box icon -->
      <rect x="15" y="20" width="18" height="14" rx="2" fill="rgba(255,255,255,0.8)" />
      <rect x="20" y="24" width="8" height="3" rx="1" fill="rgba(0,0,0,0.25)" />
      <rect x="20" y="29" width="8" height="2" rx="1" fill="rgba(0,0,0,0.25)" />
    </template>
    <template v-else>
      <!-- Unknown file: question mark -->
      <text x="24" y="32" text-anchor="middle" fill="#fff" font-size="18" font-weight="600" font-family="sans-serif">?</text>
    </template>
  </svg>
</template>

<script setup>
import { computed } from 'vue'
import { getFileIconType, getFileTypeColor } from '@/utils/fileIcon.js'

const props = defineProps({
  extension: { type: String, default: '' },
  size: { type: Number, default: 40 },
})

const iconType = computed(() => getFileIconType(props.extension))
const bgColor = computed(() => getFileTypeColor(props.extension))
</script>

<style scoped>
.file-icon-svg {
  flex-shrink: 0;
  display: block;
}
</style>
