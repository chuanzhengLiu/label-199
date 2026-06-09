<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>盘点任务列表</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待开始" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button v-if="user.role === 'ADMIN'" type="success" @click="handleCreate">新建盘点任务</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="taskNo" label="任务编号" width="200" />
        <el-table-column prop="taskName" label="任务名称" width="180" />
        <el-table-column prop="scopeType" label="盘点范围" width="120">
          <template #default="scope">
            <span>{{ getScopeTypeText(scope.row.scopeType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scopeValue" label="范围值" width="120" />
        <el-table-column prop="assigneeName" label="执行人" width="100" />
        <el-table-column prop="createName" label="创建人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="盘点进度" width="180">
          <template #default="scope">
            <el-progress :percentage="scope.row.totalCount > 0 ? Math.round(scope.row.checkedCount / scope.row.totalCount * 100) : 0" 
              :status="scope.row.status === 'COMPLETED' ? 'success' : ''" />
            <span class="progress-text">{{ scope.row.checkedCount }}/{{ scope.row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" type="primary" @click="goExecute(scope.row)" 
                :disabled="scope.row.status === 'COMPLETED'">开始盘点</el-button>
              <el-button size="small" type="success" @click="goReport(scope.row)">查看报告</el-button>
              <el-button v-if="scope.row.status === 'PENDING' && user.role === 'ADMIN'" size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新建盘点任务" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="盘点范围">
          <el-select v-model="form.scopeType" placeholder="请选择盘点范围" style="width: 100%" @change="onScopeTypeChange">
            <el-option label="按部门" value="DEPARTMENT" />
            <el-option label="按仓库/地点" value="WAREHOUSE" />
            <el-option label="全部资产" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.scopeType !== 'ALL'" label="范围值">
          <el-input v-if="form.scopeType === 'DEPARTMENT'" v-model="form.scopeValue" placeholder="请输入部门名称，如：技术部" />
          <el-input v-if="form.scopeType === 'WAREHOUSE'" v-model="form.scopeValue" placeholder="请输入存放地点关键词，如：A仓库" />
        </el-form-item>
        <el-form-item label="执行人">
          <el-select v-model="form.assigneeId" placeholder="请选择执行人" style="width: 100%" 
            filterable remote :remote-method="remoteSearchUser" :loading="userLoading">
            <el-option v-for="u in userList" :key="u.id" :label="`${u.realName} (${u.department})`" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" type="textarea" placeholder="请输入盘点任务备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定创建</el-button>
        </span>
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
  taskName: '',
  scopeType: 'ALL',
  scopeValue: '',
  assigneeId: null,
  remarks: ''
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

const getScopeTypeText = (type) => {
  const map = { 'DEPARTMENT': '按部门', 'WAREHOUSE': '按地点', 'ALL': '全部资产' }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = { 'PENDING': 'info', 'IN_PROGRESS': 'warning', 'COMPLETED': 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待开始', 'IN_PROGRESS': '进行中', 'COMPLETED': '已完成' }
  return map[status] || status
}

const remoteSearchUser = async (query) => {
  userLoading.value = true
  try {
    const res = await axios.get('/api/user/search', { params: { keyword: query || '' } })
    if (res.data.code === 200) {
      userList.value = res.data.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    userLoading.value = false
  }
}

const onScopeTypeChange = () => {
  form.scopeValue = ''
}

const handleCreate = () => {
  Object.assign(form, { taskName: '', scopeType: 'ALL', scopeValue: '', assigneeId: null, remarks: '' })
  remoteSearchUser('')
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.taskName) { ElMessage.warning('请输入任务名称'); return }
  if (!form.assigneeId) { ElMessage.warning('请选择执行人'); return }
  if (form.scopeType !== 'ALL' && !form.scopeValue) { ElMessage.warning('请输入盘点范围值'); return }
  
  const selectedUser = userList.value.find(u => u.id === form.assigneeId)
  try {
    const res = await axios.post('/api/inventory/task/create', {
      ...form,
      assigneeName: selectedUser ? selectedUser.realName : '',
      createBy: user.id,
      createName: user.realName
    })
    if (res.data.code === 200) {
      ElMessage.success('盘点任务创建成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const goExecute = async (row) => {
  if (row.status === 'PENDING') {
    try {
      await axios.post(`/api/inventory/task/start/${row.id}`)
    } catch (e) {}
  }
  router.push(`/inventory/execute/${row.id}`)
}

const goReport = (row) => {
  router.push(`/inventory/report/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该盘点任务？删除后明细数据也将清除。')
    .then(async () => {
      try {
        const res = await axios.delete(`/api/inventory/task/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          fetchData()
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
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
.progress-text {
  font-size: 12px;
  color: #666;
  margin-left: 8px;
}
</style>
