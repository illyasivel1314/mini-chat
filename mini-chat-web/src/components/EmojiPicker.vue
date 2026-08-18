<template>
  <div class="emoji-picker">
    <!-- Category tabs -->
    <div class="emoji-tabs">
      <span
        v-for="(cat, i) in emojiCategories"
        :key="cat.name"
        :class="['emoji-tab', { active: activeCat === i }]"
        @click="activeCat = i"
      >
        {{ cat.emojis[0] }}
      </span>
    </div>

    <!-- Emoji grid for active category -->
    <div class="emoji-grid">
      <span
        v-for="emoji in emojiCategories[activeCat].emojis"
        :key="emoji"
        class="emoji-item"
        @click="$emit('select', emoji)"
      >
        {{ emoji }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { emojiCategories } from '@/utils/emoji.js'

defineEmits(['select'])

const activeCat = ref(0)
</script>

<style scoped>
.emoji-picker {
  background: #fff;
}

.emoji-tabs {
  display: flex;
  border-bottom: 1px solid #eee;
  padding: 4px 8px;
  gap: 2px;
}

.emoji-tab {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}

.emoji-tab:hover {
  background: #f0f0f0;
}

.emoji-tab.active {
  background: #e0f5e9;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(11, 1fr);
  gap: 2px;
  padding: 8px 6px;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.1s;
}

.emoji-item:hover {
  background: #f0f0f0;
  transform: scale(1.2);
}
</style>
