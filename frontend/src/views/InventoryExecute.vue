<template>
  <div class="inventory-execute">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
            <span class="title">执行盘点 - {{ taskInfo.taskName }}</span>
          </div>
        </div>
      </template>

      <div class="execute-header">
        <div class="execute-info">
          <span>执行人：{{ taskInfo.executorName }}</span>
          <span class="progress-text">进度：{{ checkedCount }}/{{ totalCount }}</span>
        </div>
        <el-progress :percentage="progressPercent" :status="progressPercent === 100 ? 'success' : ''" />
        <div class="scan-section">
          <el-input 
            v-model="scanAssetNo" 
            placeholder="输入或扫描资产编号" 
            style="width: 300px"
            @keyup.enter="handleScan"
          >
            <template #append>
              <el-button @click="handleScan">查询</el-button>
            </template>
          </el-input>
          <el-button type="success" @click="handleAddProfit" style="margin-left: 10px">新增盘盈</el-button>
          <el-button v-if="user.role === 'ADMIN'" type="warning" @click="handleComplete" style="margin-left: 10px">
            完成盘点
          </el-button>
        </div>
        <div class="filter-section">
          <el-radio-group v-model="detailFilter" size="small" @change="fetchDetails">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="unchecked">未盘点</el-radio-button>
            <el-radio-button value="checked">已盘点</el-radio-button>
            <el-radio-button value="PROFIT">盘盈</el-radio-button>
            <el-radio-button value="LOSS">盘亏</el-radio-button>
            <el-radio-button value="STATUS_DIFF">状态差异</el-radio-button>
            <el-radio-button value="LOCATION_DIFF">位置差异</el-radio-button>
            <el-radio-button value="BOTH_DIFF">双重差异</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <el-table :data="detailList" style="width: 100%; margin-top: 15px" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="名称" width="150" />
        <el-table-column prop="bookStatus" label="账面状态" width="100">
          <template #default="scope">
            <el-tag size="small">{{ getAssetStatusText(scope.row.bookStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookLocation" label="账面位置" width="120" />
        <el-table-column prop="actualStatus" label="实际状态" width="100">
          <template #default="scope">
            <el-tag size="small" v-if="scope.row.actualStatus">{{ getAssetStatusText(scope.row.actualStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="actualLocation" label="实际位置" width="120" />
        <el-table-column prop="checkResult" label="盘点结果" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isChecked" :type="getResultType(scope.row.checkResult)" size="small">
              {{ getResultText(scope.row.checkResult) }}
            </el-tag>
            <el-tag v-else type="info" size="small">未盘点</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="盘点时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleCheckItem(scope.row)">
              {{ scope.row.isChecked ? '修改' : '盘点' }}
            </el-button>
            <el-button v-if="!scope.row.isChecked" size="small" type="danger" @click="handleMarkLoss(scope.row)">
              盘亏
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="checkDialogVisible" title="资产盘点" width="40%">
      <el-form :model="checkForm" label-width="100px">
        <el-form-item label="资产编号">
          <el-input v-model="checkForm.assetNo" disabled />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="checkForm.assetName" disabled />
        </el-form-item>
        <el-form-item label="账面状态">
          <el-input :value="getAssetStatusText(checkForm.bookStatus)" disabled />
        </el-form-item>
        <el-form-item label="账面位置">
          <el-input v-model="checkForm.bookLocation" disabled />
        </el-form-item>
        <el-form-item label="实际状态">
             <el-select v-model="checkForm.actualStatus" placeholder="请选择实际状态" style="width: 100%">
                 <el-option label="正常" value="NORMAL" />
                 <el-option label="借出" value="BORROWED" />
                 <el-option label="维修中" value="MAINTENANCE" />
                 <el-option label="已报废" value="SCRAPPED" />
             </el-select>
        </el-form-item>
        <el-form-item label="实际位置">
          <el-input v-model="checkForm.actualLocation" placeholder="请输入实际位置" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheck">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="profitDialogVisible" title="新增盘盈资产" width="40%">
      <el-form :model="profitForm" label-width="100px">
        <el-form-item label="资产编号">
          <el-input v-model="profitForm.assetNo" placeholder="请输入资产编号" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="profitForm.assetName" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item label="实际状态">
             <el-select v-model="profitForm.actualStatus" placeholder="请选择实际状态" style="width: 100%">
                 <el-option label="正常" value="NORMAL" />
                 <el-option label="借出" value="BORROWED" />
                 <el-option label="维修中" value="MAINTENANCE" />
                 <el-option label="已报废" value="SCRAPPED" />
             </el-select>
        </el-form-item>
        <el-form-item label="实际位置">
          <el-input v-model="profitForm.actualLocation" placeholder="请输入实际位置" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="profitForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProfit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const loading = ref(false)
const taskId = computed(() => route.params.id)
const taskInfo = reactive({})
const detailList = ref([])
const detailFilter = ref('')
const scanAssetNo = ref('')
const checkedCount = ref(0)
const totalCount = ref(0)

const checkDialogVisible = ref(false)
const checkForm = reactive({
  id: null,
  taskId: null,
  assetNo: '',
  assetName: '',
  bookStatus: '',
  bookLocation: '',
  actualStatus: '',
  actualLocation: '',
  remark: ''
})

const profitDialogVisible = ref(false)
const profitForm = reactive({
  taskId: null,
  assetNo: '',
  assetName: '',
  actualStatus: '',
  actualLocation: '',
  remark: ''
})

const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((checkedCount.value / totalCount.value) * 100)
})

const fetchTaskInfo = async () => {
  try {
    const res = await axios.get(`/api/inventory/task/${taskId.value}`)
    if (res.data.code === 200) {
      Object.assign(taskInfo, res.data.data)
      totalCount.value = res.data.data.totalCount
      checkedCount.value = res.data.data.checkedCount
    }
  } catch (error) {
    ElMessage.error('获取任务信息失败')
  }
}

const fetchDetails = async () => {
  loading.value = true
  try {
    const params = {}
    if (detailFilter.value === 'unchecked') {
      params.isChecked = 0
    } else if (detailFilter.value === 'checked') {
      params.isChecked = 1
    } else if (detailFilter.value && detailFilter.value !== '') {
      params.checkResult = detailFilter.value
    }
    
    const res = await axios.get(`/api/inventory/detail/list/${taskId.value}`, { params })
    if (res.data.code === 200) {
      detailList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取明细失败')
  } finally {
    loading.value = false
  }
}

const refreshTaskStats = async () => {
  try {
    const res = await axios.get(`/api/inventory/task/${taskId.value}`)
    if (res.data.code === 200) {
      checkedCount.value = res.data.data.checkedCount
      totalCount.value = res.data.data.totalCount
    }
  } catch (error) {
    console.error('Failed to refresh task stats', error)
  }
}

const handleBack = () => {
  router.push('/inventory')
}

const handleScan = async () => {
  if (!scanAssetNo.value) {
    ElMessage.warning('请输入资产编号')
    return
  }
  try {
    const res = await axios.get(`/api/inventory/detail/scan/${taskId.value}`, {
      params: { assetNo: scanAssetNo.value }
    })
    if (res.data.code === 200 && res.data.data) {
      const detail = res.data.data
      if (detail.id) {
        handleCheckItem(detail)
      } else {
        Object.assign(profitForm, {
          taskId: taskId.value,
          assetNo: detail.assetNo,
          assetName: detail.assetName,
          actualStatus: detail.bookStatus,
          actualLocation: detail.bookLocation,
          remark: ''
        })
        profitDialogVisible.value = true
      }
    } else {
      ElMessage.warning('未找到该资产，请确认编号是否正确')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
  scanAssetNo.value = ''
}

const handleCheckItem = (row) => {
  Object.assign(checkForm, {
    id: row.id,
    taskId: row.taskId,
    assetNo: row.assetNo,
    assetName: row.assetName,
    bookStatus: row.bookStatus,
    bookLocation: row.bookLocation,
    actualStatus: row.actualStatus || row.bookStatus,
    actualLocation: row.actualLocation || row.bookLocation,
    remark: row.remark || ''
  })
  checkDialogVisible.value = true
}

const submitCheck = async () => {
  try {
    const res = await axios.post('/api/inventory/detail/check', checkForm)
    if (res.data.code === 200) {
      ElMessage.success('盘点成功')
      checkDialogVisible.value = false
      fetchDetails()
      refreshTaskStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMarkLoss = (row) => {
  ElMessageBox.confirm('确认将该资产标记为盘亏？')
    .then(async () => {
      try {
        const res = await axios.post('/api/inventory/detail/mark-loss', null, {
          params: { taskId: taskId.value, detailId: row.id }
        })
        if (res.data.code === 200) {
          ElMessage.success('已标记为盘亏')
          fetchDetails()
          refreshTaskStats()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const handleAddProfit = () => {
  Object.assign(profitForm, {
    taskId: taskId.value,
    assetNo: '',
    assetName: '',
    actualStatus: 'NORMAL',
    actualLocation: '',
    remark: ''
  })
  profitDialogVisible.value = true
}

const submitProfit = async () => {
  if (!profitForm.assetNo) {
    ElMessage.warning('请输入资产编号')
    return
  }
  if (!profitForm.assetName) {
    ElMessage.warning('请输入资产名称')
    return
  }
  try {
    const res = await axios.post('/api/inventory/detail/add-profit', profitForm)
    if (res.data.code === 200) {
      ElMessage.success('盘盈资产已添加')
      profitDialogVisible.value = false
      fetchDetails()
      refreshTaskStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComplete = () => {
  ElMessageBox.confirm('确认完成该盘点任务？完成后将无法修改。')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/task/complete/${taskId.value}`)
        if (res.data.code === 200) {
          ElMessage.success('任务已完成')
          router.push('/inventory')
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const getAssetStatusText = (status) => {
  const map = {
    'NORMAL': '正常',
    'BORROWED': '借出',
    'MAINTENANCE': '维修中',
    'SCRAPPED': '已报废'
  }
  return map[status] || status
}

const getResultType = (result) => {
  const map = {
    'NORMAL': 'success',
    'PROFIT': 'success',
    'LOSS': 'danger',
    'STATUS_DIFF': 'warning',
    'LOCATION_DIFF': 'warning',
    'BOTH_DIFF': 'danger'
  }
  return map[result] || 'info'
}

const getResultText = (result) => {
  const map = {
    'NORMAL': '正常',
    'PROFIT': '盘盈',
    'LOSS': '盘亏',
    'STATUS_DIFF': '状态差异',
    'LOCATION_DIFF': '位置差异',
    'BOTH_DIFF': '双重差异'
  }
  return map[result] || result
}

onMounted(() => {
  fetchTaskInfo()
  fetchDetails()
})
</script>

<style scoped>
.execute-header {
  margin-bottom: 15px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.title {
  font-size: 16px;
  font-weight: bold;
}
.execute-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.progress-text {
  font-weight: bold;
  color: #409EFF;
}
.scan-section {
  margin-top: 15px;
  display: flex;
  align-items: center;
}
.filter-section {
  margin-top: 15px;
}
</style>
