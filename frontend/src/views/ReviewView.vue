<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import SectionCard from '@/components/SectionCard.vue'
import { getPracticeHistory, getPracticeReview } from '@/api/practice'
import { getStoredUser } from '@/utils/auth'
import { getLatestPracticeRecord } from '@/utils/practice'

const route = useRoute()

const currentUser = ref(getStoredUser())
const history = ref([])
const fallbackRecord = ref(getLatestPracticeRecord())
const practiceReview = ref(null)
const loading = ref(false)
const reviewLoading = ref(false)
const errorMessage = ref('')
const reviewErrorMessage = ref('')

const reviewStatusLabels = {
  PENDING: '生成中',
  SUCCESS: '已完成',
  FAILED: '生成失败',
}

let reviewPollTimer = null

function refreshUser() {
  currentUser.value = getStoredUser()
  fallbackRecord.value = getLatestPracticeRecord()
  loadHistory()
}

function stopReviewPolling() {
  if (reviewPollTimer) {
    window.clearTimeout(reviewPollTimer)
    reviewPollTimer = null
  }
}

function queueReviewPolling(recordId) {
  stopReviewPolling()
  reviewPollTimer = window.setTimeout(() => {
    loadReview(recordId, { silent: true })
  }, 1500)
}

function formatDateTime(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value || '刚刚'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function formatReviewStatus(status) {
  return reviewStatusLabels[status] || status || '生成中'
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

async function loadReview(recordId, options = {}) {
  const { silent = false } = options

  if (!currentUser.value?.username || !recordId) {
    practiceReview.value = null
    stopReviewPolling()
    return
  }

  if (!silent) {
    reviewLoading.value = true
  }
  reviewErrorMessage.value = ''

  try {
    const data = await getPracticeReview(recordId)
    practiceReview.value = data

    if (data?.status === 'PENDING') {
      queueReviewPolling(recordId)
    } else {
      stopReviewPolling()
    }
  } catch (error) {
    practiceReview.value = null
    reviewErrorMessage.value = error.message
    stopReviewPolling()
  } finally {
    if (!silent) {
      reviewLoading.value = false
    }
  }
}

const selectedRecord = computed(() => {
  const requestedId = Number(route.query.recordId || 0)

  if (history.value.length) {
    if (requestedId) {
      return history.value.find((item) => item.id === requestedId) || history.value[0]
    }

    return history.value[0]
  }

  if (!fallbackRecord.value) {
    return null
  }

  if (requestedId && fallbackRecord.value.id !== requestedId) {
    return null
  }

  return fallbackRecord.value
})

const reviewStatusMeta = computed(() => {
  const status = practiceReview.value?.status

  if (status === 'SUCCESS') {
    return {
      title: 'AI 点评已生成',
      description: '本次复盘已经整理出总结、亮点和改进建议。',
    }
  }

  if (status === 'FAILED') {
    return {
      title: 'AI 点评生成失败',
      description: '这条记录已经提交成功，但点评任务未完成，可稍后重试。',
    }
  }

  return {
    title: 'AI 点评生成中',
    description: '系统正在处理本次答案，页面会自动刷新结果。',
  }
})

const recentRecords = computed(() => {
  return history.value.filter((item) => item.id !== selectedRecord.value?.id).slice(0, 4)
})

watch(
  () => selectedRecord.value?.id || null,
  (recordId) => {
    reviewErrorMessage.value = ''
    practiceReview.value = null
    stopReviewPolling()

    if (recordId && currentUser.value?.username) {
      loadReview(recordId)
    }
  },
  { immediate: true },
)

onMounted(() => {
  loadHistory()
  window.addEventListener('auth-changed', refreshUser)
  window.addEventListener('storage', refreshUser)
})

onBeforeUnmount(() => {
  stopReviewPolling()
  window.removeEventListener('auth-changed', refreshUser)
  window.removeEventListener('storage', refreshUser)
})
</script>

<template>
  <div class="page-stack">
    <SectionCard
      v-if="!currentUser"
      eyebrow="复盘页"
      title="查看提交复盘"
      description="登录并提交答案后，这里会展示本次回答和 AI 点评。"
    >
      <div class="empty-panel">当前未登录，先去做题并提交一条答案。</div>
    </SectionCard>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-if="reviewErrorMessage" class="error-text">{{ reviewErrorMessage }}</p>

    <section v-if="currentUser" class="review-layout">
      <SectionCard
        eyebrow="本次回答"
        title="提交内容"
        description="左侧展示本次提交，右侧展示系统生成的点评结果。"
      >
        <div v-if="loading && !selectedRecord" class="empty-panel">提交记录加载中...</div>

        <div v-else-if="selectedRecord" class="question-detail-wrap">
          <div class="question-header-row">
            <div>
              <p class="question-index">记录 #{{ selectedRecord.id }}</p>
              <h2>{{ selectedRecord.questionTitle }}</h2>
            </div>
            <div class="pill-row">
              <span class="meta-pill">题目 #{{ selectedRecord.questionId }}</span>
              <span class="meta-pill">{{ formatDateTime(selectedRecord.submittedAt) }}</span>
            </div>
          </div>

          <div class="answer-paper">
            <pre>{{ selectedRecord.answerContent }}</pre>
          </div>
        </div>

        <div v-else class="empty-panel">还没有可复盘的记录，先去做题页提交一次答案。</div>
      </SectionCard>

      <div v-if="selectedRecord" class="review-side">
        <SectionCard
          eyebrow="AI 点评"
          :title="reviewStatusMeta.title"
          :description="reviewStatusMeta.description"
        >
          <div v-if="reviewLoading && !practiceReview" class="empty-panel">AI 点评加载中...</div>

          <template v-else>
            <div class="insight-grid">
              <div class="insight-card">
                <span>当前状态</span>
                <p>{{ formatReviewStatus(practiceReview?.status) }}</p>
              </div>
              <div class="insight-card">
                <span>建议分数</span>
                <p>{{ practiceReview?.overallScore ?? '--' }}</p>
              </div>
            </div>

            <p v-if="practiceReview?.status === 'SUCCESS'" class="section-note">
              {{ practiceReview.summary }}
            </p>
            <p v-else-if="practiceReview?.status === 'FAILED'" class="section-note">
              {{ practiceReview.errorMessage || '点评任务执行失败，请稍后刷新页面。' }}
            </p>
            <p v-else class="section-note">
              当前记录已经创建点评任务，页面会每 1.5 秒自动刷新一次结果。
            </p>

            <p v-if="practiceReview?.updatedAt" class="muted-text">
              最近更新时间：{{ formatDateTime(practiceReview.updatedAt) }}
            </p>
          </template>
        </SectionCard>

        <SectionCard
          v-if="practiceReview?.status === 'SUCCESS' && practiceReview.highlightExcerpt"
          eyebrow="答案摘录"
          title="快速回看"
          description="摘取一段关键内容，方便快速回顾本次回答。"
        >
          <p class="section-note">{{ practiceReview.highlightExcerpt }}</p>
        </SectionCard>

        <SectionCard
          v-if="practiceReview?.status === 'SUCCESS' && practiceReview.strengths?.length"
          eyebrow="亮点"
          title="这次做得好的地方"
          description="这些部分建议继续保留，并逐步形成稳定表达习惯。"
        >
          <ul class="detail-list">
            <li v-for="item in practiceReview.strengths" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard
          v-if="practiceReview?.status === 'SUCCESS' && practiceReview.improvements?.length"
          eyebrow="改进点"
          title="下一步可以加强的地方"
          description="优先补齐这些部分，会更接近真实面试中的高质量回答。"
        >
          <ul class="detail-list">
            <li v-for="item in practiceReview.improvements" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard
          v-if="practiceReview?.status === 'SUCCESS' && practiceReview.followUps?.length"
          eyebrow="追问建议"
          title="继续追问自己"
          description="可以把这些问题当成下一轮复盘的练习方向。"
        >
          <ul class="detail-list">
            <li v-for="item in practiceReview.followUps" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard
          v-if="recentRecords.length"
          eyebrow="最近记录"
          title="切换其他提交"
          description="这里展示最近几条历史记录，可随时切换复盘。"
        >
          <div class="record-list compact-records">
            <article v-for="item in recentRecords" :key="item.id" class="record-item tight-record">
              <div class="record-item-main">
                <strong>{{ item.questionTitle }}</strong>
                <p>#{{ item.id }} · {{ formatDateTime(item.submittedAt) }}</p>
              </div>
              <RouterLink
                :to="{ path: '/review', query: { recordId: String(item.id) } }"
                class="tiny-pill"
              >
                查看
              </RouterLink>
            </article>
          </div>
        </SectionCard>
      </div>
    </section>
  </div>
</template>