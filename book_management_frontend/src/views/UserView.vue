<script setup>
import { useReaderStore } from "@/stores/reader.js";
import { ref, computed } from "vue";
import router from "@/router/index.js";
import { updateReaderService, deleteMyAccountService } from "@/methods/reader.js";
import { ElMessage, ElMessageBox } from "element-plus";
import HeaderView from "@/components/HeaderView.vue";
import SideView from "@/components/SideView.vue";
import { useAdminStore } from "@/stores/admin.js";
import { updateAdminService } from "@/methods/admin.js";
import { useTokenStore } from "@/stores/token.js"; 
import { logoutService } from "@/methods/logout.js"; 
import request from "@/util/request"; 
import { User, Lock, Phone, Calendar, Male, Female, Postcard, Edit, SwitchButton, Delete } from "@element-plus/icons-vue";

const adminStore = useAdminStore();
const readerStore = useReaderStore();
const tokenStore = useTokenStore();

//  使用 computed 保持响应式，防止刷新后状态丢失
const isAdmin = computed(() => adminStore.isAdmin);
const currentUser = computed(() => isAdmin.value ? adminStore.admin : readerStore.reader);

let isMan = ref(true);
// 监听数据变化初始化性别
if (!isAdmin.value && currentUser.value) {
  isMan.value = currentUser.value.gender === "男";
}

// 保存修改
const handleSave = async () => {
  if (isAdmin.value) {
    await updateAdminService(adminStore.admin);
  } else {
    currentUser.value.gender = isMan.value ? "男" : "女";
    await updateReaderService(currentUser.value);
  }
  ElMessage.success("保存成功！");
};

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', { 
      confirmButtonText: '退出', 
      cancelButtonText: '取消', 
      type: 'warning' 
    });

    try {
      await logoutService(); 
    } catch (e) {
      console.warn("后端退出接口异常，忽略并强制本地退出");
    }

    // 清理数据
    tokenStore.setToken(''); 
    
    // 安全清空 store
    if (readerStore.setReader) readerStore.setReader({});
    else if (readerStore.clearReader) readerStore.clearReader();
    
    if (adminStore.setAdmin) adminStore.setAdmin({});
    else if (adminStore.clearAdmin) adminStore.clearAdmin();
    
    ElMessage.success('已退出登录');
    await router.push("/login");

  } catch (err) {
    if (err !== 'cancel') console.error("退出逻辑出错:", err);
  }
};

//  注销账号逻辑 (内联了管理员删除请求，并补全了 ID 参数)
const handleDeleteAccount = () => {
  const warningText = isAdmin.value 
    ? '注销管理员账号将永久删除您的所有数据。确定要继续吗？'
    : '注销账号将永久删除您的所有数据，且前提是必须已归还所有图书。确定要继续吗？';

  ElMessageBox.confirm(
    warningText,
    '危险操作警告',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'error', 
      icon: Delete   
    }
  ).then(async () => {
    try {
      if (isAdmin.value) {
         // 直接发起请求，并传递当前管理员的 ID
         await request.delete('/admin/delete', { 
            params: { id: adminStore.admin.id } 
         });
      } else {
         await deleteMyAccountService();
      }
      
      ElMessage.success('账号已注销，感谢您的使用！');
      
      // 清理数据并跳转
      tokenStore.setToken('');
      if (readerStore.setReader) readerStore.setReader({});
      if (adminStore.setAdmin) adminStore.setAdmin({});

      await router.push("/login");
      
    } catch (error) {
      console.error(error);
      if(isAdmin.value) {
          ElMessage.error("注销失败，请确保后端 AdminController 已添加 delete 接口");
      }
    }
  }).catch(() => {});
};

const handleCancel = () => {
  router.push(isAdmin.value ? '/admin/book' : '/book');
};
</script>

<template>
  <el-container class="page-container">
    
    <el-aside width="220px" class="my-aside" style="background-color: transparent;">
      <side-view/>
    </el-aside>
    
    <el-container>
      <el-header style="height: auto; padding: 0;">
        <header-view />
      </el-header>
      
      <el-main class="main-content">
        <div class="form-card">
          
          <div class="card-header">
            <h3><el-icon><Edit /></el-icon> 编辑资料</h3>
            <span class="sub-text">管理您的个人账户信息</span>
          </div>

          <el-form :model="currentUser" label-position="top" size="large" class="user-form">
            <el-row :gutter="40">
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="currentUser.username" disabled :prefix-icon="User" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="currentUser.nickname" :prefix-icon="Postcard"/>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="登录密码">
              <el-input v-model="currentUser.password" show-password type="password" :prefix-icon="Lock" />
            </el-form-item>

            <template v-if="!isAdmin.value"> 
               </template>
            <template v-if="!isAdmin">
              <el-row :gutter="40">
                <el-col :span="12">
                  <el-form-item label="联系电话">
                    <el-input v-model="currentUser.tel" :prefix-icon="Phone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="年龄">
                    <el-input v-model="currentUser.age" :prefix-icon="Calendar" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="性别">
                 <el-radio-group v-model="isMan" fill="#409eff">
                    <el-radio-button :label="true"><el-icon><Male /></el-icon> 男生</el-radio-button>
                    <el-radio-button :label="false"><el-icon><Female /></el-icon> 女生</el-radio-button>
                 </el-radio-group>
              </el-form-item>
            </template>

            <el-divider />

            <div class="action-footer">
              <div class="left-btns">
                 <el-button @click="handleLogout" type="info" plain :icon="SwitchButton">退出登录</el-button>
                 
                 <el-button 
                   @click="handleDeleteAccount" 
                   type="danger" 
                   plain 
                   :icon="Delete"
                   style="margin-left: 10px;"
                 >
                   注销账号
                 </el-button>
              </div>

              <div class="right-btns">
                <el-button @click="handleCancel">取消</el-button>
                <el-button type="primary" @click="handleSave" color="#626aef">保存更改</el-button>
              </div>
            </div>

          </el-form>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.page-container {
  height: 100vh;
  width: 100vw;
  background-image: url("https://wx1.sinaimg.cn/large/006N9rP0ly1i6lkg4345oj31402epn95.jpg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed; 
}
.my-aside {
  background-color: transparent !important;
}
.main-content {
  display: flex;
  justify-content: flex-start;
  padding-left: 60px;
  padding-top: 40px;
}
.form-card {
  background: white;
  width: 100%;
  max-width: 1000px; 
  padding: 50px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  height: fit-content; 
}
.card-header {
  margin-bottom: 30px;
  border-left: 4px solid #626aef;
  padding-left: 15px;
}
.card-header h3 {
  margin: 0 0 5px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.sub-text {
  color: #999;
  font-size: 14px;
}
.action-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}
.right-btns {
  display: flex;
  gap: 15px;
}
:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>