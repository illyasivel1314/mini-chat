import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import request from '../utils/request'
import { Md5 } from 'ts-md5'
import avatarUri from '../utils/emoji'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id || '')
  const userName = computed(() => user.value?.name || '')

  async function login(username, password) {

    const result = await request.post(
      "/auth/login", 
      { account: username, password: Md5.hashStr(password) }
    );
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  

  async function register(formData) {
    const result = await request.post('/auth/register', {
      account: formData.account,
      password: Md5.hashStr(formData.password),
      name: formData.name,
      position: formData.position,
      positionDescription: formData.positionDescription,
      avatar: avatarUri(formData.name, Math.floor(Math.random() * 1000))
    })
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, userId, userName, login, register, logout }
})
