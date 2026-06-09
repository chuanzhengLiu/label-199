<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资产盘点</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="任务名称">
          <el-input v-model="searchForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="状态">
             <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
                 <el-option label="待开始" value="PENDING" />
                 <el-option label="进行中" value="IN_PROGRESS" />
                 <el-option label="已完成" value="COMPLETED" />
             </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button v-if="user.role === 'ADMIN'" type="success" @click="handleCreate">新建任务</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column prop="taskType" label="盘点类型" width="100">
          <template #default="scope">
            {{ getTaskTypeText(scope.row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column prop="targetValue" label="盘点范围" width="150" />
        <el-table-column prop="executorName" label="执行人" width="100" />
        <el-table-column prop="totalCount" label="盘点总数" width="100" />
        <el-table-column prop="checkedCount" label="已盘点" width="80" />
        <el-table-column prop="profitCount" label="盘盈" width="80" />
        <el-table-column prop="lossCount" label="盘亏" width="80" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <el-button-group>
              <el-button v-if="scope.row.status === 'PENDING' && user.role === 'ADMIN'" size="small" type="primary" @click="handleStart(scope.row)">开始</el-button>
              <el-button v-if="scope.row.status === 'IN_PROGRESS' && (user.role === 'ADMIN' || user.id === scope.row.executorId)" size="small" type="success" @click="handleExecute(scope.row)">执行盘点</el-button>
              <el-button v-if="scope.row.status === 'IN_PROGRESS' && user.role === 'ADMIN'" size="small" type="warning" @click="handleComplete(scope.row)">完成</el-button>
              <el-button v-if="scope.row.status === 'COMPLETED'" size="small" type="info" @click="handleReport(scope.row)">报告</el-button>
              <el-button v-if="user.role === 'ADMIN'" size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
            </el-button-group>
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

    <el-dialog v-model="dialogVisible" title="新建盘点任务" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="盘点类型">
          <el-radio-group v-model="form.taskType">
            <el-radio value="DEPARTMENT">按部门</el-radio>
            <el-radio value="LOCATION">按仓库</el-radio>
            <el-radio value="ALL">全部资产</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.taskType === 'DEPARTMENT'" label="部门名称">
          <el-input v-model="form.targetValue" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item v-if="form.taskType === 'LOCATION'" label="仓库/位置">
          <el-input v-model="form.targetValue" placeholder="请输入仓库或位置名称" />
        </el-form-item>
        <el-form-item label="执行人">
            <el-select 
                v-model="form.executorId" 
                placeholder="请选择执行人" 
                style="width: 100%" 
                filterable 
                remote
                :remote-method="remoteSearchUser"
                :loading="userLoading"
            >
                <el-option
                    v-for="u in userList"
                    :key="u.id"
                    :label="`${u.realName} (${u.username})`"
                    :value="u.id"
                />
            </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchForm = reactive({ taskName: '', status: '' })
const user = JSON.parse(localStorage.getItem('user') || '{}')

const dialogVisible = ref(false)
const form = reactive({
  taskName: '',
  taskType: 'DEPARTMENT',
  targetValue: '',
  executorId: null,
  remark: ''
})

const userList = ref([])
const userLoading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchForm.taskName) params.taskName = searchForm.taskName
    if (searchForm.status) params.status = searchForm.status
    
    const res = await axios.get('/api/inventory/task/list', { params })
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

const remoteSearchUser = async (query) => {
  userLoading.value = true
  try {
    const res = await axios.get('/api/user/search', {
      params: { keyword: query }
    })
    if (res.data.code === 200) {
      userList.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to search users', error)
  } finally {
    userLoading.value = false
  }
}

const handleCreate = () => {
  Object.assign(form, { 
    taskName: '', 
    taskType: 'DEPARTMENT', 
    targetValue: '', 
    executorId: null,
    remark: '' 
  })
  remoteSearchUser('')
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.taskName) {
    ElMessage.warning('请输入任务名称')
    return
  }
  if (!form.executorId) {
    ElMessage.warning('请选择执行人')
    return
  }
  if ((form.taskType === 'DEPARTMENT' || form.taskType === 'LOCATION') && !form.targetValue) {
    ElMessage.warning('请输入盘点范围')
    return
  }
  try {
    const res = await axios.post('/api/inventory/task/create', form)
    if (res.data.code === 200) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleStart = async (row) => {
  ElMessageBox.confirm('确认开始该盘点任务？')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/task/start/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('任务已开始')
          fetchData()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const handleComplete = async (row) => {
  ElMessageBox.confirm('确认完成该盘点任务？完成后将无法修改。')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/task/complete/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('任务已完成')
          fetchData()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该盘点任务？相关明细也会被删除。')
    .then(async () => {
      try {
        const res = await axios.delete(`/api/inventory/task/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          fetchData()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleExecute = (row) => {
  router.push(`/inventory/execute/${row.id}`)
}

const handleReport = (row) => {
  router.push(`/inventory/report/${row.id}`)
}

const getTaskTypeText = (type) => {
  const map = {
    'DEPARTMENT': '按部门',
    'LOCATION': '按仓库',
    'ALL': '全部'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return map[status] || status
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.search-form {
    margin-bottom: 20px;
}
</style>
