<template>
  <div class="inventory-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
            <span class="title">盘点差异报告</span>
          </div>
        </div>
      </template>

      <div v-if="reportData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ reportData.task.taskName }}</el-descriptions-item>
          <el-descriptions-item label="执行人">{{ reportData.task.executorName }}</el-descriptions-item>
          <el-descriptions-item label="盘点总数">{{ reportData.task.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="已盘点">{{ reportData.task.checkedCount }}</el-descriptions-item>
          <el-descriptions-item label="盘盈数量">
            <el-tag type="success">{{ reportData.statistics.profitCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="盘亏数量">
            <el-tag type="danger">{{ reportData.statistics.lossCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态差异">
            <el-tag type="warning">{{ reportData.statistics.statusDiffCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="位置差异">
            <el-tag type="warning">{{ reportData.statistics.locationDiffCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="双重差异">
            <el-tag type="danger">{{ reportData.statistics.bothDiffCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="正常">{{ reportData.statistics.normalCount }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ reportData.task.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ reportData.task.endTime }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="activeTab" style="margin-top: 20px">
          <el-tab-pane label="盘盈资产" name="profit">
            <el-table :data="profitList" style="width: 100%">
              <el-table-column prop="assetNo" label="资产编号" width="150" />
              <el-table-column prop="assetName" label="名称" width="150" />
              <el-table-column prop="actualStatus" label="实际状态" width="100">
                <template #default="scope">
                  {{ getAssetStatusText(scope.row.actualStatus) }}
                </template>
              </el-table-column>
              <el-table-column prop="actualLocation" label="实际位置" width="150" />
              <el-table-column prop="checkTime" label="盘点时间" width="160" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="盘亏资产" name="loss">
            <el-table :data="lossList" style="width: 100%">
              <el-table-column prop="assetNo" label="资产编号" width="150" />
              <el-table-column prop="assetName" label="名称" width="150" />
              <el-table-column prop="bookStatus" label="账面状态" width="100">
                <template #default="scope">
                  {{ getAssetStatusText(scope.row.bookStatus) }}
                </template>
              </el-table-column>
              <el-table-column prop="bookLocation" label="账面位置" width="150" />
              <el-table-column prop="checkTime" label="盘点时间" width="160" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="状态差异" name="statusDiff">
            <el-table :data="statusDiffList" style="width: 100%">
              <el-table-column prop="assetNo" label="资产编号" width="120" />
              <el-table-column prop="assetName" label="名称" width="120" />
              <el-table-column prop="bookStatus" label="账面状态" width="100" />
              <el-table-column prop="actualStatus" label="实际状态" width="100" />
              <el-table-column prop="bookLocation" label="账面位置" width="120" />
              <el-table-column prop="actualLocation" label="实际位置" width="120" />
              <el-table-column prop="checkTime" label="盘点时间" width="160" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="位置差异" name="locationDiff">
            <el-table :data="locationDiffList" style="width: 100%">
              <el-table-column prop="assetNo" label="资产编号" width="120" />
              <el-table-column prop="assetName" label="名称" width="120" />
              <el-table-column prop="bookStatus" label="账面状态" width="100" />
              <el-table-column prop="actualStatus" label="实际状态" width="100" />
              <el-table-column prop="bookLocation" label="账面位置" width="120" />
              <el-table-column prop="actualLocation" label="实际位置" width="120" />
              <el-table-column prop="checkTime" label="盘点时间" width="160" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="双重差异" name="bothDiff">
            <el-table :data="bothDiffList" style="width: 100%">
              <el-table-column prop="assetNo" label="资产编号" width="120" />
              <el-table-column prop="assetName" label="名称" width="120" />
              <el-table-column prop="bookStatus" label="账面状态" width="100" />
              <el-table-column prop="actualStatus" label="实际状态" width="100" />
              <el-table-column prop="bookLocation" label="账面位置" width="120" />
              <el-table-column prop="actualLocation" label="实际位置" width="120" />
              <el-table-column prop="checkTime" label="盘点时间" width="160" />
              <el-table-column prop="remark" label="备注" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const reportData = ref(null)
const activeTab = ref('statusDiff')

const profitList = computed(() => {
  if (!reportData.value) return []
  return reportData.value.details.filter(d => d.checkResult === 'PROFIT')
})

const lossList = computed(() => {
  if (!reportData.value) return []
  return reportData.value.details.filter(d => d.checkResult === 'LOSS')
})

const statusDiffList = computed(() => {
  if (!reportData.value) return []
  return reportData.value.details.filter(d => d.checkResult === 'STATUS_DIFF')
})

const locationDiffList = computed(() => {
  if (!reportData.value) return []
  return reportData.value.details.filter(d => d.checkResult === 'LOCATION_DIFF')
})

const bothDiffList = computed(() => {
  if (!reportData.value) return []
  return reportData.value.details.filter(d => d.checkResult === 'BOTH_DIFF')
})

const fetchReport = async () => {
  try {
    const res = await axios.get(`/api/inventory/report/${route.params.id}`)
    if (res.data.code === 200) {
      reportData.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取报告失败')
  }
}

const handleBack = () => {
  router.push('/inventory')
}

const getAssetStatusText = (status) => {
  const map = {
    'NORMAL': '正常',
    'BORROWED': '借出',
    'MAINTENANCE': '维修中',
    'SCRAPPED': '已报废'
  }
  return map[status] || status
}

onMounted(() => {
  fetchReport()
})
</script>

<style scoped>
.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}
.title {
  font-size: 16px;
  font-weight: bold;
}
</style>
