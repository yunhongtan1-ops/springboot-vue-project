<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import SectionCard from '@/components/SectionCard.vue'
import { getPracticeHistory } from '@/api/practice'
import { getStoredUser } from '@/utils/auth'
import { getLatestPracticeRecord } from '@/utils/practice'

const route = useRoute()

const currentUser = ref(getStoredUser())
const history = ref([])
const fallbackRecord = ref(getLatestPracticeRecord())
const loading = ref(false)
const errorMessage = ref('')

function refreshUser() {
  currentUser.value = getStoredUser()
  fallbackRecord.value = getLatestPracticeRecord()
  loadHistory()
}

function formatSubmittedAt(value) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
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
    history.value = await getPracticeHistory(currentUser.value.username)
  } catch (error) {
    history.value = []
    errorMessage.value = error.message
  } finally {
    loading.value = false
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

const reviewSnapshot = computed(() => {
  if (!selectedRecord.value) {
    return null
  }

  const answer = selectedRecord.value.answerContent.trim()
  const answerLength = answer.length
  const paragraphCount = answer ? answer.split(/\n+/).filter(Boolean).length : 0
  const mentionsComplexity = /O\(|复杂度|complexity/i.test(answer)
  const mentionsEdgeCases = /边界|异常|null|空数组|空链表|corner|edge/i.test(answer)

  let status = '需要补充'
  let summary = '回答还比较短，建议先把解题思路和核心步骤讲完整。'

  if (answerLength >= 240) {
    status = '结构较完整'
    summary = '本次回答已经有比较完整的展开，可以继续补强边界条件和表达顺序。'
  } else if (answerLength >= 120) {
    status = '基本成型'
    summary = '已经具备主干内容，继续补充复杂度和关键边界会更像真实面试表达。'
  }

  const strengths = [
    paragraphCount > 1 ? '答案已经拆成多段，阅读体验比一整段更好。' : '至少已经形成了一版完整回答，可以继续迭代。',
    mentionsComplexity ? '你已经提到了复杂度，这是技术面试里非常重要的一部分。' : '你先把主思路写了出来，便于后续继续细化。',
    answerLength >= 120 ? '回答长度已经足够支撑一次基础复盘。' : '当前答案简洁，适合继续往上补充关键细节。',
  ]

  const improvements = [
    mentionsComplexity ? '下一步把为什么这样设计讲得更清楚，而不是只给出复杂度结论。' : '建议补上时间复杂度和空间复杂度分析。',
    mentionsEdgeCases ? '边界情况已经提到了一部分，可以再补上异常输入或极端场景。' : '建议明确边界条件，例如空输入、重复元素或异常场景。',
    paragraphCount > 1 ? '可以在每段开头先给一句结论，让表达更像口述作答。' : '建议把答案拆成“思路、步骤、复杂度、边界”四个层次。',
  ]

  const followUps = [
    '如果面试官追问更优解或替代方案，你会怎么继续展开？',
    '如果需要手写代码，变量命名和返回结构会如何组织？',
    '如果输入规模继续增大，这个方案的瓶颈会出现在哪里？',
  ]

  return {
    status,
    summary,
    answerLength,
    paragraphCount,
    strengths,
    improvements,
    followUps,
  }
})

const recentRecords = computed(() => {
  return history.value.filter((item) => item.id !== selectedRecord.value?.id).slice(0, 4)
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
    <SectionCard
      v-if="!currentUser"
      eyebrow="Review"
      title="复盘页"
      description="登录并提交答案后，这里会展示最近一次真实提交。"
    >
      <div class="empty-panel">当前未登录，先去做题并提交一条答案。</div>
    </SectionCard>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <section v-if="currentUser" class="review-layout">
      <SectionCard
        eyebrow="User Answer"
        title="本次提交"
        description="左侧展示真实提交内容，当前为 MVP 规则版复盘，不是 AI 评分。"
      >
        <div v-if="loading && !selectedRecord" class="empty-panel">提交记录加载中...</div>

        <div v-else-if="selectedRecord" class="question-detail-wrap">
          <div class="question-header-row">
            <div>
              <p class="question-index">Record #{{ selectedRecord.id }}</p>
              <h2>{{ selectedRecord.questionTitle }}</h2>
            </div>
            <div class="pill-row">
              <span class="meta-pill">题目 #{{ selectedRecord.questionId }}</span>
              <span class="meta-pill">{{ formatSubmittedAt(selectedRecord.submittedAt) }}</span>
            </div>
          </div>

          <div class="answer-paper">
            <pre>{{ selectedRecord.answerContent }}</pre>
          </div>
        </div>

        <div v-else class="empty-panel">还没有可复盘的记录，先去做题页提交一次答案。</div>
      </SectionCard>

      <div v-if="selectedRecord && reviewSnapshot" class="review-side">
        <SectionCard
          eyebrow="Review Snapshot"
          title="当前复盘概览"
          description="基于答案长度和结构的规则版提示，后续可替换为 AI 点评结果。"
        >
          <div class="insight-grid">
            <div class="insight-card">
              <span>当前状态</span>
              <p>{{ reviewSnapshot.status }}</p>
            </div>
            <div class="insight-card">
              <span>答案长度</span>
              <p>{{ reviewSnapshot.answerLength }} 字，{{ reviewSnapshot.paragraphCount }} 段</p>
            </div>
          </div>

          <p class="section-note">{{ reviewSnapshot.summary }}</p>
        </SectionCard>

        <SectionCard eyebrow="Strengths" title="做得不错的地方" description="先保留这些表达习惯。">
          <ul class="detail-list">
            <li v-for="item in reviewSnapshot.strengths" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard eyebrow="Improvements" title="下一步可以加强" description="继续补齐主流程中的薄弱点。">
          <ul class="detail-list">
            <li v-for="item in reviewSnapshot.improvements" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard eyebrow="Follow-up" title="继续追问自己" description="这些问题适合你做二次复盘。">
          <ul class="detail-list">
            <li v-for="item in reviewSnapshot.followUps" :key="item">{{ item }}</li>
          </ul>
        </SectionCard>

        <SectionCard
          v-if="recentRecords.length"
          eyebrow="Recent Records"
          title="切换其他提交"
          description="这里展示最近几条历史记录。"
        >
          <div class="record-list compact-records">
            <article v-for="item in recentRecords" :key="item.id" class="record-item tight-record">
              <div class="record-item-main">
                <strong>{{ item.questionTitle }}</strong>
                <p>#{{ item.id }} · {{ formatSubmittedAt(item.submittedAt) }}</p>
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
