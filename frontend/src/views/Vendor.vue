<template>
  <div class="page-container">
    <div class="card">
      <div class="card-header">
        <span class="title">厂商管理</span>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon> 新增厂商
        </el-button>
      </div>

      <div class="search-bar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索厂商名称或编码"
          clearable
          style="width: 240px"
          @clear="loadData"
          @keyup.enter="loadData"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="vendorCode" label="厂商编码" width="120" />
        <el-table-column prop="vendorName" label="厂商名称" min-width="150" />
        <el-table-column prop="apiBaseUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="adapterType" label="适配器类型" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.adapterType || 'default' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authType" label="认证方式" width="100">
          <template #default="{ row }">
            {{ getAuthLabel(row.authType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              size="small"
              class="status-tag"
              :class="row.status === 1 ? 'success' : 'info'"
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="testConnection(row)">
              <el-icon><Connection /></el-icon> 测试
            </el-button>
            <el-button type="primary" link @click="openDialog(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑厂商' : '新增厂商'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="厂商编码" prop="vendorCode">
          <el-input v-model="form.vendorCode" placeholder="请输入厂商编码" />
        </el-form-item>
        <el-form-item label="厂商名称" prop="vendorName">
          <el-input v-model="form.vendorName" placeholder="请输入厂商名称" />
        </el-form-item>
        <el-form-item label="API地址" prop="apiBaseUrl">
          <el-input v-model="form.apiBaseUrl" placeholder="请输入API基础地址" />
        </el-form-item>
        <el-form-item label="API版本" prop="apiVersion">
          <el-input v-model="form.apiVersion" placeholder="如: v1, v2" />
        </el-form-item>
        <el-form-item label="适配器类型" prop="adapterType">
          <el-select v-model="form.adapterType" placeholder="请选择适配器类型" style="width: 100%">
            <el-option
              v-for="item in adapterTypes"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="form.authType" placeholder="请选择认证方式" style="width: 100%">
            <el-option label="无认证" value="none" />
            <el-option label="API Key" value="api_key" />
            <el-option label="Basic认证" value="basic" />
            <el-option label="Bearer Token" value="bearer" />
            <el-option label="OAuth2.0" value="oauth2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.authType !== 'none'" label="认证配置" prop="authConfig">
          <el-input
            v-model="form.authConfig"
            type="textarea"
            :rows="4"
            placeholder="请输入JSON格式的认证配置"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Connection } from '@element-plus/icons-vue'
import { vendorApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const adapterTypes = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

const formRef = ref(null)
const form = reactive({
  id: null,
  vendorCode: '',
  vendorName: '',
  apiBaseUrl: '',
  apiVersion: '',
  authType: 'none',
  authConfig: '',
  adapterType: 'default',
  description: '',
  status: 1
})

const rules = {
  vendorCode: [{ required: true, message: '请输入厂商编码', trigger: 'blur' }],
  vendorName: [{ required: true, message: '请输入厂商名称', trigger: 'blur' }],
  apiBaseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }]
}

const getAuthLabel = (type) => {
  const map = { none: '无', api_key: 'API Key', basic: 'Basic', bearer: 'Bearer', oauth2: 'OAuth2' }
  return map[type] || type
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await vendorApi.page(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // Handled by interceptor
  } finally {
    loading.value = false
  }
}

const loadAdapterTypes = async () => {
  try {
    const res = await vendorApi.getAdapterTypes()
    adapterTypes.value = res.data
  } catch (error) {
    adapterTypes.value = ['default', 'rest_api', 'json_rpc', 'soap']
  }
}

const openDialog = (row = null) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: null,
      vendorCode: '',
      vendorName: '',
      apiBaseUrl: '',
      apiVersion: '',
      authType: 'none',
      authConfig: '',
      adapterType: 'default',
      description: '',
      status: 1
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await vendorApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await vendorApi.create(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    // Handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除厂商 "${row.vendorName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await vendorApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // Handled by interceptor
    }
  }).catch(() => {})
}

const testConnection = async (row) => {
  try {
    const res = await vendorApi.testConnection(row.id)
    if (res.data) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    // Handled by interceptor
  }
}

onMounted(() => {
  loadData()
  loadAdapterTypes()
})
</script>
