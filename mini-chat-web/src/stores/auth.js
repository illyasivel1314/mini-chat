import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'
import { Md5 } from 'ts-md5'
import avatarUri from '../utils/emoji'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id || '')
  const userName = computed(() => user.value?.name || '')

  async function login(phone, password) {
    const result = await request.post(
      "/auth/login",
      { phone, password: Md5.hashStr(password) }
    );
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  async function register(formData) {
    const result = await request.post('/auth/register', {
      phone: formData.phone,
      password: Md5.hashStr(formData.password),
      name: formData.name,
      account: formData.account,
      position: formData.position,
      duty: formData.duty,
      detail: formData.detail,
      positionDescription: formData.positionDescription,
      avatar: avatarUri(formData.name, Math.floor(Math.random() * 1000))
    })
    // 注册成功即自动登录
    token.value = result.token
    user.value = result
    localStorage.setItem('token', result.token)
    localStorage.setItem('user', JSON.stringify(result))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, userId, userName, login, register, logout }
})
