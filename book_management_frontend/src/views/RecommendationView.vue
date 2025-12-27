<script setup>
import { onMounted, ref, computed } from "vue";
import { getRecommendationListService } from "@/methods/recommendation.js";
// 引入获取所有图书的接口
import { getAllBookService } from "@/methods/book.js";
import { borrowService } from "@/methods/borrow.js";
import SideView from "@/components/SideView.vue";
import HeaderView from "@/components/HeaderView.vue";
import { Search, StarFilled, Reading } from "@element-plus/icons-vue";
import { useAdminStore } from "@/stores/admin.js";
import { ElMessage } from "element-plus";

const adminStore = useAdminStore();
const recommendList = ref([]);
const loading = ref(false);
const searchText = ref(""); 

const isAdmin = computed(() => adminStore.isAdmin);

onMounted(async () => {
  await loadRecommendations();
});

// 加载数据的同时，匹配库存
const loadRecommendations = async () => {
  loading.value = true;
  try {
    // 1. 并行发起两个请求：获取推荐列表 + 获取所有图书(为了拿库存)
    const [recRes, bookRes] = await Promise.all([
      getRecommendationListService(),
      getAllBookService({ pageSize: 1000, currentPage: 1 }) //以此拉取足够多的书来匹配
    ]);

    const rawRecList = recRes.data || [];
    const allBooks = bookRes.data || [];

    // 2. 将库存信息“嫁接”到推荐列表上
    recommendList.value = rawRecList.map(rec => {
      // 尝试通过书名匹配 如果有 ISBN 匹配更好
      const targetBook = allBooks.find(book => book.title === rec.bookTitle);
      
      // 如果匹配到了，就把库存number和ISBN赋给推荐对象
      return {
        ...rec,
        // 优先使用匹配到的库存，如果没匹配到则默认为 0
        number: targetBook ? targetBook.number : 0, 
        isbn: targetBook ? targetBook.isbn : (rec.isbn || null),
        // 如果推荐接口没返回封面，也可以尝试用图书库的封面
        bookCover: rec.bookCover || (targetBook ? targetBook.cover : "")
      };
    });

  } catch (err) {
    console.error("加载数据失败", err);
    ElMessage.error("数据加载异常");
  } finally {
    loading.value = false;
  }
};

