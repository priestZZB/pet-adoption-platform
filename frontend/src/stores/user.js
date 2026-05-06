import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { getUserInfo } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null,
    roles: []
  }),
  getters: {
    isLogin: (state) => !!state.token,
    hasRole: (state) => (role) => state.roles.includes(role),
    isAdmin: (state) => state.roles.includes('ADMIN'),
    isVolunteer: (state) => state.roles.includes('VOLUNTEER'),
    isDonor: (state) => state.roles.includes('USER_ADOPTER')
  },
  actions: {
    setToken(token) {
      this.token = token
      setToken(token)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      removeToken()
      router.push('/login')
    },
    async fetchUserInfo() {
      try {
        const info = await getUserInfo()
        this.userInfo = info
        this.roles = info.roles || []
      } catch {
        this.userInfo = null
        this.roles = []
      }
    }
  }
})
