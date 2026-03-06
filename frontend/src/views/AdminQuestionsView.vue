<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import MetricCard from '@/components/MetricCard.vue'
import SectionCard from '@/components/SectionCard.vue'
import {
  createAdminQuestion,
  deleteAdminQuestion,
  getQuestionList,
  updateAdminQuestion,
} from '@/api/question'
import { getStoredUser, isAdminUser } from '@/utils/auth'

const currentUser = ref(getStoredUser())
const questions = ref([])
const loading = ref(false)
const saving = ref(false)
const deletingId = ref(null)
const pageError = ref('')
const formError = ref('')
const successMessage = ref('')
const searchKeyword = ref('')
const filterType = ref('ALL')
const filterDifficulty = ref('ALL')

const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 'ALGORITHM',
  difficulty: 'EASY',
})

const typeOptions = [
  { label: '全部题型', value: 'ALL' },
  { label: '算法题', value: 'ALGORITHM' },
  { label: '面试题', value: 'INTERVIEW' },
  { label: '系统设计', value: 'SYSTEM_DESIGN' },
]

const difficultyOptions = [
  { label: '全部难度', value: 'ALL' },
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' },
]

const questionTypeLabels = {
  ALGORITHM: '算法题',
  INTERVIEW: '面试题',
  SYSTEM_DESIGN: '系统设计',
}

const difficultyLabels = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

const isAdmin = computed(() => isAdminUser(currentUser.value))
const isEditing = computed(() => Boolean(form.id))

const metrics = computed(() => {
  const algorithmCount = questions.value.filter((item) => item.type === 'ALGORITHM').length
  const interviewCount = questions.value.filter((item) => item.type === 'INTERVIEW').length
  const designCount = questions.value.filter((item) => item.type === 'SYSTEM_DESIGN').length

  return [
    { label: '题目总数', value: String(questions.value.length), hint: '当前题库总量', tone: 'primary' },
    { label: '筛选结果', value: String(filteredQuestions.value.length), hint: '当前搜索与筛选结果', tone: 'violet' },
    { label: '算法题', value: String(algorithmCount), hint: '适合热身与高频练习', tone: 'cyan' },
    {
      label: '面试 / 设计',
      value: String(interviewCount + designCount),
      hint: '表达题与系统设计题',
      tone: 'default',
    },
  ]
})

const filteredQuestions = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return [...questions.value]
    .sort((left, right) => right.id - left.id)
    .filter((item) => {
      const matchesKeyword =
        !keyword
        || item.title.toLowerCase().includes(keyword)
        || item.content.toLowerCase().includes(keyword)

      const matchesType = filterType.value === 'ALL' || item.type === filterType.value
      const matchesDifficulty = filterDifficulty.value === 'ALL' || item.difficulty === filterDifficulty.value

      return matchesKeyword && matchesType && matchesDifficulty
    })
})

function refreshUser() {
  currentUser.value = getStoredUser()
  if (isAdmin.value) {
    loadQuestions()
    return
  }

  questions.value = []
}

function formatQuestionType(value) {
  return questionTypeLabels[value] || value || '未分类'
}

function formatDifficulty(value) {
  return difficultyLabels[value] || value || '未知'
}

function resetFilters() {
  searchKeyword.value = ''
  filterType.value = 'ALL'
  filterDifficulty.value = 'ALL'
}

function resetForm() {
  form.id = null
  form.title = ''
  form.content = ''
  form.type = 'ALGORITHM'
  form.difficulty = 'EASY'
  formError.value = ''
}

function fillForm(question) {
  form.id = question.id
  form.title = question.title
  form.content = question.content
  form.type = question.type
  form.difficulty = question.difficulty
  formError.value = ''
  successMessage.value = `已载入题目 #${question.id}，可直接编辑后保存。`
}

function validateForm() {
  if (!form.title.trim()) {
    return '题目标题不能为空。'
  }
  if (!form.content.trim()) {
    return '题目内容不能为空。'
  }
  if (!form.type) {
    return '请选择题目类型。'
  }
  if (!form.difficulty) {
    return '请选择题目难度。'
  }
  return ''
}

