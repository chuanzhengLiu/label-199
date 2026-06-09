<template>
  <div class="borrow-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借用记录</span>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="userId" label="借用人" width="120">
             <template #default="scope">
                {{ getUserName(scope.row.userId) }}
             </template>
        </el-table-column>
        <el-table-column prop="borrowDate" label="借用日期" />
        <el-table-column prop="actualReturnDate" label="归还日期" />
        <el-table-column prop="status" label="状态">
           <template #default="scope">
              <el-tag :type="scope.row.status === 'BORROWED' ? 'warning' : 'success'">{{ scope.row.status === 'BORROWED' ? '借出' : '已归还' }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="remarks" label="备注" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'BORROWED' && user.role === 'ADMIN'" size="small" type="primary" @click="handleReturn(scope.row)">归还</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const userList = ref([])
const user = JSON.parse(localStorage.getItem('user') || '{}')

const fetchDictionaries = async () => {
    try {
        const userRes = await axios.get('/api/user/list')
        if (userRes.data.code === 200) {
            userList.value = userRes.data.data
        }
    } catch (error) {
        console.error('Failed to load dictionaries', error)
    }
}

const getUserName = (id) => {
    const user = userList.value.find(u => u.id === id)
    return user ? user.realName : id
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (user.role === 'EMPLOYEE') {
        params.userId = user.id
    }
    const res = await axios.get('/api/borrow/list', { params })
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

const handleReturn = (row) => {
    ElMessageBox.confirm('确认归还该资产?')
        .then(async () => {
            const res = await axios.post(`/api/borrow/return/${row.id}`)
            if (res.data.code === 200) {
                ElMessage.success('归还成功')
                fetchData()
            } else {
                ElMessage.error(res.data.message)
            }
        })
        .catch(() => {})
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
</style>
