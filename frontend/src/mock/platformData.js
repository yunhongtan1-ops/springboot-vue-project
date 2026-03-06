export const homeMetrics = [
  { label: '题库覆盖', value: '128+', hint: '算法题 / 场景题 / 系统设计', tone: 'primary' },
  { label: '模拟面试', value: '24h', hint: '随时进入刷题工作流', tone: 'violet' },
  { label: '平均反馈', value: '18s', hint: 'AI 点评区域先用假数据占位', tone: 'cyan' },
  { label: '本周目标', value: '7 / 10', hint: '学习节奏清晰可追踪', tone: 'default' },
]

export const featureCards = [
  {
    title: '题目工作台',
    description: '把题目列表、详情、作答区和 AI 点评放进同一视图，尽量减少切换成本。',
    tag: 'Practice Flow',
  },
  {
    title: '面试反馈视图',
    description: '单独查看答案、评分、优化建议和追问，适合复盘。',
    tag: 'AI Review',
  },
  {
    title: '学习进度仪表盘',
    description: '用数据卡片、趋势和最近练习记录，追踪训练节奏。',
    tag: 'Progress',
  },
]

export const workflowSteps = [
  {
    step: '01',
    title: '选择题目',
    detail: '左侧快速浏览题库，直接切入当前练习上下文。',
  },
  {
    step: '02',
    title: '组织答案',
    detail: '中间看题，右侧作答，保持低干扰和高可读性。',
  },
  {
    step: '03',
    title: '复盘提升',
    detail: '通过 AI 点评页和进度页观察薄弱点与提升空间。',
  },
]

export const practicePreview = {
  score: '86',
  label: '可通过',
  summary: '答案结构清晰，能覆盖核心解法，但还可以把复杂度分析说得更完整。',
  highlights: ['思路表达顺序合理', '边界条件有意识提到', '术语使用较专业'],
  followUps: ['如果数据规模增大，你会如何优化？', '这道题是否有更适合的空间复杂度写法？'],
}

export const reviewData = {
  answer: `我会先用哈希表记录已经遍历过的数字和值对应的下标。\n\n在遍历数组时，先计算 target - nums[i]，如果哈希表里已经存在这个值，就返回两个下标。否则把当前值和下标存入哈希表。\n\n这种做法时间复杂度是 O(n)，空间复杂度是 O(n)。如果需要进一步讨论，我会补充边界条件，比如数组为空、没有解或有重复元素的情况。`,
  score: 91,
  grade: 'A-',
  summary: '这是一份接近真实面试表达的答案，逻辑完整，复杂度正确，已经具备较强的可讲述性。',
  strengths: ['先给思路再解释原因，面试官更容易跟上', '复杂度结论正确，表达比较稳定', '能主动提到边界条件，说明有工程意识'],
  improvements: ['可以补一句“为什么双重循环不是最优”', '可增加一个简短样例来辅助说明', '如果是现场编码，建议明确变量命名和返回结构'],
  followUps: ['如果数组是有序的，你会如何调整方案？', '如果要求返回所有符合条件的解，你的设计会怎么变？'],
}

export const progressMetrics = [
  { label: '本周练习', value: '18', hint: '较上周 +5', tone: 'primary' },
  { label: '平均得分', value: '84', hint: '稳定上升', tone: 'violet' },
  { label: '连续打卡', value: '6 天', hint: '保持节奏', tone: 'cyan' },
  { label: '完成率', value: '72%', hint: '距离目标还差 3 题', tone: 'default' },
]

export const trendData = [
  { label: 'Mon', value: 4 },
  { label: 'Tue', value: 6 },
  { label: 'Wed', value: 5 },
  { label: 'Thu', value: 8 },
  { label: 'Fri', value: 7 },
  { label: 'Sat', value: 9 },
  { label: 'Sun', value: 6 },
]

export const recentRecords = [
  { title: 'Two Sum', type: '算法题', score: 88, time: '今天 10:24' },
  { title: 'Reverse Linked List', type: '算法题', score: 82, time: '昨天 21:10' },
  { title: 'Explain CAP Theorem', type: '面试题', score: 90, time: '03-05 19:42' },
  { title: '缓存雪崩与击穿', type: '系统设计', score: 79, time: '03-04 22:08' },
]

export const favorites = [
  'Two Sum：适合做口头表达热身',
  'CAP Theorem：适合面试概念题复盘',
  'LRU Cache：适合数据结构 + 设计结合训练',
]

export const mistakeBook = [
  '二分搜索边界条件经常解释不够清楚',
  '系统设计题回答时缺少流量预估',
  '复杂度分析有时只说时间复杂度，没有说空间复杂度',
]

export const submissionHistory = [
  { id: 'SUB-1048', title: 'Two Sum', result: '通过', score: 88 },
  { id: 'SUB-1045', title: 'Reverse Linked List', result: '继续优化', score: 82 },
  { id: 'SUB-1039', title: 'CAP Theorem', result: '优秀', score: 90 },
]