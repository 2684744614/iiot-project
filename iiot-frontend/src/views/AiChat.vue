<template>
  <div style="height: calc(100vh - 40px); display: flex; flex-direction: column;">
    <el-card style="flex: 1; display: flex; flex-direction: column;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <b>🤖 AI 运维助手</b>
          <el-button size="small" @click="clearHistory">清空对话</el-button>
        </div>
      </template>

      <!-- 对话历史 -->
      <div ref="chatBoxRef" style="flex: 1; overflow-y: auto; padding: 10px; background: #fafafa; border-radius: 4px;">
        <div v-if="messages.length === 0" style="text-align: center; color: #909399; margin-top: 60px;">
          <p style="font-size: 16px;">💡 试着这样问我：</p>
          <p>"1号电机温度过高怎么办？"</p>
          <p>"2号水泵电流异常怎么排查？"</p>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" style="margin-bottom: 16px;">
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" style="text-align: right;">
            <div style="display: inline-block; background: #409eff; color: white; padding: 8px 14px; border-radius: 8px; max-width: 70%; text-align: left;">
              {{ msg.content }}
            </div>
          </div>
          <!-- AI 消息 -->
          <div v-else style="text-align: left;">
            <div style="display: inline-block; background: white; border: 1px solid #e4e7ed; padding: 12px 16px; border-radius: 8px; max-width: 80%; white-space: pre-wrap; line-height: 1.7;">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <div v-if="loading" style="color: #909399;">
          <i class="el-icon-loading"></i> AI 正在思考...
        </div>
      </div>

      <!-- 输入框 -->
      <div style="margin-top: 16px; display: flex; gap: 10px;">
        <el-input
            v-model="input"
            placeholder="请输入你的问题..."
            @keyup.enter="send"
            :disabled="loading"
            size="large"
        />
        <el-button type="primary" size="large" @click="send" :loading="loading">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const messages = ref([])
const input = ref('')
const loading = ref(false)
const chatBoxRef = ref()

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBoxRef.value) {
      chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
  })
}

const send = async () => {
  const q = input.value.trim()
  if (!q) return

  messages.value.push({ role: 'user', content: q })
  input.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const res = await axios.get('/api/rag/chat', { params: { question: q } })
    messages.value.push({ role: 'ai', content: res.data })
  } catch (e) {
    ElMessage.error('AI 请求失败：' + (e.response?.data?.message || e.message))
    messages.value.push({ role: 'ai', content: '❌ 抱歉，AI 服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const clearHistory = () => {
  messages.value = []
}
</script>