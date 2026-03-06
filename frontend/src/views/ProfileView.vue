<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import SectionCard from '@/components/SectionCard.vue'
import { getPracticeHistory } from '@/api/practice'
import { getStoredUser } from '@/utils/auth'

const currentUser = ref(getStoredUser())
const history = ref([])
const loading = ref(false)
const errorMessage = ref('')

function refreshUser() {
  currentUser.value = getStoredUser()
  loadHistory()
}

function formatSubmittedAt(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

async function loadHistory() {
  errorMessage.value = ''

  if (!currentUser.value?.username) {
    history.value = []
    return
  }

  loading.value = true

  try {
    history.value = await getPracticeHistory()
  } catch (error) {
    history.value = []
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

const profileName = computed(() => {
  if (!currentUser.value) {
    return '访客模式'
  }
  return currentUser.value.nickname || currentUser.value.username
})

const profileSubline = computed(() => {
  if (!currentUser.value) {
    return '登录后可以查看自己的练习记录和复盘入口。'
  }

  return `用户名：${currentUser.value.username}`
})

const summaryItems = computed(() => {
  const records = history.value
  const latestRecord = records[0]
  const uniqueQuestions = new Set(records.map((item) => item.questionId)).size
  const totalAnswerLength = records.reduce((sum, item) => sum + item.answerContent.length, 0)

  return [
    `累计提交：${records.length} 次`,
    `覆盖题目：${uniqueQuestions} 道`,
    `累计答题字数：${totalAnswerLength} 字`,
    latestRecord ? `最近一次提交：${formatSubmittedAt(latestRecord.submittedAt)}` : '最近一次提交：暂无',
  ]
})

const recentSubmissions = computed(() => history.value.slice(0, 5))

const recentQuestions = computed(() => {
  const seen = new Set()
  return history.value.filter((item) => {
    if (seen.has(item.questionId)) {
      return false
    }
    seen.add(item.questionId)
    return true
  }).slice(0, 5)
})

onMounted(() => {
  loadHistory()
  window.addEventListener('auth-changed', refreshUser)
  window.addEventListener('storage', refreshUser)
})

onBeforeUnmount(() => {
  window.removeEventListener('auth-changed', refreshUser)
  window.removeEventListener('storage', refreshUser)
})
</script>

<template>
  <div class="page-stack">
    <section class="profile-hero panel-surface">
      <div class="profile-main-card">
        <div class="profile-avatar">{{ profileName.slice(0, 1).toUpperCase() }}</div>
        <div>
          <p class="eyebrow">用户中心</p>
          <h2>{{ profileName }}</h2>
          <p class="hero-text">{{ profileSubline }}</p>
        </div>
      </div>

      <div class="profile-actions">
        <RouterLink v-if="!currentUser" to="/login" class="button ghost-button">去登录</RouterLink>
        <RouterLink v-if="!currentUser" to="/register" class="button primary-button">去注册</RouterLink>
        <RouterLink v-if="currentUser" to="/practice" class="button primary-button">继续刷题</RouterLink>
      </div>
    </section>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <SectionCard
      v-if="!currentUser"
      eyebrow="个人中心"
      title="个人中心"
      description="登录后会显示你的提交摘要、最近题目和复盘入口。"
    >
      <div class="empty-panel">当前未登录，个人中心暂时没有真实数据。</div>
    </SectionCard>

    <section v-else class="content-grid three-cols">
      <SectionCard eyebrow="练习摘要" title="训练概览" description="基于最近提交汇总你的练习情况。">
        <div v-if="loading" class="empty-panel">练习记录加载中...</div>

        <ul v-else class="detail-list compact-list">
          <li v-for="item in summaryItems" :key="item">{{ item }}</li>
        </ul>
      </SectionCard>

      <SectionCard eyebrow="最近题目" title="近期练习题目" description="按最近提交去重展示。">
        <div v-if="loading" class="empty-panel">练习记录加载中...</div>

        <ul v-else-if="recentQuestions.length" class="detail-list compact-list">
          <li v-for="item in recentQuestions" :key="item.questionId">
            {{ item.questionTitle }} · 题目 #{{ item.questionId }}
          </li>
        </ul>

        <div v-else class="empty-panel">还没有题目练习记录。</div>
      </SectionCard>

      <SectionCard eyebrow="最近提交" title="复盘入口" description="可直接跳到对应复盘页。">
        <div v-if="loading" class="empty-panel">练习记录加载中...</div>

        <div v-else-if="recentSubmissions.length" class="record-list compact-records">
          <article v-for="item in recentSubmissions" :key="item.id" class="record-item tight-record">
            <div class="record-item-main">
              <strong>{{ item.questionTitle }}</strong>
              <p>#{{ item.id }} · {{ formatSubmittedAt(item.submittedAt) }}</p>
            </div>
            <RouterLink
              :to="{ path: '/review', query: { recordId: String(item.id) } }"
              class="tiny-pill"
            >
              复盘
            </RouterLink>
          </article>
        </div>

        <div v-else class="empty-panel">提交第一条答案后，这里会出现复盘入口。</div>
      </SectionCard>
    </section>
  </div>
</template>