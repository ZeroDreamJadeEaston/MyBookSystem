<script setup>
import { computed, onMounted, ref } from "vue";
import { 
  getRecommendationListService, 
  addRecommendationService, 
  deleteRecommendationService, 
  aiGenerateReasonService,
  updateRecommendationService 
} from "@/methods/recommendation.js";
import { getAllBookService } from "@/methods/book.js";
import SideView from "@/components/SideView.vue";
import HeaderView from "@/components/HeaderView.vue";
import { Plus, Search, MagicStick, Delete, Edit, View, StarFilled } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

// --- 列表与搜索相关 ---
const recommendList = ref([]);
const loading = ref(false);
const searchText = ref(""); 

onMounted(() => {
  loadRecommendations();
});

// 加载数据并匹配库存
const loadRecommendations = async () => {
  loading.value = true;
  try {
    const [recRes, bookRes] = await Promise.all([
      getRecommendationListService(),
      getAllBookService({ pageSize: 1000, currentPage: 1 })
    ]);

    const rawRecs = recRes.data || [];
    const allBooks = bookRes.data || [];

    recommendList.value = rawRecs.map(rec => {
      const targetBook = allBooks.find(b => b.title === rec.bookTitle);
      return {
        ...rec,
        number: targetBook ? targetBook.number : 0,
        isbn: targetBook ? targetBook.isbn : rec.isbn,
        // 确保封面也有值
        bookCover: rec.bookCover || (targetBook ? targetBook.cover : "")
      };
    });

  } catch (err) {
    console.error(err);
    ElMessage.error("数据加载失败");
  } finally {
    loading.value = false;
  }
};

const filteredRecommendList = computed(() => {
  if (!searchText.value) return recommendList.value;
  const lowerSearch = searchText.value.toLowerCase();
  return recommendList.value.filter(item => 
    (item.bookTitle && item.bookTitle.toLowerCase().includes(lowerSearch)) ||
    (item.bookAuthor && item.bookAuthor.toLowerCase().includes(lowerSearch))
  );
});

const highlight = (text, keyword) => {
  if (!text || !keyword) return text;
  const textStr = String(text);
  const keywordStr = String(keyword);
  try {
    const reg = new RegExp(keywordStr, 'gi'); 
    return textStr.replace(reg, (match) => {
      return `<span style="color: #f56c6c; font-weight: bold; background-color: #fef0f0;">${match}</span>`; 
    });
  } catch (e) {
    return textStr;
  }
}

// 删除推荐
const handleDelete = async (id) => {
  await deleteRecommendationService(id);
  ElMessage.success('已取消推荐');
  loadRecommendations();
};

// 查看详情逻辑
const detailVisible = ref(false);
const selectedRec = ref({});

const showDetail = (row) => {
  selectedRec.value = row;
  detailVisible.value = true;
};

// 修改推荐语逻辑 
const editDialogVisible = ref(false);
const editForm = ref({
  id: null,
  bookTitle: '',
  reason: ''
});

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    bookTitle: row.bookTitle,
    reason: row.reason
  };
  editDialogVisible.value = true;
};

const submitEdit = async () => {
  if (!editForm.value.reason) {
    return ElMessage.warning("推荐语不能为空");
  }
  try {
    await updateRecommendationService({
      id: editForm.value.id,
      reason: editForm.value.reason
    });
    ElMessage.success("修改成功！");
    editDialogVisible.value = false;
    loadRecommendations();
  } catch (err) { }
};

//  添加推荐弹窗逻辑 
const dialogVisible = ref(false);
const step = ref(1); 
const selectedBook = ref(null);
const reason = ref("");
const aiLoading = ref(false);

const bookTableData = ref([]);
const bookTotal = ref(0);
const bookLoading = ref(false);
const bookCondition = ref({
  bookName: null,
  currentPage: 1,
  pageSize: 5,
  sort: 'entryDate',
  order: 'descending'
});

const openAddDialog = () => {
  step.value = 1;
  selectedBook.value = null;
  reason.value = "";
  searchText.value = ""; 
  dialogVisible.value = true;
  loadBooks();
};

const loadBooks = async () => {
  bookLoading.value = true;
  const res = await getAllBookService(bookCondition.value);
  bookTableData.value = res.data;
  bookTotal.value = res.total;
  bookLoading.value = false;
};

const handleSelectBook = (row) => {
  selectedBook.value = row;
  step.value = 2; 
};

const handleAiGenerate = async () => {
  if (!selectedBook.value) return;
  aiLoading.value = true;
  try {
    const prompt = `请为书籍《${selectedBook.value.title}》（作者：${selectedBook.value.author}）写一段吸引人的推荐语，100字左右。`;
    const res = await aiGenerateReasonService(prompt);
    if(res.code === 1) {
        reason.value = res.data.introduction || res.data; 
        ElMessage.success("AI 推荐语生成完毕！");
    } else {
        ElMessage.warning(res.msg || "生成失败");
    }
  } catch (e) {
    ElMessage.error("AI 服务调用异常");
  } finally {
    aiLoading.value = false;
  }
};

