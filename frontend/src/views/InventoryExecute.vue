<template>
  <div class="inventory-execute">
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <span>执行盘点 - {{ taskInfo.taskName || '' }}</span>
        <el-tag v-if="taskInfo.status" :type="getStatusType(taskInfo.status)" style="margin-left: 10px">
          {{ getStatusText(taskInfo.status) }}
        </el-tag>
      </template>
    </el-page-header>

    <el-card class="stat-card">
      <el-row :gutter="20">
        <el-col :span="6"><div class="stat-item"><div class="stat-num">{{ taskInfo.totalCount || 0 }}</div><div class="stat-label">总资产</div></div></el-col>
        <el-col :span="6"><div class="stat-item checked"><div class="stat-num">{{ taskInfo.checkedCount || 0 }}</div><div class="stat-label">已盘点</div></div></el-col>
        <el-col :span="6"><div class="stat-item surplus"><div class="stat-num">{{ taskInfo.surplusCount || 0 }}</div><div class="stat-label">盘盈</div></div></el-col>
        <el-col :span="6"><div class="stat-item deficit"><div class="stat-num">{{ taskInfo.lossCount || 0 }}</div><div class="stat-label">盘亏</div></div></el-col>
      </el-row>
    </el-card>

    <el-card>
      <el-form :inline="true" class="search-form">
        <el-form-item label="筛选">
          <el-select v-model="filterResult" placeholder="全部" style="width: 150px" @change="fetchData">
            <el-option label="全部" value="ALL" />
            <el-option label="待盘点" value="PENDING" />
            <el-option label="账实相符" value="MATCH" />
            <el-option label="盘盈" value="SURPLUS" />
            <el-option label="盘亏" value="LOSS" />
            <el-option label="状态差异" value="STATUS_DIFF" />
            <el-option label="地点差异" value="LOCATION_DIFF" />
            <el-option label="双重差异" value="BOTH_DIFF" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">刷新</el-button>
          <el-button type="warning" @click="addSurplusDialog = true">登记盘盈资产</el-button>
          <el-button v-if="taskInfo.status === 'IN_PROGRESS'" type="success" @click="handleComplete">完成盘点</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" :row-class-name="rowClassName">
        <el-table-column prop="assetNo" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="资产名称" width="120" />
        <el-table-column label="账面状态" width="90">
          <template #default="scope">
            <el-tag size="small">{{ getStatusText(scope.row.bookStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookLocation" label="账面地点" width="100" />
        <el-table-column label="实际状况" width="120">
          <template #default="scope">
            <el-select v-if="scope.row.assetId !== null" v-model="scope.row.actualStatus" size="small" placeholder="请选择" style="width: 110px">
              <el-option label="正常" value="NORMAL" />
              <el-option label="损坏" value="DAMAGED" />
              <el-option label="丢失" value="LOST" />
            </el-select>
            <el-tag v-else type="warning" size="small">盘盈</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实际地点" width="130">
          <template #default="scope">
            <el-input v-model="scope.row.actualLocation" size="small" placeholder="同账面留空" :disabled="scope.row.assetId === null" />
          </template>
        </el-table-column>
        <el-table-column prop="resultType" label="盘点结果" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isChecked" size="small" :type="getResultType(scope.row.resultType)">{{ getResultText(scope.row.resultType) }}</el-tag>
            <span v-else class="pending-text">待盘点</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="scope">
            <el-input v-model="scope.row.remarks" size="small" placeholder="备注说明" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button size="small" type="primary" @click="submitCheck(scope.row)" 
              :disabled="taskInfo.status === 'COMPLETED'">提交</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" @current-change="handlePageChange" />
      </div>
    </el-card>

    <el-dialog v-model="addSurplusDialog" title="登记盘盈资产" width="450px">
      <el-form :model="surplusForm" label-width="100px">
        <el-form-item label="资产编号"><el-input v-model="surplusForm.assetNo" placeholder="现场资产编号" /></el-form-item>
        <el-form-item label="资产名称"><el-input v-model="surplusForm.assetName" placeholder="资产名称" /></el-form-item>
        <el-form-item label="存放地点"><el-input v-model="surplusForm.actualLocation" placeholder="发现地点" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="surplusForm.remarks" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addSurplusDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSurplus">提交登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const taskId = route.params.id

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(20)
const currentPage = ref(1)
const filterResult = ref('ALL')
const taskInfo = ref({})
const addSurplusDialog = ref(false)
const surplusForm = reactive({ assetNo: '', assetName: '', actualLocation: '', remarks: '' })

const fetchTaskInfo = async () => {
  try {
    const res = await axios.get(`/api/inventory/task/${taskId}`)
    if (res.data.code === 200) taskInfo.value = res.data.data
  } catch (e) {}
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (filterResult.value !== 'ALL') params.resultType = filterResult.value
    const res = await axios.get(`/api/inventory/item/list/${taskId}`, { params })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (val) => { currentPage.value = val; fetchData() }

const submitCheck = async (row) => {
  if (row.assetId !== null && !row.actualStatus) {
    ElMessage.warning('请选择资产实际状况')
    return
  }
  try {
    const res = await axios.post('/api/inventory/item/check', {
      id: row.id,
      actualStatus: row.actualStatus,
      actualLocation: row.actualLocation,
      remarks: row.remarks
    })
    if (res.data.code === 200) {
      ElMessage.success('盘点记录已提交')
      Object.assign(row, res.data.data)
      fetchTaskInfo()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const submitSurplus = async () => {
  if (!surplusForm.assetName) { ElMessage.warning('请输入资产名称'); return }
  try {
    const res = await axios.post(`/api/inventory/item/surplus/${taskId}`, surplusForm)
    if (res.data.code === 200) {
      ElMessage.success('盘盈资产已登记')
      addSurplusDialog.value = false
      Object.assign(surplusForm, { assetNo: '', assetName: '', actualLocation: '', remarks: '' })
      fetchData()
      fetchTaskInfo()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleComplete = () => {
  const pending = (taskInfo.value.totalCount || 0) - (taskInfo.value.checkedCount || 0)
  if (pending > 0) {
    ElMessage.error(`还有 ${pending} 项资产未盘点，无法完成任务`)
    return
  }
  ElMessageBox.confirm('确认完成本次盘点？完成后不可再修改盘点记录。').then(async () => {
    try {
      const res = await axios.post(`/api/inventory/task/complete/${taskId}`)
      if (res.data.code === 200) {
        ElMessage.success('盘点已完成')
        router.push(`/inventory/report/${taskId}`)
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const goBack = () => { router.push('/inventory') }

const getStatusType = (s) => ({ 'PENDING': 'info', 'IN_PROGRESS': 'warning', 'COMPLETED': 'success' }[s] || '')
const getStatusText = (s) => ({ 'NORMAL': '正常', 'BORROWED': '借出', 'MAINTENANCE': '维修中', 'SCRAPPED': '已报废', 'PENDING': '待开始', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成' }[s] || s || '-')
const getResultType = (r) => ({ 'MATCH': 'success', 'SURPLUS': 'warning', 'LOSS': 'danger', 'STATUS_DIFF': 'danger', 'LOCATION_DIFF': 'warning', 'BOTH_DIFF': 'danger' }[r] || 'info')
const getResultText = (r) => ({ 'MATCH': '相符', 'SURPLUS': '盘盈', 'LOSS': '盘亏', 'STATUS_DIFF': '状态差异', 'LOCATION_DIFF': '地点差异', 'BOTH_DIFF': '双重差异' }[r] || r || '-')
const rowClassName = ({ row }) => {
  if (!row.isChecked) return 'row-pending'
  if (row.resultType !== 'MATCH') return 'row-diff'
  return ''
}

onMounted(() => {
  fetchTaskInfo()
  fetchData()
})
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.stat-card { margin-bottom: 16px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-num { font-size: 28px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }
.stat-item.checked .stat-num { color: #67C23A; }
.stat-item.surplus .stat-num { color: #E6A23C; }
.stat-item.deficit .stat-num { color: #F56C6C; }
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.pending-text { color: #909399; font-size: 13px; }
:deep(.row-pending) { background-color: #fdf6ec; }
:deep(.row-diff) { background-color: #fef0f0; }
</style>
