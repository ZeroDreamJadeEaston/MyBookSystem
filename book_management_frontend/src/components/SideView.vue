<template>
  <div class="sidebar-container">
    <div class="menu-card">
      <el-menu 
          :default-active="activeIndex" 
          class="my-el-menu"
      >
        <el-menu-item index="1" @click="routeToBook">
          <el-icon><Notebook/></el-icon>
          <span>图书信息</span>
        </el-menu-item>

        <el-menu-item index="5" @click="routeToRecommendation">
          <el-icon><Star/></el-icon>
          <span>新书推荐</span>
        </el-menu-item>

        <el-menu-item index="2" @click="routeToBorrow">
          <el-icon><Search/></el-icon>
          <span>{{ adminStore.isAdmin ? '读者借阅信息' : '借阅记录' }}</span>
        </el-menu-item>

        <el-menu-item index="3" @click="router.push('/user')">
          <el-icon><User/></el-icon>
          <span>个人信息</span>
        </el-menu-item>

        <el-menu-item v-if="adminStore.isAdmin" index="4" @click="router.push('/reader')">
          <el-icon><Setting/></el-icon>
          <span>用户管理</span>
        </el-menu-item>

      </el-menu>
    </div>
  </div>
</template>

<script lang="ts" setup>
// 引入 Star 图标
import {Notebook, Search, Setting, User, Star} from "@element-plus/icons-vue";
import {computed} from "vue";
import {useRouter, useRoute} from "vue-router";
import {useAdminStore} from "@/stores/admin";

const adminStore = useAdminStore();
const router = useRouter();
const route = useRoute();

const activeIndex = computed(() => {
  const path = route.path;
  if (path.includes('/borrow')) return '2';
  if (path.includes('/user')) return '3';
  if (path.includes('/reader')) return '4';
  
  // 高亮判断
  if (path.includes('/recommendation')) return '5';
  
  if (path.includes('/book')) return '1';
  return '1';
});

// 根据身份去不同的推荐页
const routeToRecommendation = () => {
  if (adminStore.isAdmin) {
    router.push("/admin/recommendation");
  } else {
    router.push("/recommendation");
  }
};

const routeToBorrow = () => {
  if (adminStore.isAdmin) {
    router.push("/admin/borrow");
  } else {
    router.push("/borrow");
  }
};

const routeToBook = () => {
  if (adminStore.isAdmin) {
    router.push("/admin/book");
  } else {
    router.push("/book");
  }
};
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center; 
  justify-content: center;
  background-color: transparent; 
}

.menu-card {
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px); 
  border-radius: 20px;
  padding: 20px 10px;
  width: 85%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.my-el-menu {
  background-color: transparent !important;
  border-right: none !important;
}

:deep(.el-menu-item) {
  border-radius: 10px;
  margin-bottom: 8px;
  justify-content: center;
  height: 50px;
  font-weight: 500;
  transition: all 0.3s;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(240, 249, 235, 0.8) !important;
  color: #67c23a !important;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.5) !important;
}
</style>