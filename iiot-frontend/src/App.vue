<template>
  <el-container style="height: 100vh;">
    <!-- 左侧菜单 -->
    <el-aside width="200px" style="background-color: #2b3a4a;">
      <div style="color: #fff; padding: 20px; font-size: 16px; font-weight: bold;">
        🏭 工业设备运维系统
      </div>
      <el-menu
          :default-active="currentView"
          background-color="#2b3a4a"
          text-color="#bfcbd9"
          active-text-color="#ffd04b"
          @select="handleSelect"
      >
        <el-menu-item index="device">📊 设备监控</el-menu-item>
        <el-menu-item index="alarm">🚨 报警管理</el-menu-item>
        <el-menu-item index="chat">🤖 AI 助手</el-menu-item>
        <el-menu-item index="knowledge">📚 知识库</el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-main style="background-color: #f0f2f5;">
      <DeviceMonitor v-if="currentView === 'device'" />
      <AlarmList v-else-if="currentView === 'alarm'" />
      <AiChat v-else-if="currentView === 'chat'" />
      <KnowledgeBase v-else-if="currentView === 'knowledge'" />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import DeviceMonitor from './views/DeviceMonitor.vue'
import AlarmList from './views/AlarmList.vue'
import AiChat from './views/AiChat.vue'
import KnowledgeBase from './views/KnowledgeBase.vue'

const currentView = ref('device')

const handleSelect = (index) => {
  currentView.value = index
}
</script>

<style>
body {
  margin: 0;
  font-family: 'Microsoft YaHei', sans-serif;
}
</style>