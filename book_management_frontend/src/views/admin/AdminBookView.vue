<script setup>
import {onMounted, ref} from "vue";
import {addBookService, deleteBookService, getAllBookService, updateBookService} from "@/methods/book.js";
import SideView from "@/components/SideView.vue";
import HeaderView from "@/components/HeaderView.vue";
import {Plus, MagicStick} from "@element-plus/icons-vue"; 
import {ElMessage} from "element-plus";
import request from "@/util/request"; 

const tableData = ref([]);

// 页面加载
onMounted(async () => {
  await getAllBooks();
})

// 获取数据
const getAllBooks = async function () {
  loading.value = true;
  const result = await getAllBookService(condition.value);
  tableData.value = result.data;
  total.value = result.total;
  loading.value = false;
};

// 搜索条件
const condition = ref({
  bookName: null,
  author: null,
  isbn: null,
  number: 0,
  currentPage: 1,
  pageSize: 8,
  sort: null, 
  order: null
});

// 高亮关键词工具函数
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

// 监听表格排序事件
const handleSortChange = ({ prop, order }) => {
  condition.value.sort = prop;
  condition.value.order = order;
  getAllBooks();
};

const loading = ref(false);
const total = ref(0)

const handleSizeChange = (val) => {
  loading.value = true;
  condition.value.pageSize = val;
  getAllBooks();
  loading.value = false;
};

const handleCurrentChange = (val) => {
  loading.value = true;
  condition.value.currentPage = val;
  getAllBooks();
  loading.value = false;
};

let detail = ref(false);

const showDetail = (row) => {
  detail.value = true;
  book.value = {...row}; 
}

const closeDialog = async () => {
  detail.value = false;
  await getAllBooks();
}

let book = ref({
  title: null,
  isbn: null,
  cover: null,
  introduction: null,
  number: 0,
  author: null,
  entryDate: null 
});

let showAdd = ref(false);

const addBook = ref({
  title: null,
  isbn: null,
  cover: null,
  introduction: null,
  number: null,
  author: null,
  entryDate: null
})

const openAddDialog = () => {
  addBook.value = {
    title: null,
    isbn: null,
    cover: null,
    introduction: null,
    number: null,
    author: null,
    entryDate: null 
  };
  showAdd.value = true;
}

const aiLoading = ref(false); 
const autoFillFromAI = async () => {
  if (!addBook.value.title) {
    return ElMessage.warning("请先输入书名，AI 才能帮你查找哦！");
  }
  aiLoading.value = true;
  try {
    const res = await request.get('/book/ai', {
      params: { title: addBook.value.title }
    });
    if (res.code === 1 && res.data) {
      addBook.value.author = res.data.author;
      addBook.value.isbn = res.data.isbn;
      addBook.value.cover = res.data.cover;
      addBook.value.introduction = res.data.introduction;
      ElMessage.success("AI 补全成功！");
    } else {
      ElMessage.info(res.msg || "未找到相关书籍信息");
    }
  } catch (error) {
    console.error(error);
  } finally {
    aiLoading.value = false;
  }
}

// 重置添加表单
const resetAddForm = () => {
  addBook.value = {
    title: null,
    isbn: null,
    cover: null,
    introduction: null,
    number: null,
    author: null,
    entryDate: null 
  };
};

// 添加图书函数
const addBookFunc = async () => {
  try {
    const res = await addBookService(addBook.value);

    // 如果成功 (code === 1)
    if (res.code === 1) {
      ElMessage.success("添加成功!");
      showAdd.value = false;
      condition.value.bookName = addBook.value.title;
      await getAllBooks();
      resetAddForm();
    } 
    // 如果失败
    else {
      ElMessage.error(res.msg || "添加失败");
      resetAddForm();
    }
  } catch (err) {
    console.error("捕获异常:", err);
    resetAddForm(); 
    if (err.msg) return; 
    const errorMsg = err.response?.data?.msg || "服务器繁忙，请稍后再试";
    ElMessage.error(errorMsg);
  }
}

const rules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' }, 
    { max: 30, message: '最多30个字符', trigger: ['blur', 'change'] }
  ],
  isbn: [
    { required: true, message: '请输入ISBN号', trigger: 'blur' }, 
    { max: 20, message: '最多20个字符', trigger: ['blur', 'change'] }
  ],
  cover: [
    { max: 200, message: '最多200个字符', trigger: ['blur', 'change'] }
  ],
  introduction: [
    { max: 400, message: '最多400个字符', trigger: ['blur', 'change'] }
  ],
  number: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }, 
    { type: 'number', min: 0, message: '必须是大于等于0的数字', trigger: ['blur', 'change'] }
  ],
  author: [
    { max: 20, message: '最多20个字符', trigger: ['blur', 'change'] }
  ]
};

const deleteBook = async (isbn) => {
  await deleteBookService(isbn);
  ElMessage.success('删除成功!');
  await getAllBooks();
}

