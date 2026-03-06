import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import PracticeView from '@/views/PracticeView.vue'
import PracticeAnswerView from '@/views/PracticeAnswerView.vue'
import ReviewView from '@/views/ReviewView.vue'
import ProgressView from '@/views/ProgressView.vue'
import ProfileView from '@/views/ProfileView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import AdminQuestionsView from '@/views/AdminQuestionsView.vue'
import { getStoredUser, isAdminUser } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/practice',
      name: 'practice',
      component: PracticeView,
    },
    {
      path: '/practice/:id',
      name: 'practiceAnswer',
      component: PracticeAnswerView,
    },
    {
      path: '/review',
      name: 'review',
      component: ReviewView,
    },
    {
      path: '/progress',
      name: 'progress',
      component: ProgressView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/admin/questions',
      name: 'adminQuestions',
      component: AdminQuestionsView,
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/questions',
      redirect: '/practice',
    },
    {
      path: '/questions/:id',
      redirect: (to) => `/practice/${to.params.id}`,
    },
  ],
})

router.beforeEach((to) => {
  const user = getStoredUser()

  if (to.meta.requiresAuth && !user) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.requiresAdmin && !isAdminUser(user)) {
    return user
      ? { path: '/profile' }
      : {
          path: '/login',
          query: {
            redirect: to.fullPath,
          },
        }
  }

  return true
})

export default router