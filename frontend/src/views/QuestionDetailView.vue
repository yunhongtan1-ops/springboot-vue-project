<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getQuestionDetail } from '@/api/question'

const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const question = ref(null)

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

function formatType(value) {
  return typeLabels[value] || value || '未分类'
}

function formatDifficulty(value) {
  return difficultyLabels[value] || value || '未知'
}

const pageDescription = computed(() => {
  if (!question.value) {
    return '查看题目详情、类型和难度信息。'
  }

  return `当前查看题目 #${question.value.id}`
})

async function loadQuestionDetail(id) {
  errorMessage.value = ''
  loading.value = true

  try {
    question.value = await getQuestionDetail(id)
  } catch (error) {
    question.value = null
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (id) {
      loadQuestionDetail(id)
    }
  },
  { immediate: true },
)
</script>

<template>
  <section class="page-card">
    <div class="page-header">
      <div>
        <h2>题目详情</h2>
        <p class="page-desc">{{ pageDescription }}</p>
      </div>
      <RouterLink to="/questions" class="detail-link">返回列表</RouterLink>
    </div>

    <p v-if="loading" class="info-text">详情加载中...</p>
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <div v-if="question" class="detail-box">
      <h3>{{ question.title }}</h3>
      <div class="question-meta detail-meta">
        <span>题目 ID：{{ question.id }}</span>
        <span>类型：{{ formatType(question.type) }}</span>
        <span>难度：{{ formatDifficulty(question.difficulty) }}</span>
      </div>
      <p class="detail-content">{{ question.content }}</p>
    </div>
  </section>
</template>