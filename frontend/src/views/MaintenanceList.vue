<template>
  <div class="maintenance-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修记录</span>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="description" label="故障描述" />
        <el-table-column prop="cost" label="费用" />
        <el-table-column prop="vendor" label="维修商" />
        <el-table-column prop="status" label="状态">
           <template #default="scope">
              <el-tag :type="scope.row.status === 'PENDING' ? 'warning' : 'success'">{{ scope.row.status === 'PENDING' ? '维修中' : '已完成' }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING' && user.role === 'ADMIN'" size="small" type="primary" @click="handleComplete(scope.row)">完成维修</el-button>
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

    <el-dialog v-model="dialogVisible" title="完成维修登记" width="40%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="费用">
          <el-input-number v-model="form.cost" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="维修商">
          <el-input v-model="form.vendor" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const dialogVisible = ref(false)
const form = reactive({ id: null, cost: 0, vendor: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/maintenance/list', {
      params: { page: currentPage.value, size: pageSize.value }
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

const handleComplete = (row) => {
    form.id = row.id
    form.cost = 0
    form.vendor = ''
    dialogVisible.value = true
}

const submitComplete = async () => {
    try {
        const res = await axios.post('/api/maintenance/complete', form)
        if (res.data.code === 200) {
            ElMessage.success('维修已完成')
            dialogVisible.value = false
            fetchData()
        } else {
            ElMessage.error(res.data.message)
        }
    } catch (error) {
        ElMessage.error('操作失败')
    }
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
</style>
