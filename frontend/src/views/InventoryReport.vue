<template>
  <div class="inventory-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点差异报告</span>
          <el-button type="primary" @click="goBack">返回任务列表</el-button>
        </div>
      </template>

      <el-descriptions :column="3" border class="task-info" v-if="task">
        <el-descriptions-item label="任务编号">{{ task.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务标题">{{ task.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="盘点部门">{{ task.departmentId || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="盘点仓库">{{ task.location || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ task.assigneeName }}</el-descriptions-item>
      </el-descriptions>

      <el-row :gutter="16" class="summary-row" v-if="report">
        <el-col :span="3">
          <el-statistic title="总资产数" :value="report.totalCount" />
        </el-col>
        <el-col :span="3">
          <el-statistic title="已盘点" :value="report.checkedCount" />
        </el-col>
        <el-col :span="3">
          <el-statistic title="一致" :value="report.matchCount">
            <template #suffix>
              <el-tag type="success" size="small" v-if="report.matchCount > 0">正常</el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="3">
          <el-statistic title="盘盈" :value="report.surplusCount">
            <template #suffix>
              <el-tag type="warning" size="small" v-if="report.surplusCount > 0">多出</el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="3">
          <el-statistic title="盘亏" :value="report.shortageCount">
            <template #suffix>
              <el-tag type="danger" size="small" v-if="report.shortageCount > 0">缺少</el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="3">
          <el-statistic title="状态差异" :value="report.statusMismatchCount">
            <template #suffix>
              <el-tag type="danger" size="small" v-if="report.statusMismatchCount > 0">异常</el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="3">
          <el-statistic title="位置差异" :value="report.locationMismatchCount">
            <template #suffix>
              <el-tag type="warning" size="small" v-if="report.locationMismatchCount > 0">异常</el-tag>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="3">
          <el-statistic title="状态+位置差异" :value="report.bothMismatchCount">
            <template #suffix>
              <el-tag type="danger" size="small" v-if="report.bothMismatchCount > 0">异常</el-tag>
            </template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider />

      <h3>差异明细</h3>
      <el-table :data="diffDetails" style="width: 100%" v-loading="loading" empty-text="无差异记录">
        <el-table-column prop="assetNo" label="资产编号" width="130" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="bookDepartment" label="账面部门" width="100" />
        <el-table-column prop="bookStatus" label="账面状态" width="100">
          <template #default="scope">
            <el-tag size="small">{{ getAssetStatusText(scope.row.bookStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookLocation" label="账面位置" width="120" />
        <el-table-column prop="actualStatus" label="实际状态" width="100">
          <template #default="scope">
            <el-tag size="small" type="warning">{{ getAssetStatusText(scope.row.actualStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualLocation" label="实际位置" width="120" />
        <el-table-column label="差异类型" width="110">
          <template #default="scope">
            <el-tag :type="getResultType(scope.row.result)" size="small">
              {{ getResultText(scope.row.result) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="150" />
        <el-table-column prop="checkTime" label="盘点时间" width="170" />
      </el-table>

      <el-divider />

      <h3>全部盘点记录</h3>
      <el-table :data="allDetails" style="width: 100%" v-loading="allLoading">
        <el-table-column prop="assetNo" label="资产编号" width="130" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="bookDepartment" label="账面部门" width="100" />
        <el-table-column prop="bookStatus" label="账面状态" width="100">
          <template #default="scope">
            <el-tag size="small">{{ getAssetStatusText(scope.row.bookStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualStatus" label="实际状态" width="100">
          <template #default="scope">
            <el-tag size="small" v-if="scope.row.actualStatus">{{ getAssetStatusText(scope.row.actualStatus) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.result" :type="getResultType(scope.row.result)" size="small">
              {{ getResultText(scope.row.result) }}
            </el-tag>
            <el-tag v-else type="info" size="small">待盘</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" min-width="150" />
        <el-table-column prop="checkTime" label="盘点时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const taskId = route.query.taskId

const loading = ref(false)
const allLoading = ref(false)
const task = ref(null)
const report = ref(null)
const allDetails = ref([])

const diffDetails = computed(() => {
  if (!report.value) return []
  return report.value.diffDetails || []
})

const fetchTask = async () => {
  try {
    const res = await axios.get('/api/inventory/task/list', {
      params: { page: 1, size: 100 }
    })
    if (res.data.code === 200) {
      task.value = res.data.data.records.find(r => r.id === Number(taskId))
    }
  } catch (error) {
    console.error('Failed to fetch task', error)
  }
}

const fetchReport = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/inventory/report', { params: { taskId } })
    if (res.data.code === 200) {
      report.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取报告失败')
  } finally {
    loading.value = false
  }
}

const fetchAllDetails = async () => {
  allLoading.value = true
  try {
    const res = await axios.get('/api/inventory/detail/all', { params: { taskId } })
    if (res.data.code === 200) {
      allDetails.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取明细失败')
  } finally {
    allLoading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getAssetStatusText = (status) => {
  const map = {
    'NORMAL': '正常',
    'BORROWED': '借出',
    'MAINTENANCE': '维修中',
    'SCRAPPED': '已报废',
    'MISSING': '缺失'
  }
  return map[status] || status
}

const getResultType = (result) => {
  const map = {
    'MATCH': 'success',
    'SURPLUS': 'warning',
    'SHORTAGE': 'danger',
    'STATUS_MISMATCH': 'danger',
    'LOCATION_MISMATCH': 'warning',
    'BOTH_MISMATCH': 'danger'
  }
  return map[result] || 'info'
}

const getResultText = (result) => {
  const map = {
    'MATCH': '一致',
    'SURPLUS': '盘盈',
    'SHORTAGE': '盘亏',
    'STATUS_MISMATCH': '状态差异',
    'LOCATION_MISMATCH': '位置差异',
    'BOTH_MISMATCH': '状态+位置差异'
  }
  return map[result] || result
}

const goBack = () => {
  router.push('/inventory')
}

onMounted(() => {
  fetchTask()
  fetchReport()
  fetchAllDetails()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-info {
  margin-bottom: 20px;
}
.summary-row {
  margin: 20px 0;
}
h3 {
  margin: 10px 0 16px;
  color: #303133;
}
</style>
