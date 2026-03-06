<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getQuestionList } from '@/api/question'
import { getStoredUser } from '@/utils/auth'

const loading = ref(false)
const errorMessage = ref('')
const questions = ref([])
const currentUser = ref(getStoredUser())

const userLabel = computed(() => {
  if (!currentUser.value) {
    return '当前未登录，也可以直接查看题目。'
  }

  return `当前登录用户：${currentUser.value.nickname || currentUser.value.username}`
})

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
        <p class="page-desc">调用接口：<code>GET /api/questions</code></p>
      </div>
      <button type="button" class="small-button" @click="loadQuestions" :disabled="loading">刷新</button>
    </div>

    <p class="info-text">{{ userLabel }}</p>
    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    <p v-if="loading" class="info-text">题目加载中...</p>

    <div v-if="!loading && questions.length" class="question-list">
      <div v-for="question in questions" :key="question.id" class="question-item">
        <h3>{{ question.title }}</h3>
        <p>{{ question.content }}</p>
        <div class="question-meta">
          <span>类型：{{ question.type }}</span>
          <span>难度：{{ question.difficulty }}</span>
        </div>
        <RouterLink :to="`/questions/${question.id}`" class="detail-link">查看详情</RouterLink>
      </div>
    </div>

    <p v-if="!loading && !questions.length && !errorMessage" class="info-text">当前没有题目数据。</p>
  </section>
</template>
