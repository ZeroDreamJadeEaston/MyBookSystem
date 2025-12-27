<script setup>
import {useDark} from "@vueuse/core";
import {useReaderStore} from "@/stores/reader.js";
import {useAdminStore} from "@/stores/admin.js";
import {ref} from "vue";
import {Moon, Sunny} from "@element-plus/icons-vue";

const adminStore = useAdminStore();
const readerStore = useReaderStore();
const isAdmin = adminStore.isAdmin;

let tag = ref();
tag.value = isAdmin ? adminStore.admin.nickname : readerStore.reader.nickname;
const isDark = useDark();

const leftUrl = ref("https://wx1.sinaimg.cn/large/007fhHssgy1i6plsobqq0g30f00f0qve.gif");
const rightUrl = ref("https://wx4.sinaimg.cn/large/008nviXogy1i6q084ofhsg30b40eukjn.gif");
</script>

<template>
  <div class="header-layout">
    <div class="header-inner">
      <el-menu mode="horizontal" :ellipsis="false" class="my-menu">
        
        <div class="side-box">
           <el-avatar shape="square" :size="100" :fit="'contain'" :src="leftUrl" style="background: transparent;"/>
        </div>
  
        <div class="center-box">
          <el-tag type="success" effect="dark" round class="huge-tag">{{ tag }}</el-tag>
          <el-tag v-if="isAdmin" type="warning" effect="dark" round class="huge-tag">管理员</el-tag>
          <el-tag v-else effect="dark" round class="huge-tag reader-tag">读者</el-tag>
          <el-switch v-model="isDark" :active-action-icon="Moon" :inactive-action-icon="Sunny" class="huge-switch"
              style="--el-switch-on-color: #13ce66; --el-switch-off-color: #409eff;"/>
        </div>
  
        <div class="side-box">
          <el-avatar shape="square" :size="100" :fit="'contain'" :src="rightUrl" style="background: transparent;"/>
        </div>
  
      </el-menu>
    </div>
  </div>
</template>

<style scoped>
.header-layout {
  width: 100%;
  height: 140px; 
  background-color: white;
  display: flex;
  justify-content: center; /* 让里面的内容居中 */
  border-bottom: 1px solid #eee;
}

.header-inner {
  width: 100%;
  max-width: 1000px; /* 限制最大宽度，防止散开*/
  height: 100%;
}

.my-menu {
  width: 100%;
  height: 100%;
  border-bottom: none !important;
  display: flex;
  align-items: center;
  justify-content: space-between; /* 在限制的宽度内两端对齐 */
}

.side-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px; 
}

.center-box {
  flex: 1; 
  display: flex;
  justify-content: center; 
  align-items: center;
  gap: 20px; 
}


.huge-tag {
  font-size: 20px !important;
  padding: 20px 35px !important;
  border-radius: 30px !important;
  font-weight: bold;
}
.reader-tag {
  background-color: #e1f3d8;
  border-color: #e1f3d8;
  color: #67c23a;
}
.huge-switch {
  transform: scale(1.8);
  margin-left: 20px;
}
</style>