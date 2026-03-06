<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { submitPracticeAnswer } from '@/api/practice'
import { getQuestionDetail, getQuestionList } from '@/api/question'
import SectionCard from '@/components/SectionCard.vue'
import { getStoredUser } from '@/utils/auth'
import { getLatestPracticeRecord, saveLatestPracticeRecord } from '@/utils/practice'

const route = useRoute()
const router = useRouter()

const questions = ref([])
const question = ref(null)
const currentUser = ref(getStoredUser())
const latestRecord = ref(getLatestPracticeRecord())
const loadingList = ref(false)
const loadingDetail = ref(false)
const submitting = ref(false)
const pageError = ref('')
const submitError = ref('')
const successMessage = ref('')
const answerDraft = ref('')

const selectedId = computed(() => String(route.params.id || ''))
const canSubmit = computed(() => {
  return Boolean(currentUser.value && question.value && answerDraft.value.trim() && !submitting.value)
})

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
  if (!selectedId.value) {
    return
  }

  localStorage.setItem(draftKey(selectedId.value), answerDraft.value)
  successMessage.value = '草稿已保存在当前浏览器中。'
  submitError.value = ''
}

function clearDraft() {
  answerDraft.value = ''
  successMessage.value = ''
  if (selectedId.value) {
    localStorage.removeItem(draftKey(selectedId.value))
  }
}

async function loadQuestions() {
  loadingList.value = true
  pageError.value = ''

  try {
    const data = await getQuestionList()
    questions.value = data

    if (!route.params.id && data.length) {
      router.replace(`/practice/${data[0].id}`)
    }
  } catch (error) {
    pageError.value = error.message
  } finally {
    loadingList.value = false
  }
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

function selectQuestion(id) {
  router.push(`/practice/${id}`)
}

async function handleSubmit() {
  submitError.value = ''
  successMessage.value = ''

  if (!currentUser.value) {
    submitError.value = '请先登录，再提交你的答案。'
    return
  }

  if (!question.value) {
    submitError.value = '请先选择一道题目。'
    return
  }

  if (!answerDraft.value.trim()) {
    submitError.value = '请先填写答案内容。'
    return
  }

  submitting.value = true

  try {
    const record = await submitPracticeAnswer({
      username: currentUser.value.username,
      questionId: question.value.id,
      answerContent: answerDraft.value.trim(),
    })

    latestRecord.value = record
    saveLatestPracticeRecord(record)
    localStorage.removeItem(draftKey(String(record.questionId)))
    answerDraft.value = record.answerContent
    successMessage.value = '答案已提交，正在跳转到复盘页。'

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
  loadQuestions()
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
    <section class="workspace-grid">
      <SectionCard
        class="question-rail"
        eyebrow="Question List"
        title="题目目录"
        description="左侧题库来自当前后端接口。"
      >
        <template #actions>
          <button
            type="button"
            class="button ghost-button small-button"
            :disabled="loadingList"
            @click="loadQuestions"
          >
            刷新题库
          </button>
        </template>

        <p v-if="loadingList" class="muted-text">题目加载中...</p>
        <p v-if="pageError" class="error-text">{{ pageError }}</p>

        <div class="question-rail-list">
          <button
            v-for="item in questions"
            :key="item.id"
            type="button"
            class="question-list-item"
            :class="{ active: String(item.id) === selectedId }"
            @click="selectQuestion(item.id)"
          >
            <div class="list-item-top">
              <strong>{{ item.title }}</strong>
              <span class="tiny-pill">#{{ item.id }}</span>
            </div>
            <p>{{ item.content }}</p>
            <div class="pill-row">
              <span class="meta-pill">{{ item.type }}</span>
              <span class="meta-pill">{{ item.difficulty }}</span>
            </div>
          </button>
        </div>
      </SectionCard>

      <SectionCard
        class="question-main"
        eyebrow="Question Detail"
        title="题目详情"
        description="中间区域用于阅读题目和组织作答思路。"
      >
        <div v-if="loadingDetail" class="empty-panel">题目详情加载中...</div>

        <div v-else-if="question" class="question-detail-wrap">
          <div class="question-header-row">
            <div>
              <p class="question-index">Question {{ question.id }}</p>
              <h2>{{ question.title }}</h2>
            </div>
            <div class="pill-row">
              <span class="meta-pill bright">{{ question.type }}</span>
              <span class="meta-pill">{{ question.difficulty }}</span>
            </div>
          </div>

          <p class="question-body">{{ question.content }}</p>

          <div class="insight-grid">
            <div class="insight-card">
              <span>作答建议</span>
              <p>先给结论，再补思路、复杂度和边界条件，方便后续复盘。</p>
            </div>
            <div class="insight-card">
              <span>提交说明</span>
              <p>现在支持把答案提交到后端，并在复盘页查看最近一次真实记录。</p>
            </div>
          </div>
        </div>

        <div v-else class="empty-panel">从左侧选择一道题开始练习。</div>
      </SectionCard>

      <div class="answer-column">
        <SectionCard
          eyebrow="Answer Zone"
          title="作答区"
          description="右侧支持草稿保存和正式提交。"
        >
          <p v-if="!currentUser" class="muted-text">
            当前未登录。你仍然可以保存本地草稿，但提交答案前需要先登录。
          </p>

          <label class="editor-field">
            <span>你的答案</span>
            <textarea
              v-model="answerDraft"
              rows="13"
              placeholder="在这里组织你的回答结构、思路、复杂度分析和关键边界条件。"
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

        <SectionCard
          eyebrow="Latest Submission"
          title="最近一次提交"
          description="这里展示当前浏览器保存的最近一条真实提交记录。"
        >
          <div v-if="latestRecord" class="record-list">
            <article class="record-item">
              <div class="record-item-main">
                <strong>{{ latestRecord.questionTitle }}</strong>
                <p>记录 #{{ latestRecord.id }} · {{ formatSubmittedAt(latestRecord.submittedAt) }}</p>
              </div>
              <span class="score-badge">{{ latestRecord.answerContent.length }}</span>
            </article>

            <div class="insight-card">
              <span>答案摘要</span>
              <p>
                {{ latestRecord.answerContent.slice(0, 120) }}{{ latestRecord.answerContent.length > 120 ? '...' : '' }}
              </p>
            </div>

            <div class="action-row">
              <button
                type="button"
                class="button primary-button small-button"
                @click="router.push({ path: '/review', query: { recordId: String(latestRecord.id) } })"
              >
                查看复盘
              </button>
            </div>
          </div>

          <div v-else class="empty-panel">提交第一条答案后，这里会显示最近一次记录。</div>
        </SectionCard>
      </div>
    </section>
  </div>
</template>
