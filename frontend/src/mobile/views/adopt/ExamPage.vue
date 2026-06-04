<template>
  <div class="page">
    <!-- 开始考试 -->
    <div v-if="!started && !finished" class="card-wrap">
      <MobileCard>
        <div class="exam-intro">
          <i class="fas fa-clipboard-check icon"></i>
          <h2>领养知识考试</h2>
          <p>共 10 题，满分通过后方可申请领养</p>
          <button class="save-btn" @click="handleStart">开始考试</button>
        </div>
      </MobileCard>
    </div>

    <!-- 答题中 -->
    <div v-if="started && !finished" class="card-wrap">
      <MobileCard>
        <div class="exam-progress">第 {{ current + 1 }} / {{ questions.length }} 题</div>
        <div class="progress-bar"><div class="fill" :style="{ width: ((current + 1) / questions.length * 100) + '%' }"></div></div>
        <div class="question-card">
          <h3>{{ questions[current]?.content }}</h3>
          <div class="options">
            <button
              v-for="(opt, oi) in questions[current]?.options"
              :key="oi"
              :class="['opt', { selected: answers[current] === oi }]"
              @click="selectAnswer(oi)"
            >{{ opt }}</button>
          </div>
        </div>
        <div class="exam-actions">
          <button v-if="current > 0" class="btn-secondary" @click="current--">上一题</button>
          <button v-if="current < questions.length - 1" class="btn-primary" :disabled="answers[current] === undefined" @click="current++">下一题</button>
          <button v-if="current === questions.length - 1" class="btn-primary" :disabled="answers.some(a => a === undefined)" @click="handleSubmit">提交</button>
        </div>
      </MobileCard>
    </div>

    <!-- 考试结果 -->
    <div v-if="finished" class="card-wrap">
      <MobileCard>
        <div class="result-card">
          <i :class="passed ? 'fas fa-check-circle pass-icon' : 'fas fa-times-circle fail-icon'"></i>
          <h2>{{ passed ? '恭喜通过！' : '未通过' }}</h2>
          <p>得分：{{ score }} 分</p>
          <button v-if="passed" class="save-btn" @click="goBack">返回</button>
          <button v-else class="save-btn" @click="retryExam">重新考试</button>
        </div>
      </MobileCard>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { startExam, submitExam } from '@/api/adopt'
import MobileCard from '../../components/MobileCard.vue'

const router = useRouter()
const started = ref(false); const finished = ref(false); const passed = ref(false)
const questions = ref([]); const answers = ref([]); const current = ref(0); const score = ref(0)

async function handleStart() {
  try {
    const res = await startExam()
    questions.value = res.questions || res
    answers.value = new Array(questions.value.length).fill(undefined)
    started.value = true
  } catch {}
}

function selectAnswer(oi) {
  answers.value[current.value] = oi
  if (current.value < questions.value.length - 1) {
    setTimeout(() => current.value++, 300)
  }
}

async function handleSubmit() {
  try {
    const data = { answers: answers.value.map((a, i) => ({ questionIndex: i, answerIndex: a })) }
    const res = await submitExam(data)
    finished.value = true
    score.value = res.score || res.totalScore || 0
    passed.value = res.passed || res.isPassed || (score.value >= 100)
    ElMessage(passed.value ? '恭喜通过考试！' : '未通过，请重试')
  } catch {}
}

function retryExam() { started.value = false; finished.value = false; answers.value = []; current.value = 0 }
function goBack() { router.back() }
</script>

<style scoped>
.page { padding: 12px 16px 40px; }
.card-wrap { margin-bottom: 12px; }
.exam-intro { text-align: center; padding: 16px 0; }
.icon { font-size: 48px; color: #8ab8a0; margin-bottom: 12px; }
.exam-intro h2 { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 0 0 8px; }
.exam-intro p { font-size: 14px; color: #a09080; margin: 0 0 20px; }
.save-btn { width: 100%; height: 48px; border: none; border-radius: 12px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 16px; font-weight: 600; cursor: pointer; }
.exam-progress { font-size: 13px; color: #a09080; text-align: center; margin-bottom: 8px; }
.progress-bar { width: 100%; height: 4px; background: #ece4d8; border-radius: 2px; margin-bottom: 20px; overflow: hidden; }
.fill { height: 100%; background: #8ab8a0; border-radius: 2px; transition: width 0.3s; }
.question-card h3 { font-size: 16px; font-weight: 600; color: #5a4a42; line-height: 1.5; margin: 0 0 16px; }
.options { display: flex; flex-direction: column; gap: 10px; }
.opt { width: 100%; padding: 14px 16px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; text-align: left; cursor: pointer; transition: all 0.15s; }
.opt.selected { border-color: #8ab8a0; background: rgba(139,184,160,0.08); color: #8ab8a0; }
.exam-actions { display: flex; gap: 10px; margin-top: 20px; }
.btn-primary { flex: 1; height: 44px; border: none; border-radius: 10px; background: #8ab8a0; color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; }
.btn-primary:disabled { opacity: 0.5; }
.btn-secondary { flex: 1; height: 44px; border: 1px solid #d1e7dd; border-radius: 10px; background: #fefaf5; color: #5a4a42; font-size: 14px; cursor: pointer; }
.result-card { text-align: center; padding: 16px 0; }
.pass-icon { font-size: 48px; color: #67C23A; }
.fail-icon { font-size: 48px; color: #F56C6C; }
.result-card h2 { font-size: 20px; font-weight: 700; color: #5a4a42; margin: 8px 0; }
.result-card p { font-size: 16px; color: #a09080; margin: 0 0 20px; }
</style>