const updateBook = async () => {
  await updateBookService(book.value);
  detail.value = false;
  ElMessage.success("已保存!");
}
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
              <el-row>
                <el-button @click="openAddDialog" :icon="Plus" size="large" type="success" circle :span="2"/>
                <el-col :span="1"/>
                <el-col :span="6">
                  <el-input @input="getAllBooks" v-model="condition.bookName" size="large" placeholder="书名" clearable/>
                </el-col>
                <el-col :span="1"/>
                <el-col :span="6">
                  <el-input @input="getAllBooks" v-model="condition.author" size="large" placeholder="作者" clearable/>
                </el-col>
                <el-col :span="1"/>
                <el-col :span="6">
                  <el-input @input="getAllBooks" v-model="condition.isbn" size="large" placeholder="ISBN" clearable/>
                </el-col>
              </el-row>
              <br>

              <el-table style="width: 100%"
                        :data="tableData" 
                        v-loading="loading"
                        @sort-change="handleSortChange" 
                        :default-sort="{ prop: 'entryDate', order: 'descending' }">
                
                <el-table-column prop="title" label="书名" width="150" show-overflow-tooltip>
                    <template #default="scope">
                        <span v-html="highlight(scope.row.title, condition.bookName)"></span>
                    </template>
                </el-table-column>

                <el-table-column prop="author" label="作者" width="150" show-overflow-tooltip>
                     <template #default="scope">
                        <span v-html="highlight(scope.row.author, condition.author)"></span>
                    </template>
                </el-table-column>
                
                <el-table-column prop="isbn" label="ISBN" width="150" sortable="custom" show-overflow-tooltip>
                     <template #default="scope">
                        <span v-html="highlight(scope.row.isbn, condition.isbn)"></span>
                    </template>
                </el-table-column>
                
                <el-table-column prop="entryDate" label="入库日期" width="150" sortable="custom">
                  <template #default="scope">
                     {{ scope.row.entryDate ? scope.row.entryDate.substring(0, 10) : '未知' }}
                  </template>
                </el-table-column>

                <el-table-column sortable="custom" prop="number" label="库存量" width="120"/>

                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button link type="primary" size="small"
                               @click="showDetail(scope.row)">详细信息
                    </el-button>
                    <el-popconfirm
                        confirm-button-text="Yes"
                        cancel-button-text="我再想想"
                        confirm-button-type="danger"
                        @confirm="deleteBook(scope.row.isbn)"
                        title="真的要删除这本书吗?">
                      <template #reference>
                        <el-button link type="danger" size="small">删除
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
              <br>
              <div>
                <el-pagination
                    v-model:current-page="condition.currentPage"
                    v-model:page-size="condition.pageSize"
                    :page-sizes="[5,8,10,15,30,50,100]"
                    :background="true"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"/>
              </div>

              <el-dialog title="图书详细信息" width="40%" center align-center
                         v-model="detail" :before-close="closeDialog">
                <el-form :inline="true" :rules="rules" :model="book">
                  <el-form-item style="display: flex;" prop="cover">
                    <el-image style="width: 100px; height: 150px"
                              :preview-src-list="[book.cover]"
                              :src="book.cover" :fit="'fill'">
                        <template #error>
                          <div style="display: flex; justify-content: center; align-items: center; width: 100%; height: 100%; background: #f5f7fa; color: #909399;">
                            无封面
                          </div>
                        </template>
                    </el-image>
                  </el-form-item>
                  <el-form-item label="书名" prop="title" required>
                    <el-input v-model="book.title"/>
                  </el-form-item>
                  <el-form-item label="库存" prop="number" required>
                    <el-input v-model.number="book.number"/>
                  </el-form-item>
                  <el-form-item label="作者" prop="author">
                    <el-input v-model="book.author"/>
                  </el-form-item>
                  <el-form-item label="ISBN" prop="isbn">
                    <el-input v-model="book.isbn" disabled /> 
                  </el-form-item>
                  <el-form-item label="入库日期" prop="entryDate">
                    <el-date-picker
                      v-model="book.entryDate"
                      type="date"
                      placeholder="选择日期"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 200px"
                    />
                  </el-form-item>
                  <el-form-item label="简介" prop="introduction">
                    <el-input v-model="book.introduction"
                              :autosize="{ minRows: 2, maxRows: 6 }"
                              type="textarea"/>
                  </el-form-item>
                </el-form>
                <el-form>
                  <template #default>
                    <el-form-item>
                      <el-button type="primary" @click="updateBook">保存</el-button>
                      <el-button type="danger" @click="closeDialog">取消</el-button>
                    </el-form-item>
                  </template>
                </el-form>
              </el-dialog>

              <el-dialog v-model="showAdd" title="添加图书" width="35%" center align-center>
                <el-form :inline="true" :model="addBook" :rules="rules">
                  <el-form-item prop="title" required label="书名">
                    <el-input v-model="addBook.title" placeholder="输入书名" style="width: 220px">
                      <template #append>
                         <el-button :icon="MagicStick" @click="autoFillFromAI" :loading="aiLoading" title="AI自动补全" />
                      </template>
                    </el-input>
                  </el-form-item>
                  <el-form-item prop="author" label="作者">
                    <el-input v-model="addBook.author" placeholder="作者"/>
                  </el-form-item>
                  <el-form-item prop="isbn" required label="ISBN">
                    <el-input v-model="addBook.isbn" placeholder="ISBN"/>
                  </el-form-item>
                  <el-form-item prop="number" required label="库存">
                    <el-input v-model.number="addBook.number" placeholder="库存"/>
                  </el-form-item>
                  <el-form-item prop="entryDate" label="入库日期">
                    <el-date-picker
                      v-model="addBook.entryDate"
                      type="date"
                      placeholder="留空默认当前时间"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 200px"
                    />
                  </el-form-item>
                  <el-form-item prop="cover" label="封面">
                    <el-input v-model="addBook.cover" placeholder="图片链接"/>
                  </el-form-item>
                  <el-form-item prop="introduction" label="简介">
                    <el-input placeholder="简介" type="textarea" v-model="addBook.introduction" clearable style="width: 500px"/>
                  </el-form-item>
                </el-form>
                <div style="text-align: center; margin-top: 20px;">
                  <el-button type="primary" @click="addBookFunc">确认添加</el-button>
                  <el-button type="danger" @click="showAdd = false">取消</el-button>
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