<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { saveAuth } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')

const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    const data = await login(form)
    saveAuth(data)
    window.dispatchEvent(new Event('auth-changed'))
    router.push('/profile')
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
      <p class="eyebrow">Sign In</p>
      <h2>回到你的面试训练工作台</h2>
      <p class="hero-text">
        登录后可以在用户中心看到本地保存的会话信息，并继续浏览新的刷题工作流页面。
      </p>
      <div class="auth-points">
        <div class="auth-point">
          <strong>01</strong>
          <span>刷题页保留后端题库接口</span>
        </div>
        <div class="auth-point">
          <strong>02</strong>
          <span>AI 点评页先展示高保真占位结构</span>
        </div>
        <div class="auth-point">
          <strong>03</strong>
          <span>后续可逐步接入真实点评与进度数据</span>
        </div>
      </div>
    </section>

    <section class="auth-panel panel-surface">
      <p class="eyebrow">Auth</p>
      <h2>登录</h2>
      <p class="section-note">调用接口：POST /api/auth/login</p>

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
          {{ loading ? '登录中...' : '登录进入平台' }}
        </button>
      </form>

      <p class="switch-link">
        还没有账号？<RouterLink to="/register">立即注册</RouterLink>
      </p>
    </section>
  </div>
</template>