<template>
  <div class="scrap-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报废记录</span>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="assetId" label="资产ID" width="100" />
        <el-table-column prop="reason" label="报废原因" />
        <el-table-column prop="scrapDate" label="报废日期" />
        <el-table-column prop="status" label="状态">
           <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <div v-if="scope.row.status === 'PENDING'">
                <el-button size="small" type="success" @click="handleApprove(scope.row, true)">同意</el-button>
                <el-button size="small" type="danger" @click="handleApprove(scope.row, false)">拒绝</el-button>
            </div>
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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/scrap/list', {
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

const getStatusType = (status) => {
    const map = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
    }
    return map[status] || 'info'
}

const getStatusText = (status) => {
    const map = {
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝'
    }
    return map[status] || status
}

const handleApprove = (row, approved) => {
    ElMessageBox.confirm(`确认要${approved ? '同意' : '拒绝'}该申请吗?`)
        .then(async () => {
            const res = await axios.post(`/api/scrap/approve/${row.id}?approved=${approved}`)
            if (res.data.code === 200) {
                ElMessage.success('操作成功')
                fetchData()
            } else {
                ElMessage.error(res.data.message)
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
</style>
