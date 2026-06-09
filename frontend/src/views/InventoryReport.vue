<template>
  <div class="inventory-report">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>差异汇总报告</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <div v-if="reportData">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="任务编号">{{ reportData.task.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ reportData.task.name }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(reportData.task.status) }}</el-descriptions-item>
          <el-descriptions-item label="盘点范围">
            {{ reportData.task.scopeType === 'DEPARTMENT' ? '部门' : '仓库' }} / {{ reportData.task.scopeValue }}
          </el-descriptions-item>
          <el-descriptions-item label="执行人">{{ reportData.task.assigneeName }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ reportData.task.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="应盘总数">{{ reportData.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="已盘点数">{{ reportData.checkedCount }}</el-descriptions-item>
          <el-descriptions-item label="账实相符">{{ reportData.normalCount }}</el-descriptions-item>
          <el-descriptions-item label="盘盈">{{ reportData.profitCount }}</el-descriptions-item>
          <el-descriptions-item label="盘亏(含差异)">{{ reportData.lossCount }}</el-descriptions-item>
          <el-descriptions-item label="差异合计">{{ (reportData.profitCount || 0) + (reportData.lossCount || 0) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>差异明细</el-divider>
        <el-table :data="reportData.differences" style="width: 100%">
          <el-table-column prop="assetNo" label="资产编号" width="120" />
          <el-table-column prop="assetName" label="名称" width="160" />
          <el-table-column label="账面状态" width="110">
            <template #default="scope">{{ getAssetStatusText(scope.row.bookStatus) }}</template>
          </el-table-column>
          <el-table-column prop="bookLocation" label="账面位置" width="140" />
          <el-table-column label="实物状态" width="110">
            <template #default="scope">{{ getAssetStatusText(scope.row.actualStatus) }}</template>
          </el-table-column>
          <el-table-column prop="actualLocation" label="实物位置" width="140" />
          <el-table-column label="盘点结论" width="120">
            <template #default="scope">
              <el-tag :type="getResultType(scope.row.result)">{{ getResultText(scope.row.result) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <el-table-column prop="checkTime" label="盘点时间" width="180" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const taskId = ref(Number(route.params.id))
const loading = ref(false)
const reportData = ref(null)

const fetchReport = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/inventory/report/${taskId.value}`)
    if (res.data.code === 200) {
      reportData.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('获取报告失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/inventory')
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待执行', 'IN_PROGRESS': '执行中', 'COMPLETED': '已完成' }
  return map[status] || status
}
const getAssetStatusText = (status) => {
  const map = { 'NORMAL': '正常', 'BORROWED': '借出', 'MAINTENANCE': '维修中', 'SCRAPPED': '已报废' }
  return map[status] || status || '-'
}
const getResultType = (result) => {
  const map = {
    'MATCH': 'success',
    'STATUS_DIFF': 'warning',
    'LOCATION_DIFF': 'warning',
    'BOTH_DIFF': 'danger',
    'PROFIT': 'warning',
    'LOSS': 'danger',
    'UNCHECKED': 'info'
  }
  return map[result] || 'info'
}
const getResultText = (result) => {
  const map = {
    'MATCH': '账实相符',
    'STATUS_DIFF': '仅状态不同',
    'LOCATION_DIFF': '仅位置不同',
    'BOTH_DIFF': '状态与位置均不同',
    'PROFIT': '盘盈',
    'LOSS': '盘亏',
    'UNCHECKED': '未盘'
  }
  return map[result] || result
}

onMounted(() => {
  fetchReport()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
