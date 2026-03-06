<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitPracticeAnswer } from '@/api/practice'
import { getQuestionDetail } from '@/api/question'
import SectionCard from '@/components/SectionCard.vue'
import { getStoredUser } from '@/utils/auth'
import { getLatestPracticeRecord, saveLatestPracticeRecord } from '@/utils/practice'
import {
  buildAnswerChecklist,
  extractQuestionPrompt,
  extractQuestionSummary,
  extractQuestionTrack,
  formatDifficulty,
  formatQuestionType,
} from '@/utils/question'

const route = useRoute()
const router = useRouter()

const question = ref(null)
const currentUser = ref(getStoredUser())
const latestRecord = ref(getLatestPracticeRecord())
const loadingDetail = ref(false)
const pageError = ref('')
const submitError = ref('')
const successMessage = ref('')
const submitting = ref(false)
const answerDraft = ref('')

const questionId = computed(() => String(route.params.id || ''))
const canSubmit = computed(() => {
  return Boolean(currentUser.value && question.value && answerDraft.value.trim() && !submitting.value)
})
const questionPrompt = computed(() => {
  return question.value ? extractQuestionPrompt(question.value.content, question.value.title) : ''
})
const questionSummary = computed(() => {
  return question.value ? extractQuestionSummary(question.value) : ''
})
const questionTrack = computed(() => {
  return question.value ? extractQuestionTrack(question.value.content) : '通用题目'
})
const answerChecklist = computed(() => buildAnswerChecklist(question.value))

function refreshUser() {
  currentUser.value = getStoredUser()
}

function draftKey(id) {
  return `practice-draft-${id}`
}

