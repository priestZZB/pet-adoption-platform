<template>
  <div class="exam-page">
    <h3 class="page-title">领养考试</h3>

    <el-card>
      <!-- 阶段一：考试说明 -->
      <div v-if="phase === 'intro'" class="phase-content">
        <el-icon :size="48" color="#409EFF"><Reading /></el-icon>
        <h3>领养资格测试</h3>
        <div class="intro-rules">
          <p>共 <b>10</b> 题，每题 <b>10</b> 分，满分 <b>100</b> 分</p>
          <p>通过线：<b>100 分</b>（满分才能申请领养）</p>
          <p>请认真作答，展示你对宠物负责的态度</p>
        </div>
        <el-button class="exam-btn" size="large" @click="startExam">
          开始答题
        </el-button>
      </div>

      <!-- 阶段二：答题中 -->
      <div v-if="phase === 'answering'" class="phase-content">
        <!-- 进度条 -->
        <el-progress
          :percentage="Math.round((currentIdx / questions.length) * 100)"
          :text-inside="true"
          :stroke-width="20"
          style="margin-bottom:24px"
        />

        <div v-if="currentQuestion" class="question-area">
          <div class="q-header">
            <span class="q-number">第 {{ currentIdx + 1 }} / {{ questions.length }} 题</span>
          </div>
          <p class="q-text">{{ currentQuestion.question }}</p>

          <el-radio-group
            v-model="answers[currentQuestion.id]"
            class="q-options"
          >
            <el-radio
              v-for="opt in options"
              :key="opt.value"
              :value="opt.value"
              class="q-option"
              border
            >
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </div>

        <div class="q-nav">
          <el-button
            :disabled="currentIdx === 0"
            @click="currentIdx--"
          >
            上一题
          </el-button>
          <el-button
            v-if="currentIdx < questions.length - 1"
            class="exam-nav-btn"
            :disabled="!answers[currentQuestion?.id]"
            @click="currentIdx++"
          >
            下一题
          </el-button>
          <el-button
            v-else
            class="exam-submit-btn"
            :disabled="!answers[currentQuestion?.id]"
            @click="submitExamFn"
          >
            提交答卷
          </el-button>
        </div>
      </div>

      <!-- 阶段三：考试结果 -->
      <div v-if="phase === 'result'" class="phase-content">
        <el-icon
          :size="48"
          :color="result.isPassed ? '#67C23A' : '#F56C6C'"
        >
          {{ result.isPassed ? 'CircleCheck' : 'CircleCloseFilled' }}
        </el-icon>
        <h3>{{ result.isPassed ? '恭喜，考试通过！' : '很遗憾，未通过' }}</h3>
        <div class="result-score">
          <span class="score-num">{{ result.score }}</span>
          <span class="score-divider">/</span>
          <span class="score-total">{{ result.totalQuestions }}</span>
        </div>
        <p class="result-label">
          {{ result.isPassed ? '满分通过，现在可以提交领养申请了' : '未达到满分（100分），请重新考试' }}
        </p>
        <div class="result-actions">
          <el-button v-if="!result.isPassed" class="exam-btn" @click="startExam">
            重新考试
          </el-button>
          <el-button v-if="result.isPassed" class="exam-btn" size="large" @click="goAdopt">
            去领养
          </el-button>
        </div>

        <el-divider />

        <!-- 历史记录 -->
        <h4>考试记录</h4>
        <div v-if="historyList.length === 0" class="empty-history">
          暂无考试记录
        </div>
        <div v-else class="history-list">
          <div v-for="(h, idx) in historyList" :key="idx" class="history-item">
            <span class="history-time">{{ h.createdAt }}</span>
            <span>
              得分：<b :class="h.isPassed ? 'pass' : 'fail'">{{ h.score }}</b>
              / {{ h.totalQuestions }}
            </span>
            <el-tag
              :type="h.isPassed ? 'success' : 'danger'"
              size="small"
            >
              {{ h.isPassed ? '通过' : '未通过' }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { startExam as apiStartExam, submitExam, getExamHistory } from '@/api/adopt'

const router = useRouter()
const phase = ref('intro')
const questions = ref([])
const answers = ref({})
const currentIdx = ref(0)
const result = ref(null)
const historyList = ref([])

const currentQuestion = computed(() => questions.value[currentIdx.value])

const options = computed(() => {
  if (!currentQuestion.value) return []
  return [
    { label: 'A. ' + currentQuestion.value.optionA, value: 'A' },
    { label: 'B. ' + currentQuestion.value.optionB, value: 'B' },
    { label: 'C. ' + currentQuestion.value.optionC, value: 'C' },
    { label: 'D. ' + currentQuestion.value.optionD, value: 'D' }
  ].filter(o => o.label.length > 3) // 过滤掉空选项D
})

async function startExam() {
  try {
    questions.value = await apiStartExam()
    answers.value = {}
    currentIdx.value = 0
    phase.value = 'answering'
  } catch {
    // 请求拦截器统一处理
  }
}

async function submitExamAction() {
  const answerList = Object.entries(answers.value).map(([questionId, answer]) => ({
    questionId: parseInt(questionId),
    answer
  }))

  try {
    result.value = await submitExam({ answers: answerList })
    // 加载历史记录
    historyList.value = await getExamHistory()
    phase.value = 'result'
  } catch {
    // 请求拦截器统一处理
  }
}

function goAdopt() {
  router.push('/')
}

// 防止和全局 submitExam 重名
const submitExamFn = submitExamAction
</script>

<style scoped>
.exam-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 24px 0 40px;
}
.page-title {
  font-size: 20px;
  color: var(--yc-text-primary);
  margin: 0 0 20px;
}

