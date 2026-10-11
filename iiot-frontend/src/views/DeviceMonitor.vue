<template>
  <div>
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="font-size: 14px; color: #909399;">设备总数</div>
          <div style="font-size: 28px; font-weight: bold;">{{ devices.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="font-size: 14px; color: #909399;">在线设备</div>
          <div style="font-size: 28px; font-weight: bold; color: #67c23a;">
            {{ devices.filter(d => d.status === 1).length }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="font-size: 14px; color: #909399;">实时温度</div>
          <div style="font-size: 28px; font-weight: bold; color: #f56c6c;">
            {{ latestTemp }}℃
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="font-size: 14px; color: #909399;">连接状态</div>
          <div style="font-size: 28px; font-weight: bold;" :style="{ color: connected ? '#67c23a' : '#f56c6c' }">
            {{ connected ? '已连接' : '未连接' }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 设备列表 -->
    <el-card style="margin-bottom: 20px;">
      <template #header><b>设备列表</b></template>
      <el-table :data="devices" stripe>
        <el-table-column prop="deviceCode" label="设备编号" />
        <el-table-column prop="deviceName" label="设备名称" />
        <el-table-column prop="deviceType" label="类型" />
        <el-table-column prop="location" label="位置" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 实时曲线 -->
    <el-card>
      <template #header><b>1号电机 · 实时温度曲线（WebSocket）</b></template>
      <div ref="chartRef" style="width: 100%; height: 350px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const devices = ref([])
const latestTemp = ref('--')
const connected = ref(false)
const chartRef = ref()
let chart = null
let stompClient = null

// 初始化 ECharts 曲线
const initChart = (initialData) => {
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: initialData.map(d => new Date(d.reportTime).toLocaleTimeString())
    },
    yAxis: { type: 'value', name: '℃', min: 0, max: 120 },
    series: [{
      name: '温度',
      type: 'line',
      smooth: true,
      data: initialData.map(d => d.temperature),
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#f56c6c' }
    }]
  })
}

// 更新图表（WebSocket 收到数据时调用）
const pushToChart = (data) => {
  if (!chart) return
  const opt = chart.getOption()
  opt.xAxis[0].data.push(new Date().toLocaleTimeString())
  opt.series[0].data.push(data.temperature)
  // 只保留最近 30 个点
  if (opt.xAxis[0].data.length > 30) {
    opt.xAxis[0].data.shift()
    opt.series[0].data.shift()
  }
  chart.setOption(opt)
}

// 连接 WebSocket
const connectWs = () => {
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)
  stompClient.debug = null
  stompClient.connect({}, () => {
    connected.value = true
    stompClient.subscribe('/topic/device/1', (msg) => {
      const data = JSON.parse(msg.body)
      latestTemp.value = data.temperature
      pushToChart(data)
    })
  }, () => {
    connected.value = false
    setTimeout(connectWs, 5000)
  })
}

onMounted(async () => {
  // 1. 加载设备列表
  const res = await axios.get('/api/device/list')
  devices.value = res.data

  // 2. 加载 1 号电机最近 30 条数据，初始化曲线
  try {
    const hist = await axios.get('/api/telemetry/recent?deviceId=1&limit=30')
    initChart(hist.data)
  } catch (e) {
    console.warn('无历史数据，使用空白图表')
    initChart([])
  }

  // 3. 连接 WebSocket 实时推送
  connectWs()
})

onUnmounted(() => {
  if (stompClient) stompClient.disconnect()
  if (chart) chart.dispose()
})
</script>