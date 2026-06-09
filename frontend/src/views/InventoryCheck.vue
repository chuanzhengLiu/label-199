<template>
  <div class="inventory-check">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>现场盘点 - {{ taskInfo.name }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form :inline="true" class="search-form">
        <el-form-item label="筛选">
          <el-input v-model="keyword" placeholder="资产编号/名称" clearable style="width: 240px" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="resultFilter" placeholder="全部" clearable style="width: 160px">
            <el-option label="未盘" value="UNCHECKED" />
            <el-option label="账实相符" value="MATCH" />
            <el-option label="仅状态不同" value="STATUS_DIFF" />
            <el-option label="仅位置不同" value="LOCATION_DIFF" />
            <el-option label="状态与位置均不同" value="BOTH_DIFF" />
            <el-option label="盘亏" value="LOSS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="goProfit">登记盘盈</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredDetails" style="width: 100%" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="名称" width="160" />
        <el-table-column label="账面状态" width="110">
          <template #default="scope">{{ getAssetStatusText(scope.row.bookStatus) }}</template>
        </el-table-column>
        <el-table-column prop="bookLocation" label="账面位置" width="140" />
        <el-table-column label="实物状态" width="160">
          <template #default="scope">
            <el-select v-model="scope.row.actualStatus" placeholder="选择实物状态">
              <el-option label="正常" value="NORMAL" />
              <el-option label="借出" value="BORROWED" />
              <el-option label="维修中" value="MAINTENANCE" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="实物位置" width="160">
          <template #default="scope">
            <el-input v-model="scope.row.actualLocation" placeholder="实物存放位置" />
          </template>
        </el-table-column>
        <el-table-column label="盘点结果" width="120">
          <template #default="scope">
            <el-tag :type="getResultType(scope.row.result)">{{ getResultText(scope.row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异备注" min-width="180">
          <template #default="scope">
            <el-input v-model="scope.row.remark" placeholder="差异说明" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="submitCheck(scope.row)">核对</el-button>
            <el-button size="small" type="danger" @click="markLoss(scope.row)">标记盘亏</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const taskId = ref(Number(route.params.id))
const taskInfo = ref({})
const detailList = ref([])
const loading = ref(false)
const keyword = ref('')
const resultFilter = ref('')

const filteredDetails = computed(() => {
  const kw = (keyword.value || '').trim()
  return detailList.value.filter(d => {
    const matchKw = !kw || (d.assetNo || '').includes(kw) || (d.assetName || '').includes(kw)
    const matchRes = !resultFilter.value || d.result === resultFilter.value
    return matchKw && matchRes
  })
})

const fetchTask = async () => {
  try {
    const res = await axios.get('/api/inventory/list', { params: { page: 1, size: 100 } })
    if (res.data.code === 200) {
      const t = res.data.data.records.find(r => r.id === taskId.value)
      if (t) taskInfo.value = t
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchDetails = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/inventory/detail/list', { params: { taskId: taskId.value } })
    if (res.data.code === 200) {
      detailList.value = res.data.data.map(d => ({
        ...d,
        actualStatus: d.actualStatus || d.bookStatus,
        actualLocation: d.actualLocation || d.bookLocation
      }))
    }
  } catch (e) {
    ElMessage.error('获取明细失败')
  } finally {
    loading.value = false
  }
}

const submitCheck = async (row) => {
  const payload = {
    id: row.id,
    actualStatus: row.actualStatus,
    actualLocation: row.actualLocation,
    remark: row.remark,
    checkerId: user.id,
    result: null
  }
  try {
    const res = await axios.post('/api/inventory/detail/check', payload)
    if (res.data.code === 200) {
      ElMessage.success('盘点已记录')
      await fetchDetails()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const markLoss = async (row) => {
  const payload = {
    id: row.id,
    actualStatus: row.actualStatus,
    actualLocation: row.actualLocation,
    remark: row.remark,
    checkerId: user.id,
    result: 'LOSS'
  }
  try {
    const res = await axios.post('/api/inventory/detail/check', payload)
    if (res.data.code === 200) {
      ElMessage.success('已标记盘亏')
      await fetchDetails()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goProfit = () => {
  router.push(`/inventory/profit/${taskId.value}`)
}

const goBack = () => {
  router.push('/inventory')
}

const getAssetStatusText = (status) => {
  const map = { 'NORMAL': '正常', 'BORROWED': '借出', 'MAINTENANCE': '维修中', 'SCRAPPED': '已报废' }
  return map[status] || status || '-'
}
const getResultType = (result) => {
  const map = {
    'MATCH': 'success',
    'STATUS_DIFF': 'warning',
    'LOCATION_DIFF': 'warning',
    'BOTH_DIFF': 'danger',
    'PROFIT': 'warning',
    'LOSS': 'danger',
    'UNCHECKED': 'info'
  }
  return map[result] || 'info'
}
const getResultText = (result) => {
  const map = {
    'MATCH': '账实相符',
    'STATUS_DIFF': '仅状态不同',
    'LOCATION_DIFF': '仅位置不同',
    'BOTH_DIFF': '状态与位置均不同',
    'PROFIT': '盘盈',
    'LOSS': '盘亏',
    'UNCHECKED': '未盘'
  }
  return map[result] || result
}

onMounted(() => {
  fetchTask()
  fetchDetails()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