.phase-content {
  text-align: center;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.phase-content h3 {
  margin: 0;
  font-size: 18px;
  color: var(--yc-text-primary);
}

/* 考试说明 */
.intro-rules {
  font-size: 15px;
  color: var(--yc-text-secondary);
  line-height: 2;
}
.intro-rules b {
  color: var(--yc-accent);
}

/* 答题区 */
.question-area {
  width: 100%;
  text-align: left;
}
.q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.q-number {
  font-size: 14px;
  color: var(--yc-text-tertiary);
}
.q-text {
  font-size: 16px;
  color: var(--yc-text-primary);
  line-height: 1.6;
  margin: 0 0 20px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}
.q-option {
  width: 100% !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
  display: flex;
  align-items: center;
  padding: 12px 16px;
  font-size: 15px;
  box-sizing: border-box;
  border-radius: var(--yc-radius-input);
  border-color: var(--yc-border);
  transition: border-color 0.2s;
}
.q-option:hover {
  border-color: var(--yc-border-hover);
}
.q-option:deep(.el-radio__label) {
  flex: 1;
}
:deep(.q-option.is-checked) {
  border-color: var(--yc-accent);
  background: var(--yc-accent-light);
}

/* 导航 */
.q-nav {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  width: 100%;
}

/* 按钮暖色 */
:deep(.exam-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
  padding: 12px 28px;
  font-size: 15px;
}
:deep(.exam-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.exam-nav-btn) {
  background: var(--yc-btn-primary);
  border: 1px solid var(--yc-border);
  color: var(--yc-btn-text);
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.exam-nav-btn:hover) {
  background: var(--yc-btn-hover);
  border-color: var(--yc-border-hover);
  color: var(--yc-btn-text);
}
:deep(.exam-submit-btn) {
  background: var(--yc-accent);
  border: 1px solid var(--yc-accent);
  color: #fff;
  border-radius: var(--yc-radius-btn);
  font-weight: 500;
}
:deep(.exam-submit-btn:hover) {
  background: #7aaa92;
  border-color: #7aaa92;
  color: #fff;
}

/* 进度条暖色 */
:deep(.el-progress-bar__inner) {
  background: var(--yc-accent) !important;
}
:deep(.el-progress__text) {
  color: var(--yc-text-secondary);
}

/* 结果 */
.result-score {
  font-size: 48px;
  font-weight: bold;
  color: var(--yc-text-primary);
}
.score-num {
  color: var(--yc-accent);
}
.score-divider {
  margin: 0 4px;
  color: var(--yc-text-tertiary);
}
.score-total {
  color: var(--yc-text-tertiary);
}
.result-label {
  font-size: 14px;
  color: var(--yc-text-tertiary);
  margin: 0;
}
.result-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.history-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-tag);
  font-size: 14px;
  color: var(--yc-text-primary);
}
.history-time {
  font-size: 12px;
  color: var(--yc-text-tertiary);
}
.pass { color: #67C23A; }
.fail { color: #F56C6C; }
.empty-history {
  font-size: 14px;
  color: var(--yc-text-tertiary);
  padding: 20px 0;
}

/* Card 暖色 */
:deep(.el-card) {
  border: 1px solid var(--yc-border);
  border-radius: var(--yc-radius-card);
  background: var(--yc-bg-card);
}
</style>