function formatSubmittedAt(value) {
  if (!value) {
    return '刚刚'
  }

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

function loadDraft(id) {
  answerDraft.value = id ? localStorage.getItem(draftKey(id)) || '' : ''
}

function saveDraft() {
  if (!questionId.value) {
    return
  }

  localStorage.setItem(draftKey(questionId.value), answerDraft.value)
  successMessage.value = '草稿已保存在当前浏览器中。'
  submitError.value = ''
}

function clearDraft() {
  answerDraft.value = ''
  successMessage.value = ''
  if (questionId.value) {
    localStorage.removeItem(draftKey(questionId.value))
  }
}

function goBackToCatalog() {
  router.push({
    path: '/practice',
    query: route.query,
  })
}

async function loadQuestion(id) {
  if (!id) {
    question.value = null
    return
  }

  loadingDetail.value = true
  pageError.value = ''
  successMessage.value = ''
  submitError.value = ''

  try {
    question.value = await getQuestionDetail(id)
    loadDraft(id)
  } catch (error) {
    question.value = null
    pageError.value = error.message
  } finally {
    loadingDetail.value = false
  }
}

async function handleSubmit() {
  submitError.value = ''
  successMessage.value = ''

  if (!currentUser.value) {
    submitError.value = '请先登录，再提交你的答案。'
    return
  }

  if (!question.value) {
    submitError.value = '当前题目不存在，请返回题目列表重新选择。'
    return
  }

  if (!answerDraft.value.trim()) {
    submitError.value = '请先填写答案内容。'
    return
  }

  submitting.value = true

  try {
    const record = await submitPracticeAnswer({
      questionId: question.value.id,
      answerContent: answerDraft.value.trim(),
    })

    latestRecord.value = record
    saveLatestPracticeRecord(record)
    localStorage.removeItem(draftKey(String(record.questionId)))
    answerDraft.value = record.answerContent
    successMessage.value = '答案已提交，正在跳转 AI 分析页。'

    router.push({
      path: '/review',
      query: {
        recordId: String(record.id),
      },
    })
  } catch (error) {
    submitError.value = error.message
  } finally {
    submitting.value = false
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (id) {
      loadQuestion(id)
    }
  },
  { immediate: true },
)

onMounted(() => {
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
    <section class="practice-page-head panel-surface practice-answer-head">
      <div>
        <p class="eyebrow">单题作答</p>
        <h2>阅读题目后，集中完成这一题</h2>
        <p class="hero-text practice-head-text">
          目录页负责选题，这里负责深度作答。提交后会自动进入 AI 分析页，不再和题目列表混在同一屏里。
        </p>
      </div>

      <div class="action-row">
        <button type="button" class="button ghost-button" @click="goBackToCatalog">返回题目列表</button>
        <button type="button" class="button ghost-button" :disabled="loadingDetail" @click="loadQuestion(questionId)">
          重新加载
        </button>
      </div>
    </section>

    <section class="practice-answer-layout">
      <div class="practice-answer-side">
        <SectionCard
          eyebrow="当前题目"
          :title="question ? question.title : '题目加载中'"
          description="左侧只保留必要的题目信息，方便边看边答。"
        >
          <div v-if="loadingDetail" class="empty-panel">题目详情加载中...</div>
          <p v-else-if="pageError" class="error-text">{{ pageError }}</p>

          <div v-else-if="question" class="question-detail-wrap">
            <div class="pill-row">
              <span class="meta-pill bright">{{ formatQuestionType(question.type) }}</span>
              <span class="meta-pill">{{ formatDifficulty(question.difficulty) }}</span>
              <span class="meta-pill">方向 {{ questionTrack }}</span>
            </div>

            <p class="question-body question-prompt">{{ questionPrompt }}</p>

            <div class="summary-stack">
              <div class="insight-card">
                <span>题目摘要</span>
                <p>{{ questionSummary }}</p>
              </div>

              <div class="insight-card">
                <span>作答重点</span>
                <ul class="detail-list answer-checklist">
                  <li v-for="item in answerChecklist" :key="item">{{ item }}</li>
                </ul>
              </div>

              <div class="insight-card">
                <span>提交说明</span>
                <p>点击提交后会直接进入 AI 分析页，查看这次回答的评分、亮点和改进建议。</p>
              </div>
            </div>
          </div>

          <div v-else class="empty-panel">当前题目不存在，请返回题目列表重新选择。</div>
        </SectionCard>

        <SectionCard
          eyebrow="最近提交"
          title="最近一次作答"
          description="方便快速回到刚刚那条练习记录。"
        >
          <div v-if="latestRecord" class="record-list">
            <article class="record-item">
              <div class="record-item-main">
                <strong>{{ latestRecord.questionTitle }}</strong>
                <p>记录 #{{ latestRecord.id }} · {{ formatSubmittedAt(latestRecord.submittedAt) }}</p>
              </div>
              <span class="score-badge">{{ latestRecord.answerContent.length }}</span>
            </article>

            <div class="action-row">
              <button
                type="button"
                class="button ghost-button small-button"
                @click="router.push({ path: '/review', query: { recordId: String(latestRecord.id) } })"
              >
                查看 AI 分析
              </button>
            </div>
          </div>

          <div v-else class="empty-panel">提交第一条答案后，这里会显示最近一次记录。</div>
        </SectionCard>
      </div>

      <div class="practice-answer-main">
        <SectionCard
          class="answer-editor-card"
          eyebrow="作答区"
          title="组织你的答案"
          description="右侧答题框更大，便于你完整表达和反复修改。"
        >
          <p v-if="!currentUser" class="muted-text">
            当前未登录。你仍然可以保存本地草稿，但正式提交前需要先登录。
          </p>

          <label class="editor-field">
            <span>你的回答</span>
            <textarea
              v-model="answerDraft"
              rows="18"
              placeholder="建议按“结论 -> 思路或核心概念 -> 关键细节 -> 复杂度或取舍 -> 边界条件 -> 总结”来组织。"
            ></textarea>
          </label>

          <p v-if="submitError" class="error-text">{{ submitError }}</p>
          <p v-if="successMessage" class="success-text">{{ successMessage }}</p>

          <div class="editor-footer">
            <small>{{ answerDraft.length }} 字</small>
            <div class="action-row">
              <button type="button" class="button ghost-button small-button" @click="clearDraft">清空</button>
              <button type="button" class="button ghost-button small-button" @click="saveDraft">保存草稿</button>
              <button
                type="button"
                class="button primary-button small-button"
                :disabled="!canSubmit"
                @click="handleSubmit"
              >
                {{ submitting ? '提交中...' : '提交答案' }}
              </button>
            </div>
          </div>

          <div v-if="!currentUser" class="action-row">
            <button type="button" class="button ghost-button small-button" @click="router.push('/login')">
              去登录
            </button>
          </div>
        </SectionCard>
      </div>
    </section>
  </div>
</template>