export const homeMetrics = [
  { label: '题库覆盖', value: '128+', hint: '算法题 / 面试题 / 系统设计', tone: 'primary' },
  { label: '随时练习', value: '24h', hint: '打开页面即可进入训练流程', tone: 'violet' },
  { label: '复盘反馈', value: '18s', hint: '提交后可快速看到点评结果', tone: 'cyan' },
  { label: '本周目标', value: '7 / 10', hint: '保持节奏，持续积累', tone: 'default' },
]

export const featureCards = [
  {
    title: '刷题工作区',
    description: '把题目列表、题目详情、作答区和最近提交放在同一视图里，减少来回切换。',
    tag: '刷题流程',
  },
  {
    title: 'AI 复盘视图',
    description: '集中查看答案总结、亮点、改进建议和追问，适合做连续复盘。',
    tag: 'AI 点评',
  },
  {
    title: '学习进度面板',
    description: '通过指标卡、趋势图和最近记录，持续跟踪训练节奏。',
    tag: '学习进度',
  },
]

export const workflowSteps = [
  {
    step: '01',
    title: '选择题目',
    detail: '先浏览题库，挑选当前最适合练习的题目。',
  },
  {
    step: '02',
    title: '组织答案',
    detail: '围绕结论、思路、复杂度和边界条件整理作答结构。',
  },
  {
    step: '03',
    title: '复盘提升',
    detail: '结合 AI 点评和学习进度，找出亮点与待改进点。',
  },
]

export const practicePreview = {
  score: '86',
  label: '可继续提升',
  summary: '答案结构清晰，覆盖了核心解法，但复杂度分析和取舍说明还可以更完整。',
  highlights: ['思路表达顺序合理', '主动提到了边界条件', '术语使用较为准确'],
  followUps: ['如果数据规模继续增大，你会优先优化哪一部分？', '这道题还有没有更适合口头表达的说明方式？'],
}

export const reviewData = {
  answer: `我会先用哈希表记录已经遍历过的数字和对应的下标。\n\n在遍历数组时，先计算 target - nums[i]，如果哈希表里已经存在这个值，就返回两个下标；否则把当前值和下标存入哈希表。\n\n这种做法的时间复杂度是 O(n)，空间复杂度是 O(n)。如果需要进一步讨论，我会补充空数组、没有解以及重复元素等边界情况。`,
  score: 91,
  grade: 'A-',
  summary: '这是一份接近真实面试表达的答案，逻辑完整，复杂度说明清晰，已经具备较强的可讲述性。',
  strengths: ['先给思路再解释原因，听起来更容易跟上', '复杂度结论准确，表达比较稳定', '主动提到边界条件，说明具备工程意识'],
  improvements: ['可以补一句为什么不选择双重循环', '可以增加一个简短样例来辅助说明', '如果是现场编码，建议再明确变量命名和返回结构'],
  followUps: ['如果数组是有序的，你会如何调整方案？', '如果要求返回所有满足条件的解，你会怎么设计？'],
}

export const progressMetrics = [
  { label: '本周练习', value: '18', hint: '比上周多 5 次', tone: 'primary' },
  { label: '平均得分', value: '84', hint: '保持稳定上升', tone: 'violet' },
  { label: '连续打卡', value: '6 天', hint: '继续保持节奏', tone: 'cyan' },
  { label: '完成率', value: '72%', hint: '距离目标还差 3 题', tone: 'default' },
]

export const trendData = [
  { label: '周一', value: 4 },
  { label: '周二', value: 6 },
  { label: '周三', value: 5 },
  { label: '周四', value: 8 },
  { label: '周五', value: 7 },
  { label: '周六', value: 9 },
  { label: '周日', value: 6 },
]

export const recentRecords = [
  { title: 'Two Sum', type: '算法题', score: 88, time: '今天 10:24' },
  { title: 'Reverse Linked List', type: '算法题', score: 82, time: '昨天 21:10' },
  { title: 'Explain CAP Theorem', type: '面试题', score: 90, time: '03-05 19:42' },
  { title: '缓存雪崩与击穿', type: '系统设计', score: 79, time: '03-04 22:08' },
]

export const favorites = [
  'Two Sum：适合热身，练习口头表达',
  'CAP Theorem：适合面试概念题复盘',
  'LRU Cache：适合数据结构与设计结合训练',
]

export const mistakeBook = [
  '二分搜索的边界条件解释还不够清楚',
  '系统设计题回答时缺少流量预估',
  '复杂度分析有时只说时间复杂度，没有补充空间复杂度',
]

export const submissionHistory = [
  { id: 'SUB-1048', title: 'Two Sum', result: '通过', score: 88 },
  { id: 'SUB-1045', title: 'Reverse Linked List', result: '继续优化', score: 82 },
  { id: 'SUB-1039', title: 'CAP Theorem', result: '优秀', score: 90 },
]