const submitRecommendation = async () => {
  if (!reason.value) {
    return ElMessage.warning("请填写推荐理由");
  }

  // 唯一性检查：检查当前推荐列表中是否已存在该 ISBN
  const isDuplicate = recommendList.value.some(item => item.isbn === selectedBook.value.isbn);
  
  if (isDuplicate) {
    // 提醒错误信息
    ElMessage.error(`添加失败：ISBN [${selectedBook.value.isbn}] 已在推荐列表中！`);
    
    // 清空已填写的书籍信息
    selectedBook.value = null;
    reason.value = "";
    step.value = 1; // 回到第一步重新选择
    return;
  }

  try {
    await addRecommendationService({
      isbn: selectedBook.value.isbn,
      reason: reason.value
    });
    ElMessage.success("推荐成功！");
    dialogVisible.value = false;
    loadRecommendations();
  } catch (err) {
    // 接口报错处理
    ElMessage.error(err.response?.data?.msg || "服务器繁忙，请稍后再试");
  }
};
</script>

<template>
  <div>
    <el-container>
      <el-aside width="200px" style="background-color: transparent;">
        <side-view/>
      </el-aside>
      <el-container>
        <el-header style="height: auto; padding: 0;">
          <header-view/>
        </el-header>
        <el-main>
          <el-row>
            <el-col :span="24">
              <el-row style="margin-bottom: 20px;">
                <el-button @click="openAddDialog" :icon="Plus" size="large" type="success" circle />
                <el-col :span="1"/>
                <el-col :span="8">
                  <el-input 
                    v-model="searchText" 
                    size="large" 
                    placeholder="搜索推荐书名/作者..." 
                    clearable 
                    :prefix-icon="Search"
                  />
                </el-col>
              </el-row>
              
              <el-table 
                :data="filteredRecommendList" 
                v-loading="loading" 
                style="width: 100%" 
                :default-sort="{ prop: 'createTime', order: 'descending' }"
                border
              >
                <el-table-column label="封面" width="90" align="center">
                    <template #default="scope">
                        <el-image 
                          :src="scope.row.bookCover" 
                          style="width: 50px; height: 75px; border-radius: 4px;" 
                          fit="cover"
                          :preview-src-list="[scope.row.bookCover]"
                          preview-teleported
                        >
                          <template #error><div style="background:#f0f0f0;height:100%;display:flex;justify-content:center;align-items:center;color:#909399;font-size:12px;">无封面</div></template>
                        </el-image>
                    </template>
                </el-table-column>

                <el-table-column prop="bookTitle" label="书名" min-width="150" show-overflow-tooltip sortable>
                     <template #default="scope">
                        <span style="font-weight: bold;" v-html="highlight(scope.row.bookTitle, searchText)"></span>
                     </template>
                </el-table-column>

                <el-table-column prop="bookAuthor" label="作者" width="130" show-overflow-tooltip sortable>
                    <template #default="scope">
                        <span v-html="highlight(scope.row.bookAuthor, searchText)"></span>
                     </template>
                </el-table-column>

                <el-table-column label="库存" width="100" align="center" sortable prop="number">
                  <template #default="scope">
                    <el-tag v-if="scope.row.number > 0" type="success" effect="dark">
                      {{ scope.row.number }} 本
                    </el-tag>
                    <el-tag v-else type="danger" effect="dark">
                      缺货
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="reason" label="推荐理由" min-width="250" show-overflow-tooltip />
                <el-table-column prop="createTime" label="推荐时间" width="130" sortable>
                     <template #default="scope">
                        {{ scope.row.createTime ? scope.row.createTime.substring(0, 10) : '' }}
                     </template>
                </el-table-column>
                
                <el-table-column label="操作" width="260" fixed="right" align="center">
                    <template #default="scope">
                        <div style="display: flex; justify-content: center; gap: 5px;">
                          <el-button
                             link
                             type="primary"
                             size="small"
                             :icon="View"
                             @click="showDetail(scope.row)"
                          >
                             详情
                          </el-button>

                          <el-button 
                             link 
                             type="primary" 
                             size="small" 
                             :icon="Edit" 
                             @click="handleEdit(scope.row)"
                          >
                             修改
                          </el-button>

                          <el-popconfirm
                              title="确定要取消这本推荐吗?"
                              confirm-button-text="确定"
                              cancel-button-text="取消"
                              confirm-button-type="danger"
                              @confirm="handleDelete(scope.row.id)"
                          >
                            <template #reference>
                              <el-button link type="danger" size="small" :icon="Delete">取消推荐</el-button>
                            </template>
                          </el-popconfirm>
                        </div>
                    </template>
                </el-table-column>
              </el-table>

              <el-dialog
                title="推荐图书详情"
                width="50%"
                center
                align-center
                v-model="detailVisible"
              >
                <div style="display: flex; gap: 30px;" v-if="selectedRec.bookTitle">
                  <el-image
                    style="width: 150px; height: 210px; border-radius: 8px; flex-shrink: 0;"
                    :src="selectedRec.bookCover"
                    fit="cover"
                  >
                    <template #error><div style="background:#f0f0f0;height:100%;display:flex;justify-content:center;align-items:center;">无封面</div></template>
                  </el-image>
                  <div style="flex: 1;">
                    <h2 style="margin-top: 0; color: #303133;">{{ selectedRec.bookTitle }}</h2>
                    <p><strong>作者：</strong>{{ selectedRec.bookAuthor }}</p>
                    <p><strong>库存：</strong>
                       <span :style="{color: selectedRec.number > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold'}">
                         {{ selectedRec.number }} 本
                       </span>
                    </p>
                    <p><strong>ISBN：</strong>{{ selectedRec.isbn || '暂无' }}</p>
                    <p><strong>推荐时间：</strong>{{ selectedRec.createTime?.substring(0, 10) }}</p>
                    
                    <div style="background: #fdf6ec; padding: 15px; border-radius: 8px; border-left: 5px solid #e6a23c;">
                      <h4 style="margin: 0 0 10px 0; color: #e6a23c;"><el-icon><StarFilled /></el-icon> 馆长推荐语：</h4>
                      <p style="margin: 0; line-height: 1.6; color: #606266;">{{ selectedRec.reason }}</p>
                    </div>
                  </div>
                </div>
                <template #footer>
                  <span class="dialog-footer">
                    <el-button @click="detailVisible = false">关闭</el-button>
                  </span>
                </template>
              </el-dialog>

              <el-dialog v-model="editDialogVisible" title="修改推荐语" width="500px" align-center>
                 <h3 style="margin-top:0;">{{ editForm.bookTitle }}</h3>
                 <el-input 
                    v-model="editForm.reason" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="请输入新的推荐理由..." 
                    maxlength="500"
                    show-word-limit
                 />
                 <template #footer>
                   <span class="dialog-footer">
                     <el-button @click="editDialogVisible = false">取消</el-button>
                     <el-button type="primary" @click="submitEdit">保存修改</el-button>
                   </span>
                 </template>
              </el-dialog>

              <el-dialog v-model="dialogVisible" title="添加新书推荐" width="600px" align-center destroy-on-close :close-on-click-modal="false">
                  <div v-if="step === 1">
                      <div style="margin-bottom: 15px; display: flex; gap: 10px;">
                          <el-input v-model="bookCondition.bookName" placeholder="搜索库中书名..." :prefix-icon="Search" @input="loadBooks" clearable />
                      </div>
                      
                      <el-table :data="bookTableData" v-loading="bookLoading" height="350" border stripe size="small">
                          <el-table-column prop="title" label="书名" show-overflow-tooltip />
                          <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
                          <el-table-column prop="entryDate" label="入库时间" width="110">
                               <template #default="scope">
                                  {{ scope.row.entryDate ? scope.row.entryDate.substring(0, 10) : '未知' }}
                               </template>
                          </el-table-column>
                          <el-table-column width="80" label="操作" align="center">
                              <template #default="scope">
                                  <el-button link type="primary" size="small" @click="handleSelectBook(scope.row)">
                                      选择
                                  </el-button>
                              </template>
                          </el-table-column>
                      </el-table>

                      <el-pagination 
                          style="margin-top: 15px; justify-content: flex-end;" 
                          layout="prev, pager, next" 
                          :total="bookTotal" 
                          v-model:current-page="bookCondition.currentPage"
                          v-model:page-size="bookCondition.pageSize"
                          @current-change="loadBooks"
                          small
                      />
                  </div>

                  <div v-if="step === 2">
                      <div style="display: flex; gap: 20px; margin-bottom: 20px; background: #f5f7fa; padding: 15px; border-radius: 8px;">
                          <el-image :src="selectedBook.cover" style="width: 80px; height: 110px; border-radius: 4px;" fit="cover" />
                          <div>
                              <h3 style="margin: 0 0 10px 0;">{{ selectedBook.title }}</h3>
                              <p style="color: #666; font-size: 13px; margin: 5px 0;">作者：{{ selectedBook.author }}</p>
                              <p style="color: #666; font-size: 13px; margin: 5px 0;">ISBN：{{ selectedBook.isbn }}</p>
                          </div>
                      </div>
                      <el-form label-position="top">
                          <el-form-item label="推荐理由">
                              <el-input 
                                  v-model="reason" 
                                  type="textarea" 
                                  :rows="6" 
                                  placeholder="请输入推荐理由，或者点击下方按钮让 AI 帮你写..." 
                                  maxlength="500"
                                  show-word-limit
                              />
                          </el-form-item>
                      </el-form>
                      <div style="margin-top: 20px; display: flex; justify-content: space-between;">
                          <el-button @click="step = 1">上一步 (重选)</el-button>
                          <div style="display: flex; gap: 10px;">
                              <el-button type="success" plain :icon="MagicStick" :loading="aiLoading" @click="handleAiGenerate">AI 生成推荐语</el-button>
                              <el-button type="primary" @click="submitRecommendation">确认推荐</el-button>
                          </div>
                      </div>
                  </div>
              </el-dialog>

            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
</style>