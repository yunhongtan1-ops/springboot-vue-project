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
        <p class="eyebrow">刷题、表达、复盘一体化</p>
        <h2>把练习、提交和 AI 反馈放在同一个工作台</h2>
        <p class="hero-text">
          从题目浏览、正式作答到复盘改进，整条训练链路都集中在一处，适合持续练习和回看历史记录。
        </p>
        <div class="hero-actions">
          <RouterLink to="/practice" class="button primary-button">开始刷题</RouterLink>
          <RouterLink to="/review" class="button ghost-button">查看复盘</RouterLink>
        </div>
      </div>

      <div class="hero-preview panel-surface panel-inset">
        <div class="preview-code">
          <span class="code-dot red"></span>
          <span class="code-dot yellow"></span>
          <span class="code-dot green"></span>
          <code>练习面板预览</code>
        </div>
        <div class="preview-block">
          <p>当前题目</p>
          <strong>Two Sum</strong>
          <small>哈希表思路，适合作为热身题快速进入状态。</small>
        </div>
        <div class="preview-block align-right">
          <p>点评得分</p>
          <strong>86 / 100</strong>
          <small>结构完整，表达顺序清晰，还可以补足复杂度取舍。</small>
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
        eyebrow="核心模块"
        title="当前平台能力"
        description="围绕刷题、提交、点评和进度统计，已经形成一条连续训练流程。"
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
        eyebrow="使用路径"
        title="推荐训练节奏"
        description="按这条路径使用，体验会更完整。"
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