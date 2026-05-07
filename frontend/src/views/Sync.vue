<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="title">数据同步</span>
      </div>

      <el-form :model="syncForm" label-width="100px" class="sync-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择厂商" required>
              <el-select
                v-model="syncForm.vendorId"
                placeholder="请选择数据源厂商"
                style="width: 100%"
                @change="handleVendorChange"
              >
                <el-option
                  v-for="item in vendors"
                  :key="item.id"
                  :label="item.vendorName"
                  :value="item.id"
                >
                  <div class="vendor-option">
                    <span>{{ item.vendorName }}</span>
                    <el-tag size="small" type="info">{{ item.vendorCode }}</el-tag>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作类型">
              <el-radio-group v-model="syncForm.operationType">
                <el-radio-button value="pull">拉取数据</el-radio-button>
                <el-radio-button value="push">推送数据</el-radio-button>
                <el-radio-button value="sync">双向同步</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请求方法">
              <el-select v-model="syncForm.method" style="width: 100%">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="API端点">
              <el-input
                v-model="syncForm.endpoint"
                placeholder="如: /users, /data/list"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="请求头">
          <el-input
            v-model="syncForm.headersJson"
            type="textarea"
            :rows="3"
            placeholder='{"Content-Type": "application/json"}'
          />
        </el-form-item>

        <el-form-item v-if="syncForm.method !== 'GET'" label="请求体">
          <el-input
            v-model="syncForm.bodyJson"
            type="textarea"
            :rows="6"
            placeholder="请输入JSON格式的请求体"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="syncing" @click="handleSync">
            <el-icon><Refresh /></el-icon> 执行同步
          </el-button>
          <el-button @click="resetForm">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="result" class="card">
      <div class="card-header">
        <span class="title">同步结果</span>
        <el-tag
          :type="result.syncStatus === 1 ? 'success' : 'danger'"
          size="large"
        >
          {{ result.syncStatus === 1 ? '成功' : '失败' }}
        </el-tag>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="厂商名称">{{ result.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationLabel(result.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ result.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="响应状态码">
          <el-tag :type="result.responseCode >= 200 && result.responseCode < 300 ? 'success' : 'danger'">
            {{ result.responseCode }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ result.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="数据量">{{ result.dataCount || 0 }} 条</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ result.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="result.errorMessage" label="错误信息" :span="2">
          <el-text type="danger">{{ result.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="result.responseBody" class="response-body">
        <div class="response-header">
          <span>响应数据</span>
          <el-button type="primary" link @click="copyResponse">
            <el-icon><CopyDocument /></el-icon> 复制
          </el-button>
        </div>
        <pre class="json-viewer">{{ formatJson(result.responseBody) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, RefreshLeft, CopyDocument } from '@element-plus/icons-vue'
import { vendorApi, syncApi } from '@/api'

const vendors = ref([])
const syncing = ref(false)
const result = ref(null)
const selectedVendor = ref(null)

const syncForm = reactive({
  vendorId: null,
  operationType: 'pull',
  method: 'GET',
  endpoint: '',
  headersJson: '',
  bodyJson: ''
})

const getOperationLabel = (type) => {
  const map = { pull: '拉取', push: '推送', sync: '同步', query: '查询', test: '测试' }
  return map[type] || type
}

const formatJson = (str) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const loadVendors = async () => {
  try {
    const res = await vendorApi.list()
    vendors.value = res.data
  } catch (error) {
    // Handled by interceptor
  }
}

const handleVendorChange = (id) => {
  selectedVendor.value = vendors.value.find(v => v.id === id)
}

const handleSync = async () => {
  if (!syncForm.vendorId) {
    ElMessage.warning('请选择数据源厂商')
    return
  }

  syncing.value = true
  result.value = null

  try {
    let headers = null
    let body = null

    if (syncForm.headersJson) {
      try {
        headers = JSON.parse(syncForm.headersJson)
      } catch {
        ElMessage.error('请求头格式错误，请输入有效的JSON')
        syncing.value = false
        return
      }
    }

    if (syncForm.bodyJson && syncForm.method !== 'GET') {
      try {
        body = JSON.parse(syncForm.bodyJson)
      } catch {
        ElMessage.error('请求体格式错误，请输入有效的JSON')
        syncing.value = false
        return
      }
    }

    const res = await syncApi.sync({
      vendorId: syncForm.vendorId,
      operationType: syncForm.operationType,
      endpoint: syncForm.endpoint,
      method: syncForm.method,
      headers,
      body
    })

    result.value = res.data

    if (res.data.syncStatus === 1) {
      ElMessage.success('同步成功')
    } else {
      ElMessage.error('同步失败: ' + (res.data.errorMessage || '未知错误'))
    }
  } catch (error) {
    // Handled by interceptor
  } finally {
    syncing.value = false
  }
}

const resetForm = () => {
  Object.assign(syncForm, {
    vendorId: null,
    operationType: 'pull',
    method: 'GET',
    endpoint: '',
    headersJson: '',
    bodyJson: ''
  })
  selectedVendor.value = null
  result.value = null
}

const copyResponse = () => {
  if (result.value?.responseBody) {
    navigator.clipboard.writeText(formatJson(result.value.responseBody))
    ElMessage.success('已复制到剪贴板')
  }
}

onMounted(() => {
  loadVendors()
})
</script>

<style lang="scss" scoped>
.sync-form {
  max-width: 900px;
}

.vendor-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.response-body {
  margin-top: 20px;

  .response-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-weight: 600;
  }
}

.json-viewer {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
  max-height: 400px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}
</style>
