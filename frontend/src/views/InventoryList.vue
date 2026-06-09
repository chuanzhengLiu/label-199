<template>
  <div class="inventory-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资产盘点</span>
          <el-button v-if="user.role === 'ADMIN'" type="success" @click="handleCreate">新建盘点任务</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="执行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="taskNo" label="任务编号" width="180" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column label="盘点范围" width="180">
          <template #default="scope">
            {{ scope.row.scopeType === 'DEPARTMENT' ? '部门' : '仓库' }} / {{ scope.row.scopeValue }}
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="执行人" width="100" />
        <el-table-column prop="totalCount" label="应盘" width="80" />
        <el-table-column prop="checkedCount" label="已盘" width="80" />
        <el-table-column prop="profitCount" label="盘盈" width="80" />
        <el-table-column prop="lossCount" label="盘亏" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="360">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING' && canExecute(scope.row)" size="small" type="primary" @click="handleStart(scope.row)">开始</el-button>
            <el-button v-if="scope.row.status === 'IN_PROGRESS' && canExecute(scope.row)" size="small" type="primary" @click="goCheck(scope.row)">现场盘点</el-button>
            <el-button v-if="scope.row.status === 'IN_PROGRESS' && canExecute(scope.row)" size="small" type="success" @click="goProfit(scope.row)">登记盘盈</el-button>
            <el-button v-if="scope.row.status === 'IN_PROGRESS' && canExecute(scope.row)" size="small" type="warning" @click="handleFinish(scope.row)">完成</el-button>
            <el-button size="small" @click="goReport(scope.row)">差异报告</el-button>
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

    <!-- 创建盘点任务 -->
    <el-dialog v-model="createVisible" title="新建盘点任务" width="50%">
      <el-form :model="createForm" label-width="120px">
        <el-form-item label="任务名称">
          <el-input v-model="createForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="盘点范围">
          <el-select v-model="createForm.scopeType" style="width: 140px">
            <el-option label="按部门" value="DEPARTMENT" />
            <el-option label="按仓库" value="LOCATION" />
          </el-select>
          <el-input v-model="createForm.scopeValue" :placeholder="createForm.scopeType === 'DEPARTMENT' ? '请输入部门名称' : '请输入仓库/位置关键字'" style="width: calc(100% - 150px); margin-left: 10px;" />
        </el-form-item>
        <el-form-item label="执行人">
          <el-select
            v-model="createForm.assigneeId"
            placeholder="请输入姓名 or 用户名搜索"
            style="width: 100%"
            filterable
            remote
            :remote-method="remoteSearchUser"
            :loading="userLoading"
            @change="onAssigneeChange"
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
          <el-input v-model="createForm.remark" type="textarea" placeholder="任务说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
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
const user = JSON.parse(localStorage.getItem('user') || '{}')

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchForm = reactive({ status: '' })

const createVisible = ref(false)
const createForm = reactive({
  name: '',
  scopeType: 'DEPARTMENT',
  scopeValue: '',
  assigneeId: null,
  assigneeName: '',
  remark: ''
})

const userList = ref([])
const userLoading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (searchForm.status) params.status = searchForm.status
    if (user.role === 'EMPLOYEE') params.assigneeId = user.id
    const res = await axios.get('/api/inventory/list', { params })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) {
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
    const res = await axios.get('/api/user/search', { params: { keyword: query } })
    if (res.data.code === 200) userList.value = res.data.data
  } catch (e) {
    console.error(e)
  } finally {
    userLoading.value = false
  }
}

const onAssigneeChange = (id) => {
  const u = userList.value.find(x => x.id === id)
  createForm.assigneeName = u ? u.realName : ''
}

const handleCreate = () => {
  Object.assign(createForm, {
    name: '',
    scopeType: 'DEPARTMENT',
    scopeValue: '',
    assigneeId: null,
    assigneeName: '',
    remark: ''
  })
  remoteSearchUser('')
  createVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.name || !createForm.scopeValue || !createForm.assigneeId) {
    ElMessage.warning('请填写完整任务信息')
    return
  }
  try {
    const payload = {
      ...createForm,
      creatorId: user.id,
      creatorName: user.realName
    }
    const res = await axios.post('/api/inventory/create', payload)
    if (res.data.code === 200) {
      ElMessage.success('任务创建成功')
      createVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const canExecute = (row) => {
  return user.role === 'ADMIN' || row.assigneeId === user.id
}

const handleStart = async (row) => {
  try {
    const res = await axios.post(`/api/inventory/start/${row.id}`)
    if (res.data.code === 200) {
      ElMessage.success('盘点已开始')
      fetchData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleFinish = (row) => {
  ElMessageBox.confirm('确认完成本次盘点? 完成后将生成差异汇总。')
    .then(async () => {
      try {
        const res = await axios.post(`/api/inventory/finish/${row.id}`)
        if (res.data.code === 200) {
          ElMessage.success('盘点已完成')
          fetchData()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('操作失败')
      }
    })
    .catch(() => {})
}

const goCheck = (row) => {
  router.push(`/inventory/check/${row.id}`)
}

const goProfit = (row) => {
  router.push(`/inventory/profit/${row.id}`)
}

const goReport = (row) => {
  router.push(`/inventory/report/${row.id}`)
}

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'IN_PROGRESS': 'primary', 'COMPLETED': 'success' }
  return map[status] || 'info'
}
const getStatusText = (status) => {
  const map = { 'PENDING': '待执行', 'IN_PROGRESS': '执行中', 'COMPLETED': '已完成' }
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
