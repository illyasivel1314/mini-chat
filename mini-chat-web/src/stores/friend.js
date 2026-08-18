import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import request from '../utils/request'

export const useFriendStore = defineStore('friend', () => {
  const friends = ref([])

  const groupedFriends = computed(() => {
    const groups = {}
    friends.value.forEach(f => {
      // Get first letter of pinyin, fallback to '#'
      const letter = f.pinyin?.[0]?.toUpperCase() || '#'
      if (!groups[letter]) groups[letter] = []
      groups[letter].push(f)
    })

    // Sort keys
    const sorted = Object.keys(groups)
      .sort((a, b) => {
        if (a === '#') return 1
        if (b === '#') return -1
        return a.localeCompare(b)
      })
      .map(letter => ({
        letter,
        items: groups[letter]
      }))
    return sorted
  })

  async function loadFriends() {
    let value = await request.get('/auth/friend/list', {})
    friends.value = value
  }

  return { friends, groupedFriends, loadFriends }
})
