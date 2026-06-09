<template>
  <div class="statistics">
    <div class="page-header">
        <div class="header-left">
            <h2>数据统计大屏</h2>
            <span class="subtitle">实时监控资产状态与库存情况</span>
        </div>
        <el-button type="primary" icon="Download" @click="exportReport" v-if="user.role === 'ADMIN' || user.role === 'MANAGER'">
            导出分析报表
        </el-button>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="(item, index) in dashboardData" :key="index">
        <el-card shadow="hover" class="dashboard-card" :body-style="{ padding: '20px' }">
          <div class="card-icon" :style="{ background: item.bgColor }">
             <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">{{ item.title }}</div>
            <div class="card-number" :style="{ color: item.color }">
                <span class="num">{{ item.value }}</span>
                <span class="unit">件</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 分类分布饼图 -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="header-title">资产分类分布</span>
            </div>
          </template>
          <div ref="categoryChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
      
      <!-- 状态统计柱状图 -->
      <el-col :span="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="header-title">资产状态统计</span>
            </div>
          </template>
          <div ref="statusChartRef" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row mt-4">
      <!-- 库存预警 -->
      <el-col :span="24">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header warning-header">
              <span class="header-title">库存预警监控 (可用 < 5)</span>
              <el-tag type="danger" effect="dark" round>{{ lowStockList.length }} 项异常</el-tag>
            </div>
          </template>
          <el-table :data="lowStockList" style="width: 100%" :header-cell-style="{ background: '#f5f7fa' }">
            <el-table-column prop="categoryName" label="资产类别">
                <template #default="scope">
                    <span style="font-weight: 500;">{{ scope.row.categoryName }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="count" label="当前可用库存" align="center">
                <template #default="scope">
                    <span style="color: #F56C6C; font-weight: bold; font-size: 16px;">{{ scope.row.count }}</span>
                </template>
            </el-table-column>
            <el-table-column label="状态" align="center">
                <template #default>
                    <el-tag type="danger" effect="plain">库存不足</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="建议操作" align="center">
                <template #default="scope">
                    <el-button type="primary" link icon="Plus" @click="handlePurchase(scope.row)">立即采购</el-button>
                </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- Purchase Dialog -->
    <el-dialog v-model="purchaseVisible" title="采购申请" width="30%">
      <el-form :model="purchaseForm" label-width="100px">
        <el-form-item label="资产类别">
          <el-input v-model="purchaseForm.categoryName" disabled />
        </el-form-item>
        <el-form-item label="采购数量">
          <el-input-number v-model="purchaseForm.quantity" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="purchaseForm.remarks" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="purchaseVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPurchase">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Box, Timer, Tools, Delete, Download, Plus } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const dashboardData = ref([
    { title: '资产总数', value: 0, color: '#409EFF', bgColor: '#ecf5ff', icon: 'Box' },
    { title: '借出资产', value: 0, color: '#E6A23C', bgColor: '#fdf6ec', icon: 'Timer' },
    { title: '维修中', value: 0, color: '#F56C6C', bgColor: '#fef0f0', icon: 'Tools' },
    { title: '已报废', value: 0, color: '#909399', bgColor: '#f4f4f5', icon: 'Delete' }
])

const lowStockList = ref([])
const categoryDist = ref([])
const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8e44ad', '#1abc9c', '#d35400']

// Purchase Logic
const purchaseVisible = ref(false)
const purchaseForm = reactive({
    categoryName: '',
    quantity: 1,
    remarks: ''
})

const handlePurchase = (row) => {
    purchaseForm.categoryName = row.categoryName
    purchaseForm.quantity = 1
    purchaseForm.remarks = ''
    purchaseVisible.value = true
}

const submitPurchase = async () => {
    try {
        const res = await axios.post('/api/purchase/apply', purchaseForm)
        if (res.data.code === 200) {
             ElMessage.success('采购申请已提交')
             purchaseVisible.value = false
        } else {
             ElMessage.error(res.data.msg || '提交失败')
        }
    } catch (error) {
        ElMessage.error('提交失败')
    }
}

// Chart Refs
const categoryChartRef = ref(null)
const statusChartRef = ref(null)
let categoryChart = null
let statusChart = null

const initCharts = () => {
    if (categoryChartRef.value) {
        categoryChart = echarts.init(categoryChartRef.value)
    }
    if (statusChartRef.value) {
        statusChart = echarts.init(statusChartRef.value)
    }
}

