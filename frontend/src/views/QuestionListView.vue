<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getQuestionList } from '@/api/question'
import { getStoredUser } from '@/utils/auth'

const loading = ref(false)
const errorMessage = ref('')
const questions = ref([])
const currentUser = ref(getStoredUser())

const typeLabels = {
  ALGORITHM: '算法题',
  INTERVIEW: '面试题',
  SYSTEM_DESIGN: '系统设计',
}

const difficultyLabels = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

const userLabel = computed(() => {
  if (!currentUser.value) {
    return '当前未登录，也可以先浏览题目内容。'
  }

  return `当前登录用户：${currentUser.value.nickname || currentUser.value.username}`
})

function formatType(value) {
  return typeLabels[value] || value || '未分类'
}

function formatDifficulty(value) {
  return difficultyLabels[value] || value || '未知'
}

async function loadQuestions() {
  errorMessage.value = ''
  loading.value = true

  try {
    questions.value = await getQuestionList()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadQuestions()
})
</script>

<template>
  <section class="page-card">
    <div class="page-header">
      <div>
        <h2>题目列表</h2>
        <p class="page-desc">按顺序浏览当前题库，选择后可进入详情页。</p>
      </div>
      <button type="button" class="small-button" :disabled="loading" @click="loadQuestions">刷新</button>
    </div>

    <p class="info-text">{{ userLabel }}</p>
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-if="loading" class="info-text">题目加载中...</p>

    <div v-if="!loading && questions.length" class="question-list">
      <div v-for="question in questions" :key="question.id" class="question-item">
        <h3>{{ question.title }}</h3>
        <p>{{ question.content }}</p>
        <div class="question-meta">
          <span>类型：{{ formatType(question.type) }}</span>
          <span>难度：{{ formatDifficulty(question.difficulty) }}</span>
        </div>
        <RouterLink :to="`/questions/${question.id}`" class="detail-link">查看详情</RouterLink>
      </div>
    </div>

    <p v-if="!loading && !questions.length && !errorMessage" class="info-text">当前没有题目数据。</p>
  </section>
</template>