const filteredList = computed(() => {
  if (!searchText.value) return recommendList.value;
  return recommendList.value.filter(item => 
    item.bookTitle.includes(searchText.value) || 
    item.bookAuthor.includes(searchText.value)
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

// 详情弹窗
let detailVisible = ref(false);
let selectedRec = ref({});

const showDetail = (row) => {
  selectedRec.value = row;
  detailVisible.value = true;
};

// 借阅逻辑
let showDrawer = ref(false);
let borrowBook = ref({});
const dueDate = ref(new Date());

const handleShowBorrow = (row) => {
  if (isAdmin.value) {
    ElMessage.warning("管理员不能借阅图书");
    return;
  }
  // 必须要有 ISBN 才能借
  if (!row.isbn) {
    ElMessage.error("未找到该书的ISBN信息，无法借阅");
    return;
  }
  borrowBook.value = row;
  showDrawer.value = true;
};

const submitBorrow = async () => {
  try {
    const isbn = borrowBook.value.isbn;
    await borrowService(isbn, dueDate.value);
    
    ElMessage.success("借阅成功！");
    showDrawer.value = false;
    await loadRecommendations(); // 刷新列表更新库存
  } catch (err) {
    console.error(err);
  }
};

const disabledDate = (time) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return time.getTime() < today.getTime();
};
</script>

<template>
  <div class="page-background">
    <el-container>
      <el-aside width="220px" style="background-color: transparent;">
        <side-view />
      </el-aside>
      
      <el-container>
        <el-header style="height: auto; padding: 0;">
          <header-view />
        </el-header>
        
        <el-main>
          <el-row>
            <el-col :span="24">
              <div class="search-and-tag-container">
                <el-button
                  @click="loadRecommendations"
                  :icon="Search"
                  size="large"
                  type="success"
                  circle
                />
                
                <el-input
                  v-model="searchText"
                  size="large"
                  placeholder="搜索推荐的书名或作者"
                  clearable
                  style="width: 400px; margin: 0 20px;"
                />

                <el-tag type="warning" size="large" effect="dark" round class="center-tag">
                  <el-icon><StarFilled /></el-icon> 本周精选推荐
                </el-tag>
              </div>
              
              <br />

              <el-table
                height="600"
                :data="filteredList"
                style="width: 100%; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);"
                v-loading="loading"
                element-loading-text="加载数据中..."
                border
              >
                <el-table-column label="封面" width="100" align="center">
                  <template #default="scope">
                    <el-image 
                      :src="scope.row.bookCover" 
                      style="width: 50px; height: 70px; border-radius: 4px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);" 
                      fit="cover"
                      :preview-src-list="[scope.row.bookCover]"
                      preview-teleported
                    >
                        <template #error>
                          <div class="image-slot">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                    </el-image>
                  </template>
                </el-table-column>

                <el-table-column prop="bookTitle" label="书名" min-width="150" show-overflow-tooltip>
                  <template #default="scope">
                    <span style="font-weight: bold; color: #409eff;" v-html="'《' + highlight(scope.row.bookTitle, searchText) + '》'"></span>
                  </template>
                </el-table-column>
                
                <el-table-column prop="bookAuthor" label="作者" width="150" show-overflow-tooltip>
                   <template #default="scope">
                      <span v-html="highlight(scope.row.bookAuthor, searchText)"></span>
                   </template>
                </el-table-column>
                
                <el-table-column label="库存" width="100" align="center" sortable prop="number">
                  <template #default="scope">
                    <el-tag v-if="scope.row.number > 0" type="success" effect="dark">
                      余 {{ scope.row.number }} 本
                    </el-tag>
                    <el-tag v-else type="danger" effect="dark">
                      暂无库存
                    </el-tag>
                  </template>
                </el-table-column>

                <el-table-column prop="reason" label="推荐理由" min-width="250" show-overflow-tooltip>
                  <template #default="scope">
                    <el-tag type="warning" size="small" effect="plain" style="margin-right: 8px;">推荐</el-tag>
                    <span>{{ scope.row.reason }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="推荐日期" width="120" sortable prop="createTime">
                  <template #default="scope">
                    {{ scope.row.createTime ? scope.row.createTime.substring(0, 10) : '近期' }}
                  </template>
                </el-table-column>

                <el-table-column label="操作" width="180" fixed="right" align="center">
                  <template #default="scope">
                    <el-button
                      type="success"
                      size="small"
                      :icon="Reading"
                      :disabled="!scope.row.isbn || scope.row.number <= 0 || isAdmin"
                      @click="handleShowBorrow(scope.row)"
                    >
                      {{ scope.row.number > 0 ? '借阅' : '缺货' }}
                    </el-button>

                    <el-button
                      link
                      type="primary"
                      size="small"
                      @click="showDetail(scope.row)"
                    >详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-drawer v-model="showDrawer" size="30%">
                <template #header>
                   <h2 style="margin:0; color:#409eff;">📚 借阅确认</h2>
                </template>
                
                <div style="text-align: center; margin-bottom: 20px;">
                    <el-image 
                        style="width: 120px; height: 180px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);" 
                        :src="borrowBook.bookCover" 
                        :fit="'cover'" 
                    />
                    <h3 style="margin-top:15px; color:#303133;">{{ borrowBook.bookTitle }}</h3>
                </div>

                <div style="padding: 0 20px;">
                    <h4 style="color: #606266; margin-bottom: 10px;">请选择归还日期：</h4>
                    <el-date-picker
                        v-model="dueDate"
                        type="datetime"
                        placeholder="选择归还日期"
                        format="YYYY-MM-DD HH:mm"
                        style="width: 100%;"
                        :disabled-date="disabledDate"
                        size="large"
                    />
                </div>

                <template #footer>
                  <div style="display: flex; justify-content: flex-end; gap: 10px;">
                    <el-button @click="showDrawer = false" size="large">取消</el-button>
                    <el-button type="primary" @click="submitBorrow" size="large">确认借阅</el-button>
                  </div>
                </template>
              </el-drawer>

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
                  />
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
                    <el-button 
                      type="primary" 
                      @click="handleShowBorrow(selectedRec)" 
                      :disabled="!selectedRec.isbn || selectedRec.number <= 0 || isAdmin"
                    >
                      立即借阅
                    </el-button>
                  </span>
                </template>
              </el-dialog>
              
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
:deep(.el-main) {
  padding: 20px 40px;
}

.search-and-tag-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.4);
  padding: 15px 0;
  border-radius: 20px;
  backdrop-filter: blur(5px);
}

.center-tag {
  font-size: 16px;
  font-weight: bold;
  padding: 0 20px;
  height: 40px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.page-background {
  min-height: 100vh;
}
</style>