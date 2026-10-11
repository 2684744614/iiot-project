<template>
  <div>
    <!-- 上传区域 -->
    <el-card style="margin-bottom: 20px;">
      <template #header><b>📚 上传设备手册 / 运维文档</b></template>
      <el-upload
          drag
          :show-file-list="false"
          :before-upload="handleUpload"
          accept=".txt,.pdf,.doc,.docx"
      >
        <el-icon style="font-size: 50px; color: #c0c4cc;"><upload-filled /></el-icon>
        <div style="margin-top: 10px; color: #606266;">
          将文件拖到此处，或<em style="color: #409eff;">点击上传</em>
        </div>
        <template #tip>
          <div style="color: #909399; font-size: 12px; margin-top: 8px;">
            支持 PDF、Word、TXT 格式，上传后系统会自动解析、切片、向量化
          </div>
        </template>
      </el-upload>
    </el-card>

    <!-- 文档列表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <b>已上传文档</b>
          <el-button type="primary" size="small" @click="loadDocs">刷新</el-button>
        </div>
      </template>

      <el-table :data="documents" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="250" />
        <el-table-column prop="fileType" label="类型" width="150" />
        <el-table-column prop="chunkCount" label="切片数" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="200" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已向量化' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const documents = ref([])

const loadDocs = async () => {
  try {
    const res = await axios.get('/api/rag/documents')
    documents.value = res.data
  } catch (e) {
    ElMessage.error('加载文档列表失败')
  }
}

const handleUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file)

  const loading = ElMessage({
    message: '正在解析、向量化，请稍候...',
    type: 'info',
    duration: 0
  })

  try {
    await axios.post('/api/rag/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    loading.close()
    ElMessage.success('上传成功！')
    loadDocs()
  } catch (e) {
    loading.close()
    ElMessage.error('上传失败：' + (e.response?.data?.message || e.message))
  }
  return false  // 阻止 el-upload 默认行为
}

onMounted(loadDocs)
</script>