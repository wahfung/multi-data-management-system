<template>
  <div class="page-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-value">{{ dashboardData.vendorCount || 0 }}</div>
          <div class="stat-label">数据源厂商</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card success">
          <div class="stat-value">{{ dashboardData.activeVendorCount || 0 }}</div>
          <div class="stat-label">启用厂商</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card info">
          <div class="stat-value">{{ dashboardData.todayCount || 0 }}</div>
          <div class="stat-label">今日同步</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card warning">
          <div class="stat-value">{{ dashboardData.successRate || '0%' }}</div>
          <div class="stat-label">成功率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <div class="card">
          <div class="card-header">
            <span class="title">同步趋势 (近7天)</span>
          </div>
          <v-chart :option="chartOption" class="chart" autoresize />
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="card">
          <div class="card-header">
            <span class="title">同步统计</span>
          </div>
          <div class="stats-list">
            <div class="stats-item">
              <span class="label">总同步次数</span>
              <span class="value">{{ dashboardData.totalCount || 0 }}</span>
            </div>
            <div class="stats-item success">
              <span class="label">成功次数</span>
              <span class="value">{{ dashboardData.successCount || 0 }}</span>
            </div>
            <div class="stats-item danger">
              <span class="label">失败次数</span>
              <span class="value">{{ dashboardData.failCount || 0 }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="card">
      <div class="card-header">
        <span class="title">最近操作记录</span>
        <el-button type="primary" text @click="$router.push('/record')">
          查看更多 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <el-table :data="dashboardData.recentRecords || []" v-loading="loading">
        <el-table-column prop="vendorName" label="厂商名称" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getOperationLabel(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column prop="dataCount" label="数据量" width="80" />
        <el-table-column prop="executionTime" label="耗时" width="100">
          <template #default="{ row }">
            {{ row.executionTime ? row.executionTime + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { ArrowRight } from '@element-plus/icons-vue'
import { dashboardApi } from '@/api'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const dashboardData = reactive({})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['成功', '失败'],
    bottom: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: (dashboardData.weeklyTrend || []).map(item => item.date)
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '成功',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: 'rgba(103, 194, 58, 0.2)'
      },
      lineStyle: {
        color: '#67c23a'
      },
      itemStyle: {
        color: '#67c23a'
      },
      data: (dashboardData.weeklyTrend || []).map(item => item.success)
    },
    {
      name: '失败',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: 'rgba(245, 108, 108, 0.2)'
      },
      lineStyle: {
        color: '#f56c6c'
      },
      itemStyle: {
        color: '#f56c6c'
      },
      data: (dashboardData.weeklyTrend || []).map(item => item.fail)
    }
  ]
}))

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await dashboardApi.getData()
    Object.assign(dashboardData, res.data)
  } catch (error) {
    // Handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.stat-row {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
}

.stats-list {
  .stats-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-radius: 8px;
    background: #f5f7fa;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .label {
      color: #606266;
    }

    .value {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }

    &.success .value {
      color: #67c23a;
    }

    &.danger .value {
      color: #f56c6c;
    }
  }
}
</style>