const updateCategoryChart = () => {
    if (!categoryChart) return
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)'
        },
        legend: {
            orient: 'vertical',
            left: 'left',
            type: 'scroll'
        },
        series: [
            {
                name: '资产分类',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: 20,
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: categoryDist.value.map(item => ({
                    value: item.count,
                    name: item.categoryName
                }))
            }
        ]
    }
    categoryChart.setOption(option)
}

const updateStatusChart = () => {
    if (!statusChart) return
    // Remove "Total" from comparison as it dwarfs others usually, or keep it. 
    // Let's show Breakdown: Available (Total - Borrowed - Maintenance - Scrapped) vs Borrowed vs Maintenance vs Scrapped
    // But we don't have "Available" directly, we can calculate.
    // Dashboard data: 0: Total, 1: Borrowed, 2: Maintenance, 3: Scrapped
    const total = dashboardData.value[0].value
    const borrowed = dashboardData.value[1].value
    const maintenance = dashboardData.value[2].value
    const scrapped = dashboardData.value[3].value
    const available = total - borrowed - maintenance - scrapped // Approximate

    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                data: ['在库可用', '已借出', '维修中', '已报废'],
                axisTick: { alignWithLabel: true }
            }
        ],
        yAxis: [
            {
                type: 'value'
            }
        ],
        series: [
            {
                name: '数量',
                type: 'bar',
                barWidth: '60%',
                data: [
                    { value: available > 0 ? available : 0, itemStyle: { color: '#67C23A' } },
                    { value: borrowed, itemStyle: { color: '#E6A23C' } },
                    { value: maintenance, itemStyle: { color: '#F56C6C' } },
                    { value: scrapped, itemStyle: { color: '#909399' } }
                ]
            }
        ]
    }
    statusChart.setOption(option)
}

const fetchDashboardStats = async () => {
    try {
        const res = await axios.get('/api/stats/dashboard')
        if (res.data.code === 200) {
            const data = res.data.data
            dashboardData.value[0].value = data.totalAssets
            dashboardData.value[1].value = data.borrowedAssets
            dashboardData.value[2].value = data.maintenanceAssets
            dashboardData.value[3].value = data.scrappedAssets
            updateStatusChart()
        }
    } catch (error) {
        console.error(error)
    }
}

const fetchLowStockStats = async () => {
    try {
        const res = await axios.get('/api/stats/low-stock')
        if (res.data.code === 200) {
            lowStockList.value = res.data.data
        }
    } catch (error) {
        console.error(error)
    }
}

const fetchCategoryDistribution = async () => {
    try {
        const res = await axios.get('/api/stats/category-distribution')
        if (res.data.code === 200) {
            categoryDist.value = res.data.data
            updateCategoryChart()
        }
    } catch (error) {
        console.error(error)
    }
}

const exportReport = () => {
    let csvContent = "data:text/csv;charset=utf-8,\uFEFF";
    csvContent += "统计指标,数值\n";
    dashboardData.value.forEach(item => {
        csvContent += `${item.title},${item.value}\n`;
    });
    
    csvContent += "\n分类名称,数量\n";
    categoryDist.value.forEach(item => {
        csvContent += `${item.categoryName},${item.count}\n`;
    });
    
    csvContent += "\n预警类别,可用数量\n";
    lowStockList.value.forEach(item => {
        csvContent += `${item.categoryName},${item.count}\n`;
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "asset_report.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    ElMessage.success('报表已导出');
}

const handleResize = () => {
    categoryChart && categoryChart.resize()
    statusChart && statusChart.resize()
}

onMounted(async () => {
    initCharts()
    await Promise.all([
        fetchDashboardStats(),
        fetchLowStockStats(),
        fetchCategoryDistribution()
    ])
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    categoryChart && categoryChart.dispose()
    statusChart && statusChart.dispose()
})
</script>

<style scoped>
.statistics {
    padding-bottom: 20px;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.header-left h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
}
.subtitle {
    color: #909399;
    font-size: 14px;
    margin-top: 5px;
    display: block;
}

.dashboard-card {
    border: none;
    transition: all 0.3s;
}
.dashboard-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}
.card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 15px;
}
.card-content {
    text-align: left;
}
.card-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
}
.card-number {
    font-size: 28px;
    font-weight: bold;
    display: flex;
    align-items: baseline;
}
.unit {
    font-size: 14px;
    margin-left: 5px;
    font-weight: normal;
    color: #909399;
}

.chart-card {
    border-radius: 8px;
    border: 1px solid #EBEEF5;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.header-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}
.warning-header {
    display: flex;
    align-items: center;
    gap: 10px;
}
.mb-4 {
    margin-bottom: 20px;
}
.mt-4 {
    margin-top: 20px;
}
</style>
