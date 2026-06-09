<template>
  <div class="inventory-profit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>登记盘盈 - 任务 #{{ taskId }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width: 600px;">
        <el-form-item label="资产编号">
          <el-input v-model="form.assetNo" placeholder="实物上的编号（可选）" />
        </el-form-item>
        <el-form-item label="资产名称">
          <el-input v-model="form.assetName" placeholder="资产名称" />
        </el-form-item>
        <el-form-item label="实物状态">
          <el-select v-model="form.actualStatus" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="借出" value="BORROWED" />
            <el-option label="维修中" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="form.actualLocation" placeholder="存放位置" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="差异说明" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitProfit">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-divider>已登记盘盈</el-divider>
      <el-table :data="profitList" style="width: 100%" v-loading="loading">
        <el-table-column prop="assetNo" label="资产编号" width="120" />
        <el-table-column prop="assetName" label="名称" width="160" />
        <el-table-column label="实物状态" width="120">
          <template #default="scope">{{ getAssetStatusText(scope.row.actualStatus) }}</template>
        </el-table-column>
        <el-table-column prop="actualLocation" label="存放位置" width="160" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="checkTime" label="登记时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user') || '{}')

const taskId = ref(Number(route.params.id))
const loading = ref(false)
const profitList = ref([])

const form = reactive({
  taskId: taskId.value,
  assetNo: '',
  assetName: '',
  actualStatus: 'NORMAL',
  actualLocation: '',
  remark: '',
  checkerId: user.id
})

const fetchProfits = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/inventory/detail/list', { params: { taskId: taskId.value } })
    if (res.data.code === 200) {
      profitList.value = res.data.data.filter(d => d.result === 'PROFIT')
    }
  } catch (e) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    taskId: taskId.value,
    assetNo: '',
    assetName: '',
    actualStatus: 'NORMAL',
    actualLocation: '',
    remark: '',
    checkerId: user.id
  })
}

const submitProfit = async () => {
  if (!form.assetName) {
    ElMessage.warning('请输入资产名称')
    return
  }
  try {
    const res = await axios.post('/api/inventory/detail/profit', form)
    if (res.data.code === 200) {
      ElMessage.success('盘盈已登记')
      resetForm()
      fetchProfits()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const goBack = () => {
  router.push('/inventory')
}

const getAssetStatusText = (status) => {
  const map = { 'NORMAL': '正常', 'BORROWED': '借出', 'MAINTENANCE': '维修中', 'SCRAPPED': '已报废' }
  return map[status] || status || '-'
}

onMounted(() => {
  fetchProfits()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
