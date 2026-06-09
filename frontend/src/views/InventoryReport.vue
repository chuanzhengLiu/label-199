<template>
  <div class="inventory-report">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span>盘点差异报告 - {{ report.task?.taskName || '' }}</span>
      </template>
    </el-page-header>

    <el-card v-if="report.task" class="summary-card">
      <template #header>
        <div class="card-header"><span>任务概览</span></div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="任务编号">{{ report.task.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ report.task.taskName }}</el-descriptions-item>
        <el-descriptions-item label="盘点范围">{{ getScopeText(report.task) }}</el-descriptions-item>
        <el-descriptions-item label="执行人">{{ report.task.assigneeName }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ report.task.createName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ report.task.createTime }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ report.task.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ report.task.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusType(report.task.status)">{{ getStatusText(report.task.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="16" class="stat-row">
      <el-col :span="4"><el-card class="stat-box total"><div class="num">{{ report.task?.totalCount || 0 }}</div><div class="label">总资产</div></el-card></el-col>
      <el-col :span="4"><el-card class="stat-box match"><div class="num">{{ summary.matchCount || 0 }}</div><div class="label">账实相符</div></el-card></el-col>
      <el-col :span="4"><el-card class="stat-box surplus"><div class="num">{{ summary.surplusCount || 0 }}</div><div class="label">盘盈</div></el-card></el-col>
      <el-col :span="4"><el-card class="stat-box deficit"><div class="num">{{ summary.lossCount || 0 }}</div><div class="label">盘亏</div></el-card></el-col>
      <el-col :span="4"><el-card class="stat-box status-diff"><div class="num">{{ summary.statusDiff || 0 }}</div><div class="label">状态差异</div></el-card></el-col>
      <el-col :span="4"><el-card class="stat-box loc-diff"><div class="num">{{ summary.locationDiff || 0 }}</div><div class="label">地点差异</div></el-card></el-col>
    </el-row>

    <el-card v-if="summary.bothDiff > 0" style="margin-bottom: 16px">
      <el-row :gutter="16">
        <el-col :span="4"><el-card class="stat-box both-diff" shadow="never"><div class="num">{{ summary.bothDiff }}</div><div class="label">双重差异</div></el-card></el-col>
        <el-col :span="4" v-if="summary.pendingCount > 0 && report.task?.status !== 'COMPLETED'"><el-card class="stat-box pending" shadow="never"><div class="num">{{ summary.pendingCount }}</div><div class="label">待盘点</div></el-card></el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>差异明细</span>
          <el-radio-group v-model="filterType" size="small" @change="filterItems">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="DIFF">仅差异</el-radio-button>
            <el-radio-button label="SURPLUS">盘盈</el-radio-button>
            <el-radio-button label="LOSS">盘亏</el-radio-button>
            <el-radio-button label="STATUS_DIFF">状态差异</el-radio-button>
            <el-radio-button label="LOCATION_DIFF">地点差异</el-radio-button>
            <el-radio-button label="BOTH_DIFF">双重差异</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="filteredItems" style="width: 100%" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="130" />
        <el-table-column prop="assetName" label="资产名称" width="140" />
        <el-table-column label="账面信息" width="200">
          <template #default="scope">
            <div>状态：<el-tag size="small">{{ getStatusText(scope.row.bookStatus) }}</el-tag></div>
            <div>地点：{{ scope.row.bookLocation || '-' }}</div>
            <div>部门：{{ scope.row.bookDepartment || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="实际盘点" width="200">
          <template #default="scope">
            <div v-if="scope.row.resultType === 'SURPLUS'">
              <el-tag type="warning" size="small">盘盈（账外资产）</el-tag>
            </div>
            <div v-else-if="scope.row.resultType === 'LOSS'">
              <el-tag type="danger" size="small">盘亏（资产丢失）</el-tag>
            </div>
            <div v-else-if="scope.row.isChecked">
              <div v-if="scope.row.actualStatus">实际状况：<el-tag size="small" :type="scope.row.bookStatus !== scope.row.actualStatus ? 'danger' : 'success'">{{ getActualStatusText(scope.row.actualStatus) }}</el-tag></div>
              <div v-if="scope.row.actualLocation">实际地点：{{ scope.row.actualLocation }}</div>
            </div>
            <div v-else><span class="pending-text">待盘点</span></div>
          </template>
        </el-table-column>
        <el-table-column prop="resultType" label="差异类型" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.isChecked" :type="getResultType(scope.row.resultType)">{{ getResultText(scope.row.resultType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" />
        <el-table-column prop="checkTime" label="盘点时间" width="170" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const taskId = route.params.id

const loading = ref(false)
const report = ref({})
const filterType = ref('DIFF')

const summary = computed(() => report.value.summary || {})
const allItems = computed(() => report.value.items || [])
const filteredItems = computed(() => {
  if (filterType.value === 'ALL') return allItems.value
  if (filterType.value === 'DIFF') return allItems.value.filter(i => i.isChecked && i.resultType !== 'MATCH')
  return allItems.value.filter(i => i.resultType === filterType.value)
})

const fetchReport = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/inventory/item/report/${taskId}`)
    if (res.data.code === 200) {
      report.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取报告失败')
  } finally {
    loading.value = false
  }
}

const filterItems = () => {}

const goBack = () => { router.push('/inventory') }

const getScopeText = (task) => {
  if (task.scopeType === 'ALL') return '全部资产'
  if (task.scopeType === 'DEPARTMENT') return `按部门：${task.scopeValue}`
  if (task.scopeType === 'WAREHOUSE') return `按地点：${task.scopeValue}`
  return '-'
}

const getStatusType = (s) => ({ 'PENDING': 'info', 'IN_PROGRESS': 'warning', 'COMPLETED': 'success' }[s] || '')
const getStatusText = (s) => ({ 'NORMAL': '正常', 'BORROWED': '借出', 'MAINTENANCE': '维修中', 'SCRAPPED': '已报废', 'PENDING': '待开始', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成' }[s] || s || '-')
const getActualStatusText = (s) => ({ 'NORMAL': '正常', 'DAMAGED': '损坏' }[s] || s || '-')
const getResultType = (r) => ({ 'MATCH': 'success', 'SURPLUS': 'warning', 'LOSS': 'danger', 'STATUS_DIFF': 'danger', 'LOCATION_DIFF': 'warning', 'BOTH_DIFF': 'danger' }[r] || 'info')
const getResultText = (r) => ({ 'MATCH': '账实相符', 'SURPLUS': '盘盈', 'LOSS': '盘亏', 'STATUS_DIFF': '状态差异', 'LOCATION_DIFF': '地点差异', 'BOTH_DIFF': '双重差异' }[r] || r || '-')

onMounted(() => { fetchReport() })
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.summary-card { margin-bottom: 16px; }
.stat-row { margin-bottom: 16px; }
.stat-box { text-align: center; }
.stat-box .num { font-size: 32px; font-weight: bold; }
.stat-box .label { font-size: 13px; color: #666; margin-top: 6px; }
.stat-box.total .num { color: #409EFF; }
.stat-box.match .num { color: #67C23A; }
.stat-box.surplus .num { color: #E6A23C; }
.stat-box.deficit .num { color: #F56C6C; }
.stat-box.status-diff .num { color: #F56C6C; }
.stat-box.loc-diff .num { color: #E6A23C; }
.stat-box.both-diff .num { color: #F56C6C; font-size: 28px; }
.stat-box.pending .num { color: #909399; font-size: 28px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pending-text { color: #909399; font-size: 13px; }
</style>
