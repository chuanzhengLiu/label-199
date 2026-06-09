<template>
  <div class="inventory-check">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点执行</span>
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
        <el-descriptions-item label="进度">
          {{ task.checkedCount || 0 }} / {{ task.totalCount || 0 }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="action-bar">
        <el-button type="success" @click="handleAddSurplus">盘盈登记</el-button>
        <el-tag type="info">未盘: {{ uncheckedCount }}</el-tag>
        <el-tag type="success">一致: {{ matchCount }}</el-tag>
        <el-tag type="danger">差异: {{ diffCount }}</el-tag>
        <el-tag type="danger">盘亏: {{ shortageCount }}</el-tag>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="130" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="bookDepartment" label="账面部门" width="100" />
        <el-table-column prop="bookStatus" label="账面状态" width="100">
          <template #default="scope">
            <el-tag size="small">{{ getAssetStatusText(scope.row.bookStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookLocation" label="账面位置" width="120" />
        <el-table-column label="实际状态" width="130">
          <template #default="scope">
            <el-select
              v-if="scope.row.checkTime"
              v-model="scope.row.actualStatus"
              size="small"
              disabled
            >
              <el-option v-for="s in assetStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-tag v-else-if="scope.row.result === 'SHORTAGE'" type="danger" size="small">缺失</el-tag>
            <el-select
              v-else
              v-model="scope.row.actualStatus"
              size="small"
              placeholder="选择状态"
            >
              <el-option v-for="s in assetStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="实际位置" width="140">
          <template #default="scope">
            <el-input
              v-if="scope.row.checkTime"
              v-model="scope.row.actualLocation"
              size="small"
              disabled
            />
            <span v-else-if="scope.row.result === 'SHORTAGE'">-</span>
            <el-input
              v-else
              v-model="scope.row.actualLocation"
              size="small"
              placeholder="实际位置"
            />
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
        <el-table-column label="备注" min-width="150">
          <template #default="scope">
            <el-input
              v-if="scope.row.checkTime"
              v-model="scope.row.remarks"
              size="small"
              disabled
            />
            <span v-else-if="scope.row.result === 'SHORTAGE'">{{ scope.row.remarks }}</span>
            <el-input
              v-else
              v-model="scope.row.remarks"
              size="small"
              placeholder="备注"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button
              v-if="!scope.row.checkTime && scope.row.result !== 'SHORTAGE'"
              size="small"
              type="primary"
              @click="handleCheckItem(scope.row)"
            >确认</el-button>
            <el-button
              v-if="!scope.row.checkTime && scope.row.result !== 'SHORTAGE'"
              size="small"
              type="danger"
              @click="handleMarkShortage(scope.row)"
            >盘亏</el-button>
            <el-button
              v-if="scope.row.checkTime"
              size="small"
              type="warning"
              plain
              @click="handleRecheck(scope.row)"
            >重盘</el-button>
            <el-button
              v-if="scope.row.result === 'SHORTAGE'"
              size="small"
              type="info"
              plain
              @click="handleUnmarkShortage(scope.row)"
            >撤销盘亏</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="surplusVisible" title="盘盈登记" width="40%">
      <el-form :model="surplusForm" label-width="100px">
        <el-form-item label="资产编号">
          <el-input v-model="surplusForm.assetNo" placeholder="请输入实物资产编号" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="surplusForm.assetName" placeholder="请输入实物资产名称" />
        </el-form-item>
        <el-form-item label="实际状态">
          <el-select v-model="surplusForm.actualStatus" placeholder="请选择" style="width: 100%">
            <el-option v-for="s in assetStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际位置">
          <el-input v-model="surplusForm.actualLocation" placeholder="请输入实际位置" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="surplusForm.remarks" type="textarea" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="surplusVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSurplus">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shortageVisible" title="盘亏登记" width="40%">
      <el-form :model="shortageForm" label-width="100px">
        <el-form-item label="资产编号">
          <el-input :model-value="shortageForm.assetNo" disabled />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input :model-value="shortageForm.assetName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="shortageForm.remarks" type="textarea" placeholder="请说明缺失原因或情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shortageVisible = false">取消</el-button>
        <el-button type="danger" @click="submitShortage">确认盘亏</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const taskId = route.query.taskId

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(20)
const currentPage = ref(1)
const task = ref(null)

const surplusVisible = ref(false)
const surplusForm = reactive({
  assetNo: '',
  assetName: '',
  actualStatus: 'NORMAL',
  actualLocation: '',
  remarks: ''
})

const shortageVisible = ref(false)
const shortageForm = reactive({
  id: null,
  assetNo: '',
  assetName: '',
  remarks: ''
})

const assetStatusOptions = [
  { label: '正常', value: 'NORMAL' },
  { label: '借出', value: 'BORROWED' },
  { label: '维修中', value: 'MAINTENANCE' },
  { label: '已报废', value: 'SCRAPPED' },
  { label: '缺失', value: 'MISSING' }
]

const uncheckedCount = computed(() => tableData.value.filter(d => !d.checkTime && d.result !== 'SHORTAGE').length)
const matchCount = computed(() => tableData.value.filter(d => d.result === 'MATCH').length)
const diffCount = computed(() => tableData.value.filter(d => d.result && d.result !== 'MATCH' && d.result !== 'SHORTAGE').length)
const shortageCount = computed(() => tableData.value.filter(d => d.result === 'SHORTAGE').length)

const fetchTask = async () => {
  try {
    const res = await axios.get('/api/inventory/task/list', {
      params: { page: 1, size: 1, assigneeId: user.id }
    })
    if (res.data.code === 200) {
      task.value = res.data.data.records.find(r => r.id === Number(taskId))
    }
  } catch (error) {
    console.error('Failed to fetch task', error)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/inventory/detail/list', {
      params: { page: currentPage.value, size: pageSize.value, taskId }
    })
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

const handlePageChange = (val) => {
  currentPage.value = val
  fetchData()
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

const handleCheckItem = async (row) => {
  if (!row.actualStatus) {
    ElMessage.warning('请选择实际状态')
    return
  }
  try {
    const res = await axios.post('/api/inventory/detail/check', {
      id: row.id,
      actualStatus: row.actualStatus,
      actualLocation: row.actualLocation || row.bookLocation,
      remarks: row.remarks
    })
    if (res.data.code === 200) {
      ElMessage.success('盘点确认成功')
      fetchData()
      fetchTask()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleRecheck = (row) => {
  ElMessageBox.confirm('确认重新盘点该资产？')
    .then(async () => {
      try {
        const res = await axios.post('/api/inventory/detail/check', {
          id: row.id,
          actualStatus: row.actualStatus,
          actualLocation: row.actualLocation,
          remarks: row.remarks
        })
        if (res.data.code === 200) {
          ElMessage.success('重新盘点成功')
          fetchData()
          fetchTask()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const handleAddSurplus = () => {
  Object.assign(surplusForm, {
    assetNo: '',
    assetName: '',
    actualStatus: 'NORMAL',
    actualLocation: '',
    remarks: ''
  })
  surplusVisible.value = true
}

const handleMarkShortage = (row) => {
  Object.assign(shortageForm, {
    id: row.id,
    assetNo: row.assetNo,
    assetName: row.assetName,
    remarks: ''
  })
  shortageVisible.value = true
}

const submitShortage = async () => {
  try {
    const res = await axios.post(`/api/inventory/detail/shortage/${shortageForm.id}`, null, {
      params: { remarks: shortageForm.remarks }
    })
    if (res.data.code === 200) {
      ElMessage.success('已标记为盘亏')
      shortageVisible.value = false
      fetchData()
      fetchTask()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleUnmarkShortage = (row) => {
  ElMessageBox.confirm('确认撤销该资产的盘亏标记？')
    .then(async () => {
      try {
        const res = await axios.post('/api/inventory/detail/check', {
          id: row.id,
          actualStatus: row.bookStatus,
          actualLocation: row.bookLocation,
          remarks: ''
        })
        if (res.data.code === 200) {
          ElMessage.success('已撤销盘亏标记')
          fetchData()
          fetchTask()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const submitSurplus = async () => {
  if (!surplusForm.assetNo || !surplusForm.assetName) {
    ElMessage.warning('请填写资产编号和名称')
    return
  }
  try {
    const res = await axios.post('/api/inventory/detail/surplus', {
      ...surplusForm,
      taskId: Number(taskId)
    })
    if (res.data.code === 200) {
      ElMessage.success('盘盈登记成功')
      surplusVisible.value = false
      fetchData()
      fetchTask()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goBack = () => {
  router.push('/inventory')
}

onMounted(() => {
  fetchTask()
  fetchData()
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
.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