async function loadQuestions() {
  if (!isAdmin.value) {
    return
  }

  loading.value = true
  pageError.value = ''

  try {
    questions.value = await getQuestionList()
  } catch (error) {
    questions.value = []
    pageError.value = error.message
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  formError.value = ''
  pageError.value = ''
  successMessage.value = ''

  const validationMessage = validateForm()
  if (validationMessage) {
    formError.value = validationMessage
    return
  }

  saving.value = true

  try {
    const payload = {
      title: form.title.trim(),
      content: form.content.trim(),
      type: form.type,
      difficulty: form.difficulty,
    }

    const saved = isEditing.value
      ? await updateAdminQuestion(form.id, payload)
      : await createAdminQuestion(payload)

    await loadQuestions()
    fillForm(saved)
    successMessage.value = isEditing.value
      ? `题目 #${saved.id} 已更新。`
      : `题目 #${saved.id} 已创建。`
  } catch (error) {
    formError.value = error.message
  } finally {
    saving.value = false
  }
}

async function handleDelete(question) {
  const confirmed = window.confirm(`确认删除题目《${question.title}》吗？该操作不可撤销。`)
  if (!confirmed) {
    return
  }

  deletingId.value = question.id
  pageError.value = ''
  successMessage.value = ''

  try {
    await deleteAdminQuestion(question.id)
    if (form.id === question.id) {
      resetForm()
    }
    await loadQuestions()
    successMessage.value = `题目 #${question.id} 已删除。`
  } catch (error) {
    pageError.value = error.message
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  if (isAdmin.value) {
    loadQuestions()
  }
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
      v-if="!isAdmin"
      eyebrow="管理员后台"
      title="没有访问权限"
      description="只有管理员账号可以维护题库。"
    >
      <div class="empty-panel">请使用管理员账号登录后再访问本页。</div>
    </SectionCard>

    <template v-else>
      <section class="metric-grid four-cols">
        <MetricCard
          v-for="item in metrics"
          :key="item.label"
          :label="item.label"
          :value="item.value"
          :hint="item.hint"
          :tone="item.tone"
        />
      </section>

      <p v-if="pageError" class="error-text">{{ pageError }}</p>
      <p v-if="successMessage" class="success-text">{{ successMessage }}</p>

      <section class="content-grid two-cols">
        <SectionCard
          eyebrow="搜索筛选"
          title="题库列表"
          description="支持按关键词、题型、难度过滤，并可直接进入编辑状态。"
        >
          <div class="filter-grid">
            <label class="editor-field filter-field filter-span-2">
              <span>关键词</span>
              <input v-model.trim="searchKeyword" type="text" placeholder="搜索题目标题或题干内容" />
            </label>

            <label class="editor-field filter-field">
              <span>题型</span>
              <select v-model="filterType">
                <option v-for="option in typeOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>

            <label class="editor-field filter-field">
              <span>难度</span>
              <select v-model="filterDifficulty">
                <option v-for="option in difficultyOptions" :key="option.value" :value="option.value">
                  {{ option.label }}
                </option>
              </select>
            </label>
          </div>

          <div class="action-row">
            <button type="button" class="button ghost-button small-button" :disabled="loading" @click="loadQuestions">
              刷新题库
            </button>
            <button type="button" class="button ghost-button small-button" @click="resetFilters">
              重置筛选
            </button>
          </div>

          <div v-if="loading" class="empty-panel">题库加载中...</div>

          <div v-else-if="filteredQuestions.length" class="record-list admin-list">
            <article v-for="item in filteredQuestions" :key="item.id" class="admin-question-card">
              <div class="admin-item-header">
                <div class="record-item-main">
                  <p class="question-index">题目 #{{ item.id }}</p>
                  <strong>{{ item.title }}</strong>
                </div>
                <div class="admin-item-actions">
                  <button type="button" class="button ghost-button small-button" @click="fillForm(item)">
                    编辑
                  </button>
                  <button
                    type="button"
                    class="button ghost-button small-button danger-button"
                    :disabled="deletingId === item.id"
                    @click="handleDelete(item)"
                  >
                    {{ deletingId === item.id ? '删除中...' : '删除' }}
                  </button>
                </div>
              </div>

              <p class="admin-snippet">{{ item.content }}</p>

              <div class="pill-row">
                <span class="meta-pill">{{ formatQuestionType(item.type) }}</span>
                <span class="meta-pill">{{ formatDifficulty(item.difficulty) }}</span>
              </div>
            </article>
          </div>

          <div v-else class="empty-panel">当前筛选条件下没有题目。</div>
        </SectionCard>

        <SectionCard
          eyebrow="题目编辑器"
          :title="isEditing ? '编辑题目' : '创建题目'"
          description="支持新增题目、维护题干内容和调整题型、难度。"
        >
          <div class="admin-form-head">
            <p class="section-note">
              {{ isEditing ? `当前正在编辑题目 #${form.id}` : '当前为新建模式，可直接录入新题。' }}
            </p>
            <button type="button" class="button ghost-button small-button" @click="resetForm">
              {{ isEditing ? '切换为新建' : '清空表单' }}
            </button>
          </div>

          <form class="auth-form" @submit.prevent="handleSave">
            <label class="editor-field">
              <span>题目标题</span>
              <input v-model.trim="form.title" type="text" placeholder="例如：LRU Cache" />
            </label>

            <label class="editor-field">
              <span>题目内容</span>
              <textarea
                v-model="form.content"
                rows="11"
                placeholder="请输入题目描述、约束条件和你希望候选人回答的重点。"
              ></textarea>
            </label>

            <div class="filter-grid">
              <label class="editor-field filter-field">
                <span>题型</span>
                <select v-model="form.type">
                  <option v-for="option in typeOptions.slice(1)" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>

              <label class="editor-field filter-field">
                <span>难度</span>
                <select v-model="form.difficulty">
                  <option v-for="option in difficultyOptions.slice(1)" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
            </div>

            <p v-if="formError" class="error-text">{{ formError }}</p>

            <div class="form-actions">
              <button type="submit" class="button primary-button" :disabled="saving">
                {{ saving ? '保存中...' : isEditing ? '保存修改' : '创建题目' }}
              </button>
              <button type="button" class="button ghost-button" :disabled="saving" @click="resetForm">
                重置表单
              </button>
            </div>
          </form>
        </SectionCard>
      </section>
    </template>
  </div>
</template>