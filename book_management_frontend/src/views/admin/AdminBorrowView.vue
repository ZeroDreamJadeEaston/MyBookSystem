<script setup>
import {
  getAllBorrowService,
  deleteBorrowBatchService,
  deleteByIdService,
  returnBookService,
  updateDueDateService 
} from "@/methods/borrow.js";
import { onMounted, ref } from "vue";
import HeaderView from "@/components/HeaderView.vue";
import SideView from "@/components/SideView.vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit } from "@element-plus/icons-vue"; // 引入 Edit 图标

const tableData = ref([]);
const filterStatus = ref('all');
const ids = ref([]);
const loading = ref(false);

// 控制修改日期的弹窗
const dialogVisible = ref(false);
const newDueDate = ref('');
const currentBorrowId = ref(null);

onMounted(() => {
  loadData();
});

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const result = await getAllBorrowService();
    const allData = Array.isArray(result.data) ? result.data : [];
    
    // 筛选逻辑
    let filteredData = allData;
    if (filterStatus.value === 'returned') {
      filteredData = allData.filter(item => item.status === true);
    } else if (filterStatus.value === 'notReturned') {
      filteredData = allData.filter(item => item.status === false);
    }
    tableData.value = filteredData;
  } catch (error) {
    console.error(error);
    ElMessage.error("数据加载失败");
  } finally {
    loading.value = false;
  }
};

const flushStatus = (status) => {
  filterStatus.value = status;
  loadData();
};

// 代还书
const returnBook = async (id, isbn) => {
  await returnBookService(id, isbn);
  ElMessage.success('操作成功：已归还！');
  await loadData();
};

// 删除单条
const deleteById = (id) => {
  ElMessageBox.confirm('确定要删除这条借阅记录吗？', '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteByIdService(id);
    ElMessage.success('删除成功！');
    await loadData();
  }).catch(() => {});
};

// 批量删除
const handleSelectionChange = (val) => {
  ids.value = val.map(item => item.id);
};

const deleteBorrowBatch = () => {
  if (ids.value.length === 0) {
    ElMessage.warning('请先勾选要删除的记录');
    return;
  }
  ElMessageBox.confirm(`确定要批量删除这 ${ids.value.length} 条记录吗？`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteBorrowBatchService(ids.value);
    ElMessage.success('批量删除成功！');
    await loadData();
  }).catch(() => {});
};

// 打开修改日期的弹窗
const openDateDialog = (row) => {
  currentBorrowId.value = row.id;
  newDueDate.value = row.dueDate; // 默认回显当前日期
  dialogVisible.value = true;
};

// 提交日期修改
const submitDateChange = async () => {
  if (!newDueDate.value) {
    ElMessage.warning("请选择日期");
    return;
  }
  try {
    // 调用后端接口
    await updateDueDateService(currentBorrowId.value, newDueDate.value);
    ElMessage.success("修改期限成功！");
    dialogVisible.value = false; // 关闭弹窗
    await loadData(); // 刷新表格
  } catch (error) {
    console.error(error);
    // 错误处理通常在 request 拦截器里有，可省略或手动提示
  }
};

// 行样式逻辑
const tableRowClassName = ({ row }) => {
  const current = new Date().toISOString().split('T')[0];
  if (row.status) {
    return 'success-row';
  } else {
    return current > row.dueDate ? 'danger-row' : 'warning-row';
  }
};
</script>

<template>
  <div>
    <el-container>
      <el-aside width="220px" style="background-color: transparent;">
        <side-view/>
      </el-aside>
      
      <el-container>
        <el-header style="height: auto; padding: 0;">
          <header-view/>
        </el-header>
        
        <el-main>
          <div style="display: flex; justify-content: space-between; width: 80%; margin: 0 auto 10px auto;">
            <el-button-group>
              <el-button @click="flushStatus('all')" :type="filterStatus === 'all' ? 'primary' : ''">全部</el-button>
              <el-button @click="flushStatus('returned')" type="success" plain>已归还</el-button>
              <el-button @click="flushStatus('notReturned')" type="danger" plain>未归还</el-button>
            </el-button-group>
            
            <el-button type="danger" :icon="Delete" @click="deleteBorrowBatch" plain>批量删除</el-button>
          </div>
          
          <el-row>
            <el-col :span="24">
              <el-table 
                v-loading="loading"
                ref="table" 
                :data="tableData" 
                style="width: 100%" 
                height="550"
                :row-class-name="tableRowClassName"
                @selection-change="handleSelectionChange"
              >
                <el-table-column type="selection" width="55" align="center"/>
                
                <el-table-column prop="username" label="借阅人" width="120" sortable>
                   <template #default="scope">
                      <span style="font-weight: bold;">{{ scope.row.username }}</span>
                      <span v-if="scope.row.nickname" style="font-size: 12px; color: #666; margin-left: 5px;">
                        ({{ scope.row.nickname }})
                      </span>
                   </template>
                </el-table-column>

                <el-table-column prop="bookName" label="图书名称" min-width="150" show-overflow-tooltip sortable/>
                <el-table-column prop="isbn" label="ISBN" width="140" show-overflow-tooltip/>
                <el-table-column prop="borrowDate" label="借阅日期" width="120" sortable/>
                <el-table-column prop="dueDate" label="应还日期" width="120" sortable/>
                <el-table-column prop="returnDate" label="归还日期" width="120" sortable>
                   <template #default="scope">
                     {{ scope.row.returnDate ? scope.row.returnDate : '-' }}
                   </template>
                </el-table-column>
                
                <el-table-column prop="status" label="状态" width="100" align="center">
                  <template #default="scope">
                    <el-tag v-if="!scope.row.status" type="danger">未归还</el-tag>
                    <el-tag v-else type="success">已归还</el-tag>
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="200" align="center" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="!scope.row.status"
                      link type="primary" 
                      size="small" 
                      @click="returnBook(scope.row.id, scope.row.isbn)"
                    >
                      代还
                    </el-button>
                    
                    <el-button 
                      v-if="!scope.row.status"
                      link type="warning" 
                      size="small" 
                      @click="openDateDialog(scope.row)"
                    >
                      延期
                    </el-button>

                    <el-button link type="danger" size="small" @click="deleteById(scope.row.id)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>

          <el-dialog
            v-model="dialogVisible"
            title="修改应还日期"
            width="30%"
            align-center
          >
            <el-form>
              <el-form-item label="新的应还日期" label-width="120px">
                <el-date-picker
                  v-model="newDueDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitDateChange">
                  确定修改
                </el-button>
              </span>
            </template>
          </el-dialog>

        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style>

.el-table {
  width: 80%;       
  margin: 0 auto;   
  min-width: 150px;
}

.el-table .danger-row {
  --el-table-tr-bg-color: rgba(255, 107, 107, 0.3);
}

.el-table .warning-row {
  --el-table-tr-bg-color: rgba(255, 251, 0, 0.3);
}

.el-table .success-row {
  --el-table-tr-bg-color: rgba(122, 255, 107, 0.3);
}
</style>