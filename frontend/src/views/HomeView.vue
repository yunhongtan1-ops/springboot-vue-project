<script setup>
import { RouterLink } from 'vue-router'
import MetricCard from '@/components/MetricCard.vue'
import SectionCard from '@/components/SectionCard.vue'
import { featureCards, homeMetrics, workflowSteps } from '@/mock/platformData'
</script>

<template>
  <div class="page-stack">
    <section class="hero-surface panel-surface">
      <div class="hero-copy">
        <p class="eyebrow">LeetCode + CodeSignal + AI Interview</p>
        <h2>更适合刷题、表达和复盘的现代化面试工作台</h2>
        <p class="hero-text">
          题目阅读、答题组织、AI 点评和学习进度被整理进统一界面，降低切换成本，适合长时间训练。
        </p>
        <div class="hero-actions">
          <RouterLink to="/practice" class="button primary-button">开始刷题</RouterLink>
          <RouterLink to="/review" class="button ghost-button">查看点评样例</RouterLink>
        </div>
      </div>

      <div class="hero-preview panel-surface panel-inset">
        <div class="preview-code">
          <span class="code-dot red"></span>
          <span class="code-dot yellow"></span>
          <span class="code-dot green"></span>
          <code>Interview Workspace Preview</code>
        </div>
        <div class="preview-block">
          <p>Question</p>
          <strong>Two Sum</strong>
          <small>HashMap · O(n) · 高频热身题</small>
        </div>
        <div class="preview-block align-right">
          <p>AI Score</p>
          <strong>86 / 100</strong>
          <small>表达结构清晰，复杂度还能更完整</small>
        </div>
      </div>
    </section>

    <section class="metric-grid four-cols">
      <MetricCard
        v-for="item in homeMetrics"
        :key="item.label"
        :label="item.label"
        :value="item.value"
        :hint="item.hint"
        :tone="item.tone"
      />
    </section>

    <section class="content-grid two-cols">
      <SectionCard
        eyebrow="Core Modules"
        title="平台核心能力"
        description="页面结构先搭完整，后续可以继续把 AI 和练习接口逐步接进来。"
      >
        <div class="feature-grid">
          <article v-for="feature in featureCards" :key="feature.title" class="feature-card">
            <span class="tiny-pill">{{ feature.tag }}</span>
            <h4>{{ feature.title }}</h4>
            <p>{{ feature.description }}</p>
          </article>
        </div>
      </SectionCard>

      <SectionCard
        eyebrow="Workflow"
        title="推荐使用路径"
        description="先刷题，再看点评，最后回到进度页复盘。"
      >
        <div class="workflow-list">
          <article v-for="item in workflowSteps" :key="item.step" class="workflow-item">
            <span class="workflow-step">{{ item.step }}</span>
            <div>
              <h4>{{ item.title }}</h4>
              <p>{{ item.detail }}</p>
            </div>
          </article>
        </div>
      </SectionCard>
    </section>
  </div>
</template>