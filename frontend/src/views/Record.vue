<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="title">操作记录</span>
      </div>

      <div class="search-bar">
        <el-select
          v-model="queryParams.vendorId"
          placeholder="选择厂商"
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in vendors"
            :key="item.id"
            :label="item.vendorName"
            :value="item.id"
          />
        </el-select>

        <el-select
          v-model="queryParams.operationType"
          placeholder="操作类型"
          clearable
          style="width: 120px"
        >
          <el-option label="拉取" value="pull" />
          <el-option label="推送" value="push" />
          <el-option label="同步" value="sync" />
          <el-option label="查询" value="query" />
          <el-option label="测试" value="test" />
        </el-select>

        <el-select
          v-model="queryParams.syncStatus"
          placeholder="同步状态"
          clearable
          style="width: 120px"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="2" />
          <el-option label="部分成功" :value="3" />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />

        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <el-icon><RefreshLeft /></el-icon> 重置
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="vendorName" label="厂商名称" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getOperationLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="syncStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.syncStatus)"
              size="small"
              class="status-tag"
              :class="getStatusType(row.syncStatus)"
            >
              {{ getStatusLabel(row.syncStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseCode" label="响应码" width="80">
          <template #default="{ row }">
            <el-text :type="row.responseCode >= 200 && row.responseCode < 300 ? 'success' : 'danger'">
              {{ row.responseCode || '-' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column prop="dataCount" label="数据量" width="80" />
        <el-table-column prop="executionTime" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.executionTime ? row.executionTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="detailVisible" title="记录详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="厂商名称">{{ currentRecord.vendorName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationLabel(currentRecord.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="同步状态">
          <el-tag :type="getStatusType(currentRecord.syncStatus)">
            {{ getStatusLabel(currentRecord.syncStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">
          {{ currentRecord.requestUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentRecord.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="响应状态码">{{ currentRecord.responseCode }}</el-descriptions-item>
        <el-descriptions-item label="数据量">{{ currentRecord.dataCount }} 条</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ currentRecord.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRecord.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ currentRecord.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.errorMessage" label="错误信息" :span="2">
          <el-text type="danger">{{ currentRecord.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="请求头" name="headers">
          <pre class="json-viewer">{{ formatJson(currentRecord?.requestHeaders) }}</pre>
        </el-tab-pane>
        <el-tab-pane label="请求体" name="body">
          <pre class="json-viewer">{{ formatJson(currentRecord?.requestBody) }}</pre>
        </el-tab-pane>
        <el-tab-pane label="响应数据" name="response">
          <pre class="json-viewer">{{ formatJson(currentRecord?.responseBody) }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { Search, RefreshLeft, View } from '@element-plus/icons-vue'
import { vendorApi, recordApi } from '@/api'

const loading = ref(false)
const detailVisible = ref(false)
const tableData = ref([])
const vendors = ref([])
const total = ref(0)
const currentRecord = ref(null)
const activeTab = ref('response')
const dateRange = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  vendorId: null,
  operationType: null,
  syncStatus: null,
  startTime: null,
  endTime: null
})

watch(dateRange, (val) => {
  if (val) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = null
    queryParams.endTime = null
  }
})

const getOperationLabel = (type) => {
  const map = { pull: '拉取', push: '推送', sync: '同步', query: '查询', test: '测试' }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { 0: '待处理', 1: '成功', 2: '失败', 3: '部分成功' }
  return map[status] || '未知'
}

const formatJson = (str) => {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await recordApi.page(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // Handled by interceptor
  } finally {
    loading.value = false
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

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    vendorId: null,
    operationType: null,
    syncStatus: null,
    startTime: null,
    endTime: null
  })
  dateRange.value = null
  loadData()
}

const showDetail = async (row) => {
  try {
    const res = await recordApi.getById(row.id)
    currentRecord.value = res.data
    activeTab.value = 'response'
    detailVisible.value = true
  } catch (error) {
    // Handled by interceptor
  }
}

onMounted(() => {
  loadData()
  loadVendors()
})
</script>

<style lang="scss" scoped>
.detail-tabs {
  margin-top: 20px;
}

.json-viewer {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  overflow: auto;
  max-height: 300px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  margin: 0;
}
</style>
