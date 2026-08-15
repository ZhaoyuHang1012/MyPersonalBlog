import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getMe } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('blog_token') || '',
    user: JSON.parse(localStorage.getItem('blog_user') || 'null')
  }),
  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    async login(username, password) {
      const data = await loginApi({ username, password })
      this.setAuth(data)
    },
    async register(payload) {
      const data = await registerApi(payload)
      this.setAuth(data)
    },
    setAuth(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem('blog_token', data.token)
      localStorage.setItem('blog_user', JSON.stringify(data.user))
    },
    async fetchMe() {
      this.user = await getMe()
      localStorage.setItem('blog_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('blog_token')
      localStorage.removeItem('blog_user')
    }
  }
})
