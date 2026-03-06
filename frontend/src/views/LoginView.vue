<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { saveAuth } from '@/utils/auth'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: '',
})

function resolveRedirectTarget() {
  return typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
    ? route.query.redirect
    : '/profile'
}

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    const data = await login(form)
    saveAuth(data)
    window.dispatchEvent(new Event('auth-changed'))
    router.push(resolveRedirectTarget())
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
      <p class="eyebrow">账号登录</p>
      <h2>回到你的面试训练工作台</h2>
      <p class="hero-text">
        登录后可以继续上次练习，查看最近复盘，并在个人中心同步你的真实训练记录。
      </p>
      <div class="auth-points">
        <div class="auth-point">
          <strong>01</strong>
          <span>继续刷题，沿用之前保存的思路和草稿。</span>
        </div>
        <div class="auth-point">
          <strong>02</strong>
          <span>直接查看最近一次提交对应的 AI 点评。</span>
        </div>
        <div class="auth-point">
          <strong>03</strong>
          <span>在学习进度和个人中心里追踪训练节奏。</span>
        </div>
      </div>
    </section>

    <section class="auth-panel panel-surface">
      <p class="eyebrow">欢迎回来</p>
      <h2>登录</h2>
      <p v-if="route.query.redirect" class="section-note">登录成功后会返回你刚才要访问的页面。</p>

      <form class="auth-form" @submit.prevent="handleLogin">
        <label class="editor-field">
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名" />
        </label>

        <label class="editor-field">
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" />
        </label>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

        <button type="submit" class="button primary-button wide-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录并进入平台' }}
        </button>
      </form>

      <p class="switch-link">
        还没有账号？<RouterLink to="/register">立即注册</RouterLink>
      </p>
    </section>
  </div>
</template>