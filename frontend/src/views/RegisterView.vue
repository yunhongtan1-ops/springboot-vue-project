<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { register } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

async function handleRegister() {
  errorMessage.value = ''
  successMessage.value = ''
  loading.value = true

  try {
    await register(form)
    successMessage.value = '注册成功，正在跳转到登录页。'
    window.setTimeout(() => {
      router.push('/login')
    }, 900)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-layout">
    <section class="auth-aside panel-surface">
      <p class="eyebrow">创建账号</p>
      <h2>建立你的训练身份</h2>
      <p class="hero-text">
        注册完成后即可开始提交答案、查看 AI 点评，并在学习进度里沉淀自己的练习轨迹。
      </p>
      <div class="auth-points">
        <div class="auth-point">
          <strong>01</strong>
          <span>保存个人练习记录，后续可持续回看。</span>
        </div>
        <div class="auth-point">
          <strong>02</strong>
          <span>提交答案后自动进入复盘链路。</span>
        </div>
        <div class="auth-point">
          <strong>03</strong>
          <span>通过进度页观察最近 7 天的训练节奏。</span>
        </div>
      </div>
    </section>

    <section class="auth-panel panel-surface">
      <p class="eyebrow">新用户</p>
      <h2>注册</h2>

      <form class="auth-form" @submit.prevent="handleRegister">
        <label class="editor-field">
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名" />
        </label>

        <label class="editor-field">
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </label>

        <label class="editor-field">
          <span>昵称</span>
          <input v-model.trim="form.nickname" type="text" placeholder="请输入昵称，可选" />
        </label>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        <p v-if="successMessage" class="success-text">{{ successMessage }}</p>

        <button type="submit" class="button primary-button wide-button" :disabled="loading">
          {{ loading ? '注册中...' : '创建账号' }}
        </button>
      </form>

      <p class="switch-link">
        已有账号？<RouterLink to="/login">返回登录</RouterLink>
      </p>
    </section>
  </div>
</template>