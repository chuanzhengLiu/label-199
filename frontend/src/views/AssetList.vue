<template>
  <div class="asset-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资产列表</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="资产名称" />
        </el-form-item>
        <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 150px">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
        </el-form-item>
        <el-form-item label="状态">
             <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
                 <el-option label="正常" value="NORMAL" />
                 <el-option label="借出" value="BORROWED" />
                 <el-option label="维修中" value="MAINTENANCE" />
                 <el-option label="已报废" value="SCRAPPED" />
             </el-select>
        </el-form-item>
        <el-form-item label="部门">
             <el-input v-model="searchForm.departmentId" placeholder="部门名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button v-if="user.role === 'ADMIN'" type="success" @click="handleAdd">新增资产</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="120" />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="serialNumber" label="序列号" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120">
             <template #default="scope">
                {{ getCategoryName(scope.row.categoryId) }}
             </template>
        </el-table-column>
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="departmentId" label="所属部门" width="120" />
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="location" label="存放地点" />
        <el-table-column prop="purchaseDate" label="采购日期" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button-group>
              <el-button v-if="user.role === 'ADMIN'" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button v-if="scope.row.status === 'NORMAL' && (user.role === 'ADMIN' || user.role === 'EMPLOYEE')" size="small" type="success" @click="handleBorrow(scope.row)">借用</el-button>
              <el-button v-if="scope.row.status === 'NORMAL' && (user.role === 'ADMIN' || user.role === 'EMPLOYEE')" size="small" type="warning" @click="handleMaintain(scope.row)">维修</el-button>
              <el-button v-if="scope.row.status === 'NORMAL' && user.role === 'ADMIN'" size="small" type="danger" @click="handleScrap(scope.row)">报废</el-button>
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

    <!-- Asset Form Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑资产' : '新增资产'" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="资产编号">
          <el-input v-model="form.assetNo" placeholder="请输入资产编号" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                    v-for="item in categoryList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
            </el-select>
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="form.serialNumber" placeholder="请输入序列号" />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.departmentId" placeholder="请输入所属部门" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="使用年限(月)">
          <el-input-number v-model="form.usefulLife" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="存放地点">
          <el-input v-model="form.location" placeholder="请输入存放地点" />
        </el-form-item>
        <el-form-item label="采购日期">
             <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Borrow Dialog -->
    <el-dialog v-model="borrowVisible" title="借用资产" width="40%">
      <el-form :model="borrowForm" label-width="100px">
        <el-form-item label="借用人">
            <el-select 
                v-model="borrowForm.userId" 
                placeholder="请输入姓名 or 用户名搜索" 
                style="width: 100%" 
                filterable 
                remote
                :remote-method="remoteSearchUser"
                :loading="userLoading"
                :disabled="user.role === 'EMPLOYEE'"
            >
                <el-option
                    v-for="user in userList"
                    :key="user.id"
                    :label="`${user.realName} (${user.username})`"
                    :value="user.id"
                />
            </el-select>
        </el-form-item>
        <el-form-item label="预计归还">
             <el-date-picker 
                v-model="borrowForm.returnDate" 
                type="datetime" 
                placeholder="选择日期时间" 
                style="width: 100%" 
                value-format="YYYY-MM-DD HH:mm:ss" 
             />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="borrowForm.remarks" type="textarea" placeholder="请输入借用用途或备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">确定</el-button>
      </template>
    </el-dialog>

    <!-- Maintenance Dialog -->
    <el-dialog v-model="maintainVisible" title="报修资产" width="40%">
      <el-form :model="maintainForm" label-width="100px">
        <el-form-item label="故障描述">
          <el-input v-model="maintainForm.description" type="textarea" placeholder="请详细描述故障情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="maintainVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMaintain">确定</el-button>
      </template>
    </el-dialog>

    <!-- Scrap Dialog -->
    <el-dialog v-model="scrapVisible" title="申请报废" width="40%">
      <el-form :model="scrapForm" label-width="100px">
        <el-form-item label="报废原因">
          <el-input v-model="scrapForm.reason" type="textarea" placeholder="请输入报废原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scrapVisible = false">取消</el-button>
        <el-button type="primary" @click="submitScrap">确定</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const searchForm = reactive({ name: '', categoryId: null, status: '', departmentId: '' })
const user = JSON.parse(localStorage.getItem('user') || '{}')

