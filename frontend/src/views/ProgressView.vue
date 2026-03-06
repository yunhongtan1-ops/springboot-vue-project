<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import MetricCard from '@/components/MetricCard.vue'
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

function startOfWeek(date) {
  const result = new Date(date)
  const day = result.getDay()
  const diff = day === 0 ? -6 : 1 - day
  result.setDate(result.getDate() + diff)
  result.setHours(0, 0, 0, 0)
  return result
}

async function loadHistory() {
  errorMessage.value = ''

  if (!currentUser.value?.username) {
    history.value = []
    return
  }

  loading.value = true

  try {
    history.value = await getPracticeHistory(currentUser.value.username)
  } catch (error) {
    history.value = []
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

const metrics = computed(() => {
  const records = history.value
  const total = records.length
  const uniqueQuestions = new Set(records.map((item) => item.questionId)).size
  const totalAnswerLength = records.reduce((sum, item) => sum + item.answerContent.length, 0)
  const avgAnswerLength = total ? Math.round(totalAnswerLength / total) : 0
  const weekStart = startOfWeek(new Date())
  const weekCount = records.filter((item) => new Date(item.submittedAt) >= weekStart).length

  return [
    { label: '累计提交', value: String(total), hint: '真实练习记录', tone: 'primary' },
    { label: '本周提交', value: String(weekCount), hint: '按当前周统计', tone: 'violet' },
    { label: '覆盖题目', value: String(uniqueQuestions), hint: '去重后的题目数', tone: 'cyan' },
    { label: '平均字数', value: String(avgAnswerLength), hint: '每次答案平均长度', tone: 'default' },
  ]
})

const trendData = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return Array.from({ length: 7 }, (_, index) => {
    const day = new Date(today)
    day.setDate(today.getDate() - (6 - index))
    const dayKey = day.toISOString().slice(0, 10)
    const count = history.value.filter((item) => {
      const submitted = new Date(item.submittedAt)
      if (Number.isNaN(submitted.getTime())) {
        return false
      }
      return submitted.toISOString().slice(0, 10) === dayKey
    }).length

    return {
      label: `${day.getMonth() + 1}/${day.getDate()}`,
      value: count,
    }
  })
})

const recentRecords = computed(() => history.value.slice(0, 6))

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
    <section v-if="currentUser" class="metric-grid four-cols">
      <MetricCard
        v-for="item in metrics"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
        :tone="item.tone"
      />
    </section>

    <SectionCard
      v-else
      eyebrow="Progress"
      title="练习进度"
      description="登录后即可查看真实提交趋势和最近练习记录。"
    >
      <div class="empty-panel">当前未登录，进度页暂时没有可展示的数据。</div>
    </SectionCard>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <section v-if="currentUser" class="content-grid two-cols">
      <SectionCard eyebrow="Trend" title="最近 7 天提交趋势" description="柱状图展示每天的提交次数。">
        <div v-if="loading" class="empty-panel">练习记录加载中...</div>

        <div v-else class="trend-chart">
          <div v-for="item in trendData" :key="item.label" class="trend-item">
            <div class="trend-bar-wrap">
              <div class="trend-bar" :style="{ height: `${item.value * 18 + 10}px` }"></div>
            </div>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </SectionCard>

      <SectionCard eyebrow="Recent Work" title="最近提交记录" description="当前展示最近 6 条真实提交。">
        <div v-if="loading" class="empty-panel">练习记录加载中...</div>

        <div v-else-if="recentRecords.length" class="record-list">
          <article v-for="item in recentRecords" :key="item.id" class="record-item">
            <div class="record-item-main">
              <strong>{{ item.questionTitle }}</strong>
              <p>题目 #{{ item.questionId }} · {{ formatSubmittedAt(item.submittedAt) }}</p>
            </div>
            <span class="score-badge">{{ item.answerContent.length }}</span>
          </article>
        </div>

        <div v-else class="empty-panel">还没有提交记录，先去完成第一道题。</div>
      </SectionCard>
    </section>
  </div>
</template>
