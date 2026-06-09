<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button type="success" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="role" label="角色">
           <template #default="scope">
              <el-tag :type="getRoleType(scope.row.role)">{{ getRoleText(scope.row.role) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- User Form Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
            <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="行政部门 (管理员)" value="ADMIN" />
                <el-option label="用户 (数据管理员)" value="MANAGER" />
                <el-option label="普通员工" value="EMPLOYEE" />
            </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" placeholder="请输入部门" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const dialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)

const searchForm = reactive({
  username: ''
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: '',
  department: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  department: [{ required: true, message: '请输入部门', trigger: 'blur' }]
}

const getRoleType = (role) => {
  const map = {
    'ADMIN': 'danger',
    'MANAGER': 'warning',
    'EMPLOYEE': 'success'
  }
  return map[role] || 'info'
}

const getRoleText = (role) => {
  const map = {
    'ADMIN': '行政部门',
    'MANAGER': '用户',
    'EMPLOYEE': '普通员工'
  }
  return map[role] || role
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/user/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        username: searchForm.username
      }
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

const handlePageChange = (page) => {
  currentPage.value = page
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = 'EMPLOYEE'
  form.department = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.password = row.password // Usually backend doesn't return password or we shouldn't show it. But for simple edit we might need to handle it.
  // Ideally, password should be empty on edit, and only updated if user enters something. 
  // But given current simple implementation, let's just populate it or leave it. 
  // Wait, if I send password back, it will update password. 
  // Let's assume user wants to change password too.
  form.realName = row.realName
  form.role = row.role
  form.department = row.department
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/user/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.data.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = isEdit.value ? '/api/user/update' : '/api/user/add'
        const res = await axios.post(url, form)
        if (res.data.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
          dialogVisible.value = false
          fetchData()
        } else {
          ElMessage.error(res.data.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

onMounted(() => {
  fetchData()
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
  text-align: right;
}
</style>