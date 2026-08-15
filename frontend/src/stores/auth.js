import { defineStore } from 'pinia'
import { authApi } from '../api'
import { tokenStorage } from '../utils/tokenStorage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: tokenStorage.get(),
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
  },
  actions: {
    async login(payload) {
      const { data } = await authApi.login(payload)
      this.accessToken = data.accessToken
      tokenStorage.set(data.accessToken)
    },
    logout() {
      this.accessToken = ''
      tokenStorage.clear()
    },
  },
})