// Dictionaries
const categoryList = ref([])
const userList = ref([])
const userLoading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
    id: null,
    assetNo: '',
    name: '',
    categoryId: null,
    model: '',
    serialNumber: '',
    departmentId: '',
    supplier: '',
    price: 0,
    usefulLife: null,
    location: '',
    purchaseDate: ''
})

const borrowVisible = ref(false)
const borrowForm = reactive({ assetId: null, userId: null, returnDate: null, remarks: '' })

const maintainVisible = ref(false)
const maintainForm = reactive({ assetId: null, description: '' })

const scrapVisible = ref(false)
const scrapForm = reactive({ assetId: null, reason: '' })

const fetchDictionaries = async () => {
    try {
        const catRes = await axios.get('/api/category/list')
        if (catRes.data.code === 200) {
            categoryList.value = catRes.data.data
        }
        // Initial load of users (top 50)
        remoteSearchUser('')
    } catch (error) {
        console.error('Failed to load dictionaries', error)
    }
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

const getCategoryName = (id) => {
    const cat = categoryList.value.find(c => c.id === id)
    return cat ? cat.name : id
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/asset/list', {
      params: { page: currentPage.value, size: pageSize.value, name: searchForm.name }
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
        'NORMAL': 'success',
        'BORROWED': 'warning',
        'MAINTENANCE': 'danger',
        'SCRAPPED': 'info'
    }
    return map[status] || 'info'
}

const getStatusText = (status) => {
    const map = {
        'NORMAL': '正常',
        'BORROWED': '借出',
        'MAINTENANCE': '维修中',
        'SCRAPPED': '已报废'
    }
    return map[status] || status
}

const handleAdd = () => {
    isEdit.value = false
    Object.assign(form, { id: null, assetNo: '', name: '', categoryId: null, model: '', price: 0, location: '', purchaseDate: '' })
    dialogVisible.value = true
}

const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(form, row)
    dialogVisible.value = true
}

const submitForm = async () => {
    if(!form.categoryId) {
         ElMessage.warning('请选择分类')
         return
    }
    try {
        const url = isEdit.value ? '/api/asset/update' : '/api/asset/add'
        const res = await axios.post(url, form)
        if (res.data.code === 200) {
            ElMessage.success('操作成功')
            dialogVisible.value = false
            fetchData()
        } else {
            ElMessage.error(res.data.message)
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

const handleDelete = (row) => {
    ElMessageBox.confirm('确认删除该资产?')
        .then(async () => {
            try {
                const res = await axios.delete(`/api/asset/${row.id}`)
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

// Borrow Logic
const handleBorrow = (row) => {
    borrowForm.assetId = row.id
    if (user.role === 'EMPLOYEE') {
        borrowForm.userId = user.id
    } else {
        borrowForm.userId = null
    }
    borrowForm.returnDate = null
    borrowForm.remarks = ''
    borrowVisible.value = true
}

const submitBorrow = async () => {
    if (!borrowForm.userId) {
        ElMessage.warning('请选择借用人')
        return
    }
    try {
        const res = await axios.post('/api/borrow/apply', borrowForm)
        if (res.data.code === 200) {
            ElMessage.success('借用申请已提交')
            borrowVisible.value = false
            fetchData()
        } else {
            ElMessage.error(res.data.message)
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

// Maintenance Logic
const handleMaintain = (row) => {
    maintainForm.assetId = row.id
    maintainForm.description = ''
    maintainVisible.value = true
}

const submitMaintain = async () => {
    if (!maintainForm.description) {
        ElMessage.warning('请输入故障描述')
        return
    }
    try {
        const res = await axios.post('/api/maintenance/report', maintainForm)
        if (res.data.code === 200) {
            ElMessage.success('报修成功')
            maintainVisible.value = false
            fetchData()
        } else {
            ElMessage.error(res.data.message)
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

// Scrap Logic
const handleScrap = (row) => {
    scrapForm.assetId = row.id
    scrapForm.reason = ''
    scrapVisible.value = true
}

const submitScrap = async () => {
    if (!scrapForm.reason) {
        ElMessage.warning('请输入报废原因')
        return
    }
    try {
        const res = await axios.post('/api/scrap/apply', scrapForm)
        if (res.data.code === 200) {
            ElMessage.success('报废申请已提交')
            scrapVisible.value = false
            fetchData()
        } else {
            ElMessage.error(res.data.message)
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
}

onMounted(() => {
  fetchDictionaries()
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
