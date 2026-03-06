<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SectionCard from '@/components/SectionCard.vue'
import { getQuestionList } from '@/api/question'
import {
  extractQuestionSummary,
  extractQuestionTrack,
  formatDifficulty,
  formatQuestionType,
} from '@/utils/question'

const route = useRoute()
const router = useRouter()

const questions = ref([])
const loading = ref(false)
const pageError = ref('')
const searchKeyword = ref('')
const activeType = ref('ALL')
const activeDifficulty = ref('ALL')
const currentPage = ref(1)

const pageSize = 12

const typeOptions = [
  { value: 'ALL', label: '全部题目' },
  { value: 'ALGORITHM', label: '算法题' },
  { value: 'INTERVIEW', label: '面试题' },
]

const difficultyOptions = [
  { value: 'ALL', label: '全部难度' },
  { value: 'EASY', label: '简单' },
  { value: 'MEDIUM', label: '中等' },
  { value: 'HARD', label: '困难' },
]

function parsePage(value) {
  const page = Number.parseInt(value || '1', 10)
  return Number.isFinite(page) && page > 0 ? page : 1
}

function normalizeFilter(value, allowList, fallback) {
  return allowList.includes(value) ? value : fallback
}

function buildQueryObject() {
  const query = {}

  if (searchKeyword.value.trim()) {
    query.keyword = searchKeyword.value.trim()
  }

  if (activeType.value !== 'ALL') {
    query.type = activeType.value
  }

  if (activeDifficulty.value !== 'ALL') {
    query.difficulty = activeDifficulty.value
  }

  if (currentPage.value > 1) {
    query.page = String(currentPage.value)
  }

  return query
}

function isSameQuery(left, right) {
  const leftEntries = Object.entries(left)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => [key, String(Array.isArray(value) ? value[0] : value)])
    .sort(([a], [b]) => a.localeCompare(b))

  const rightEntries = Object.entries(right)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => [key, String(value)])
    .sort(([a], [b]) => a.localeCompare(b))

  return JSON.stringify(leftEntries) === JSON.stringify(rightEntries)
}

function syncRouteQuery() {
  const nextQuery = buildQueryObject()
  if (isSameQuery(route.query, nextQuery)) {
    return
  }

  router.replace({ path: '/practice', query: nextQuery })
}

function applyRouteQuery(query) {
  const allowedTypes = typeOptions.map((item) => item.value)
  const allowedDifficulties = difficultyOptions.map((item) => item.value)
  searchKeyword.value = typeof query.keyword === 'string' ? query.keyword : ''
  activeType.value = normalizeFilter(query.type, allowedTypes, 'ALL')
  activeDifficulty.value = normalizeFilter(query.difficulty, allowedDifficulties, 'ALL')
  currentPage.value = parsePage(query.page)
}

const filteredQuestions = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return questions.value.filter((question) => {
    const matchesType = activeType.value === 'ALL' || question.type === activeType.value
    const matchesDifficulty = activeDifficulty.value === 'ALL' || question.difficulty === activeDifficulty.value
    const matchesKeyword =
      !keyword ||
      question.title.toLowerCase().includes(keyword) ||
      extractQuestionTrack(question.content).toLowerCase().includes(keyword)

    return matchesType && matchesDifficulty && matchesKeyword
  })
})

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredQuestions.value.length / pageSize))
})

const pagedQuestions = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredQuestions.value.slice(start, start + pageSize)
})

const visiblePageNumbers = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  const normalizedStart = Math.max(1, end - 4)

  for (let page = normalizedStart; page <= end; page += 1) {
    pages.push(page)
  }

  return pages
})

const catalogStats = computed(() => {
  const algorithmCount = questions.value.filter((item) => item.type === 'ALGORITHM').length
  const interviewCount = questions.value.filter((item) => item.type === 'INTERVIEW').length

  return [
    { label: '题库总量', value: questions.value.length, hint: '当前可练习题目' },
    { label: '算法题', value: algorithmCount, hint: '偏思路与复杂度' },
    { label: '面试题', value: interviewCount, hint: '偏表达与理解' },
    { label: '当前结果', value: filteredQuestions.value.length, hint: '筛选后可见题目' },
  ]
})

const activeTypeLabel = computed(() => {
  return typeOptions.find((item) => item.value === activeType.value)?.label || '全部题目'
})

function updateFilters(options = {}) {
  if (Object.prototype.hasOwnProperty.call(options, 'keyword')) {
    searchKeyword.value = options.keyword
  }

  if (Object.prototype.hasOwnProperty.call(options, 'type')) {
    activeType.value = options.type
  }

  if (Object.prototype.hasOwnProperty.call(options, 'difficulty')) {
    activeDifficulty.value = options.difficulty
  }

  if (Object.prototype.hasOwnProperty.call(options, 'page')) {
    currentPage.value = options.page
  }

  syncRouteQuery()
}

function handleKeywordInput(event) {
  updateFilters({ keyword: event.target.value, page: 1 })
}

function handleTypeChange(value) {
  updateFilters({ type: value, page: 1 })
}

function handleDifficultyChange(event) {
  updateFilters({ difficulty: event.target.value, page: 1 })
}

function clearFilters() {
  updateFilters({ keyword: '', type: 'ALL', difficulty: 'ALL', page: 1 })
}

