<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <b>🚨 报警记录（最近 50 条）</b>
          <el-button type="primary" size="small" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-table :data="alarms" stripe border @row-click="showDetail">
        <el-table-column prop="deviceName" label="设备" width="120" />
        <el-table-column prop="metric" label="指标" width="100">
          <template #default="{ row }">
            {{ metricLabel(row.metric) }}
          </template>
        </el-table-column>
        <el-table-column prop="metricValue" label="实测值" width="100" />
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelType(row.level)">{{ levelLabel(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="报警时间" width="200" />
        <el-table-column label="AI 建议" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.aiSuggestion" type="success" effect="plain">已生成</el-tag>
            <el-tag v-else type="info" effect="plain">处理中...</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="showDetail(row)">查看建议</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI 建议弹窗 -->
    <el-dialog v-model="dialogVisible" title="🤖 AI 智能处置建议" width="700px">
      <div v-if="currentAlarm">
        <el-descriptions :column="2" border style="margin-bottom: 16px;">
          <el-descriptions-item label="设备">{{ currentAlarm.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="报警指标">{{ metricLabel(currentAlarm.metric) }}</el-descriptions-item>
          <el-descriptions-item label="实测值">{{ currentAlarm.metricValue }}</el-descriptions-item>
          <el-descriptions-item label="阈值">{{ currentAlarm.threshold }}</el-descriptions-item>
          <el-descriptions-item label="报警时间" :span="2">{{ currentAlarm.alarmTime }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
            v-if="!currentAlarm.aiSuggestion"
            title="AI 正在生成建议，请稍后刷新"
            type="warning"
            :closable="false"
        />
        <div v-else style="background: #f5f7fa; padding: 16px; border-radius: 4px; line-height: 1.8; white-space: pre-wrap;">
          {{ currentAlarm.aiSuggestion }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const alarms = ref([])
const dialogVisible = ref(false)
const currentAlarm = ref(null)

const loadData = async () => {
  const res = await axios.get('/api/alarm/list')
  alarms.value = res.data
}

const showDetail = (row) => {
  currentAlarm.value = row
  dialogVisible.value = true
}

const metricLabel = (m) => {
  if (m === 'temperature') return '温度'
  if (m === 'vibration') return '振动'
  if (m === 'current') return '电流'
  return m
}

const levelLabel = (l) => {
  if (l === 1) return '警告'
  if (l === 2) return '严重'
  if (l === 3) return '紧急'
  return '未知'
}

const levelType = (l) => {
  if (l === 1) return 'warning'
  if (l === 2) return 'danger'
  if (l === 3) return 'danger'
  return 'info'
}

onMounted(loadData)
</script>