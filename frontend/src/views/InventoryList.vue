<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点任务</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button v-if="user.role === 'ADMIN'" type="success" @click="handleAdd">新建盘点</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column prop="title" label="任务标题" min-width="180" />
        <el-table-column prop="departmentId" label="盘点部门" width="120" />
        <el-table-column prop="location" label="盘点仓库" width="120" />
        <el-table-column prop="assigneeName" label="执行人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160">
          <template #default="scope">
            <el-progress
              :percentage="scope.row.totalCount ? Math.round(scope.row.checkedCount / scope.row.totalCount * 100) : 0"
              :status="getProgressStatus(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button
                v-if="scope.row.status === 'PENDING' && scope.row.assigneeId === user.id"
                size="small"
                type="primary"
                @click="handleStart(scope.row)"
              >开始盘点</el-button>
              <el-button
                v-if="scope.row.status === 'IN_PROGRESS' && scope.row.assigneeId === user.id"
                size="small"
                type="success"
                @click="handleCheck(scope.row)"
              >执行盘点</el-button>
              <el-button
                v-if="scope.row.status === 'IN_PROGRESS' && scope.row.assigneeId === user.id"
                size="small"
                type="warning"
                @click="handleComplete(scope.row)"
              >完成盘点</el-button>
              <el-button
                v-if="scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELLED'"
                size="small"
                type="info"
                @click="handleReport(scope.row)"
              >查看报告</el-button>
              <el-button
                v-if="scope.row.status === 'COMPLETED'"
                size="small"
                type="info"
                @click="handleReport(scope.row)"
              >查看报告</el-button>
              <el-button
                v-if="user.role === 'ADMIN' && (scope.row.status === 'PENDING' || scope.row.status === 'IN_PROGRESS')"
                size="small"
                type="danger"
                @click="handleCancel(scope.row)"
              >取消</el-button>
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
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务标题">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="盘点部门">
          <el-input v-model="form.departmentId" placeholder="请输入部门名称（留空则不限部门）" />
        </el-form-item>
        <el-form-item label="盘点仓库">
          <el-input v-model="form.location" placeholder="请输入仓库名称（留空则不限仓库）" />
        </el-form-item>
        <el-form-item label="执行人">
          <el-select
            v-model="form.assigneeId"
            placeholder="请选择执行人"
            filterable
            remote
            :remote-method="remoteSearchUser"
            :loading="userLoading"
            style="width: 100%"
          >
            <el-option
              v-for="u in userList"
              :key="u.id"
              :label="`${u.realName} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchForm = reactive({ status: '' })
const user = JSON.parse(localStorage.getItem('user') || '{}')

const dialogVisible = ref(false)
const form = reactive({
  title: '',
  departmentId: '',
  location: '',
  assigneeId: null
})

const userList = ref([])
const userLoading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchForm.status) params.status = searchForm.status
    if (user.role !== 'ADMIN') params.assigneeId = user.id
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

const getProgressStatus = (row) => {
  if (row.status === 'COMPLETED') return 'success'
  if (row.status === 'IN_PROGRESS') return ''
  return ''
}

const handleAdd = () => {
  Object.assign(form, { title: '', departmentId: '', location: '', assigneeId: null })
  dialogVisible.value = true
}

const remoteSearchUser = async (query) => {
  userLoading.value = true
  try {
    const res = await axios.get('/api/user/search', { params: { keyword: query } })
    if (res.data.code === 200) {
      userList.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to search users', error)
  } finally {
    userLoading.value = false
  }
}

const submitForm = async () => {
  if (!form.title) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!form.assigneeId) {
    ElMessage.warning('请选择执行人')
    return
  }
  try {
    const assignee = userList.value.find(u => u.id === form.assigneeId)
    const payload = {
      ...form,
      creatorId: user.id,
      creatorName: user.realName,
      assigneeName: assignee ? assignee.realName : ''
    }
    const res = await axios.post('/api/inventory/task/add', payload)
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
}

const handleCheck = (row) => {
  router.push({ path: '/inventory-check', query: { taskId: row.id } })
}

const handleComplete = (row) => {
  ElMessageBox.confirm('确认完成盘点？完成后将无法修改盘点结果。')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/task/complete/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('盘点已完成')
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

const handleReport = (row) => {
  router.push({ path: '/inventory-report', query: { taskId: row.id } })
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该盘点任务？')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/task/cancel/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('任务已取消')
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

onMounted(() => {
  fetchData()
  remoteSearchUser('')
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