function goToPage(page) {
  const normalizedPage = Math.min(Math.max(page, 1), totalPages.value)
  updateFilters({ page: normalizedPage })
}

function openQuestion(questionId) {
  router.push({
    path: `/practice/${questionId}`,
    query: route.query,
  })
}

async function loadQuestions() {
  loading.value = true
  pageError.value = ''

  try {
    questions.value = await getQuestionList()
  } catch (error) {
    pageError.value = error.message
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query,
  (query) => {
    applyRouteQuery(query)
  },
  { immediate: true },
)

watch(totalPages, (value) => {
  if (currentPage.value > value) {
    currentPage.value = value
    syncRouteQuery()
  }
})

onMounted(() => {
  loadQuestions()
})
</script>

<template>
  <div class="page-stack">
    <section class="practice-page-head panel-surface">
      <div>
        <p class="eyebrow">刷题目录</p>
        <h2>先选题，再进入单题作答</h2>
        <p class="hero-text practice-head-text">
          题库先按算法题和面试题分类展示，只保留摘要信息。你先在这里选题，进入作答页后再集中答题，提交后自动跳转 AI 分析。
        </p>
      </div>

      <div class="catalog-stat-strip">
        <article v-for="item in catalogStats" :key="item.label" class="catalog-stat-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.hint }}</small>
        </article>
      </div>
    </section>

    <section class="practice-catalog-layout">
      <SectionCard
        class="catalog-sidebar"
        eyebrow="题目分类"
        title="按方向筛选"
        description="先缩小范围，再分页查看题目。"
      >
        <div class="catalog-tabs">
          <button
            v-for="item in typeOptions"
            :key="item.value"
            type="button"
            class="catalog-tab"
            :class="{ active: activeType === item.value }"
            @click="handleTypeChange(item.value)"
          >
            <span>{{ item.label }}</span>
          </button>
        </div>

        <label class="editor-field">
          <span>难度筛选</span>
          <select :value="activeDifficulty" @change="handleDifficultyChange">
            <option v-for="item in difficultyOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>

        <div class="insight-card">
          <span>当前结果</span>
          <p>
            当前筛选下共有 {{ filteredQuestions.length }} 道题，每页展示 {{ pageSize }} 道。
            适合先浏览目录，再进入单题挑战。
          </p>
        </div>

        <div class="insight-card">
          <span>推荐节奏</span>
          <p>先刷 3 到 5 道同类题，再统一回看 AI 点评，训练感会更强。</p>
        </div>

        <button type="button" class="button ghost-button small-button wide-button" @click="clearFilters">
          重置筛选
        </button>
      </SectionCard>

      <SectionCard
        class="catalog-main"
        eyebrow="题目列表"
        title="分页刷题"
        description="目录页只展示摘要，点击后进入单题作答。"
      >
        <template #actions>
          <button type="button" class="button ghost-button small-button" :disabled="loading" @click="loadQuestions">
            刷新题库
          </button>
        </template>

        <div class="catalog-toolbar">
          <label class="editor-field catalog-search">
            <span>搜索题目</span>
            <input
              :value="searchKeyword"
              type="text"
              placeholder="按题目标题或方向关键词搜索"
              @input="handleKeywordInput"
            />
          </label>

          <div class="catalog-toolbar-meta">
            <span class="tiny-pill">{{ activeTypeLabel }}</span>
            <span class="tiny-pill accent-pill">第 {{ currentPage }} / {{ totalPages }} 页</span>
          </div>
        </div>

        <p v-if="loading" class="muted-text">题库加载中...</p>
        <p v-else-if="pageError" class="error-text">{{ pageError }}</p>

        <div v-else-if="pagedQuestions.length" class="compact-list catalog-list">
          <article v-for="item in pagedQuestions" :key="item.id" class="catalog-question-card">
            <div class="catalog-card-head">
              <div class="catalog-card-copy">
                <div class="list-item-top">
                  <strong>{{ item.title }}</strong>
                  <span class="tiny-pill">#{{ item.id }}</span>
                </div>
                <p class="question-summary">{{ extractQuestionSummary(item) }}</p>
              </div>

              <button type="button" class="button primary-button small-button question-card-cta" @click="openQuestion(item.id)">
                开始挑战
              </button>
            </div>

            <div class="question-list-meta">
              <span class="meta-pill bright">{{ formatQuestionType(item.type) }}</span>
              <span class="meta-pill">{{ formatDifficulty(item.difficulty) }}</span>
              <span class="meta-pill">方向 {{ extractQuestionTrack(item.content) }}</span>
            </div>
          </article>
        </div>

        <div v-else class="empty-panel">
          当前筛选条件下没有题目，换一个分类或清空关键词后再试。
        </div>

        <div v-if="filteredQuestions.length" class="pager">
          <button
            type="button"
            class="button ghost-button small-button pager-button"
            :disabled="currentPage === 1"
            @click="goToPage(currentPage - 1)"
          >
            上一页
          </button>

          <button
            v-for="page in visiblePageNumbers"
            :key="page"
            type="button"
            class="pager-number"
            :class="{ active: currentPage === page }"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>

          <button
            type="button"
            class="button ghost-button small-button pager-button"
            :disabled="currentPage === totalPages"
            @click="goToPage(currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </SectionCard>
    </section>
  </div>
</template>