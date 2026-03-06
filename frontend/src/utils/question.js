const questionTypeLabels = {
  ALGORITHM: '算法题',
  INTERVIEW: '面试题',
  SYSTEM_DESIGN: '系统设计',
}

const difficultyLabels = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

function extractMetadataLine(content, key) {
  if (!content) {
    return ''
  }

  const matcher = new RegExp(`^${key}:\\s*(.+)$`, 'mi')
  const match = content.match(matcher)
  return match?.[1]?.trim() || ''
}

export function formatQuestionType(value) {
  return questionTypeLabels[value] || value || '未分类'
}

export function formatDifficulty(value) {
  return difficultyLabels[value] || value || '未知'
}

export function extractQuestionTrack(content) {
  return extractMetadataLine(content, 'Track') || '通用题目'
}

export function extractQuestionPrompt(content, fallbackTitle = '') {
  return extractMetadataLine(content, 'Prompt') || fallbackTitle || content?.trim() || ''
}

export function extractQuestionSource(content) {
  return extractMetadataLine(content, 'Source') || '题库'
}

export function extractQuestionSummary(question) {
  const track = extractQuestionTrack(question?.content)

  if (question?.type === 'ALGORITHM') {
    return `${track} 方向，建议说明解题思路、核心数据结构、时间复杂度、空间复杂度和边界条件。`
  }

  if (question?.type === 'SYSTEM_DESIGN') {
    return `${track} 方向，建议从模块拆分、容量预估、瓶颈分析和关键取舍展开。`
  }

  return `${track} 方向，建议从核心概念、适用场景、实现细节、优缺点和常见追问展开回答。`
}

export function buildAnswerChecklist(question) {
  if (!question) {
    return []
  }

  if (question.type === 'ALGORITHM') {
    return [
      '先讲核心思路，再补充为什么这么做。',
      '说明会用到的数据结构和关键步骤。',
      '补充时间复杂度、空间复杂度和边界情况。',
      '如果时间允许，可以简要写出伪代码或关键代码。',
    ]
  }

  if (question.type === 'SYSTEM_DESIGN') {
    return [
      '先明确目标、约束和核心用户场景。',
      '再说明系统模块、数据流和存储设计。',
      '补充扩展性、可用性、监控和容灾策略。',
      '最后说明主要瓶颈和取舍。',
    ]
  }

  return [
    '先给结论，再解释概念和适用场景。',
    '补充实现方式、关键细节和常见误区。',
    '说明优缺点、边界条件和现实取舍。',
    '最后主动补一句常见追问或延展思考。',
  ]
}