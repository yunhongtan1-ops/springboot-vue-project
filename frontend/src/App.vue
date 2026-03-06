<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { getHealth } from '@/api/common'
import { clearAuth, getStoredUser } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const user = ref(getStoredUser())
const healthText = ref('Backend checking')
const healthClass = ref('status-checking')

const navItems = [
  { label: '首页', to: '/', match: '/' },
  { label: '刷题页', to: '/practice', match: '/practice' },
  { label: 'AI 点评', to: '/review', match: '/review' },
  { label: '学习进度', to: '/progress', match: '/progress' },
  { label: '用户中心', to: '/profile', match: '/profile' },
]

function refreshUser() {
  user.value = getStoredUser()
}

function isActive(match) {
  if (match === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(match)
}

async function loadHealth() {
  try {
    const data = await getHealth()
    healthText.value = `后端状态 ${data.status}`
    healthClass.value = data.status === 'UP' ? 'status-up' : 'status-down'
  } catch {
    healthText.value = '后端连接失败'
    healthClass.value = 'status-down'
  }
}

function handleLogout() {
  clearAuth()
  refreshUser()
  window.dispatchEvent(new Event('auth-changed'))
  router.push('/login')
}

const userName = computed(() => {
  if (!user.value) {
    return '访客模式'
  }
  return user.value.nickname || user.value.username
})

onMounted(() => {
  loadHealth()
  window.addEventListener('auth-changed', refreshUser)
  window.addEventListener('storage', refreshUser)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', refreshUser)
  window.removeEventListener('storage', refreshUser)
})
</script>

<template>
  <div class="app-shell">
    <div class="bg-orb bg-orb-one"></div>
    <div class="bg-orb bg-orb-two"></div>
    <div class="bg-grid"></div>

    <header class="app-topbar panel-surface">
      <div class="brand-block">
        <div class="brand-mark">AI</div>
        <div>
          <p class="eyebrow">Interview Workspace</p>
          <h1 class="brand-title">AI 面试刷题平台</h1>
        </div>
      </div>

      <nav class="top-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: isActive(item.match) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="topbar-actions">
        <span class="status-pill" :class="healthClass">{{ healthText }}</span>
        <div class="user-pill">
          <span class="user-avatar">{{ userName.slice(0, 1).toUpperCase() }}</span>
          <div>
            <strong>{{ userName }}</strong>
            <small>{{ user ? '已登录会话' : '可先浏览页面' }}</small>
          </div>
        </div>
        <div class="action-row">
          <RouterLink v-if="!user" to="/login" class="button ghost-button">登录</RouterLink>
          <RouterLink v-if="!user" to="/register" class="button primary-button">注册</RouterLink>
          <button v-if="user" type="button" class="button ghost-button" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>