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
    successMessage.value = '注册成功，准备跳转到登录页。'
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
      <p class="eyebrow">Create Account</p>
      <h2>创建你的训练身份</h2>
      <p class="hero-text">
        当前注册逻辑仍然走现有后端接口，注册完成后即可用新账号进入整套深色工作台页面。
      </p>
      <div class="auth-points">
        <div class="auth-point">
          <strong>Tech</strong>
          <span>Vue 3 + Vite + Router + Axios</span>
        </div>
        <div class="auth-point">
          <strong>UI</strong>
          <span>深蓝灰背景 + 蓝紫强调色 + 高可读排版</span>
        </div>
        <div class="auth-point">
          <strong>Flow</strong>
          <span>首页 / 刷题 / 点评 / 进度 / 用户中心</span>
        </div>
      </div>
    </section>

    <section class="auth-panel panel-surface">
      <p class="eyebrow">Auth</p>
      <h2>注册</h2>
      <p class="section-note">调用接口：POST /api/auth/register</p>

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