<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { getHealth } from '@/api/common'
import { clearAuth, getStoredUser, isAdminUser } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const user = ref(getStoredUser())
const healthText = ref('后端检测中')
const healthClass = ref('status-checking')

const navItems = computed(() => {
  const items = [
    { label: '首页', to: '/', match: '/' },
    { label: '刷题页', to: '/practice', match: '/practice' },
    { label: 'AI 点评', to: '/review', match: '/review' },
    { label: '学习进度', to: '/progress', match: '/progress' },
    { label: '用户中心', to: '/profile', match: '/profile' },
  ]

  if (isAdminUser(user.value)) {
    items.push({ label: '题库管理', to: '/admin/questions', match: '/admin/questions' })
  }

  return items
})

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

const userStatusText = computed(() => {
  if (!user.value) {
    return '可先浏览页面'
  }
  return isAdminUser(user.value) ? '管理员会话' : '已登录会话'
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
          <p class="eyebrow">面试训练工作台</p>
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
            <small>{{ userStatusText }}</small>
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