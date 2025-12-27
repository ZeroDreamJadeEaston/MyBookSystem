<script setup>
import {reactive, ref, onMounted} from "vue"; // 引入 onMounted
import {adminLoginService, loginService} from "@/methods/login";
import { useRoute } from "vue-router"; // 引入 useRoute
import router from "@/router";
import {useTokenStore} from "@/stores/token";
import {useReaderStore} from "@/stores/reader.js";
import {ElLoading, ElNotification, ElMessage} from "element-plus";
import {useAdminStore} from "@/stores/admin.js";

const adminStore = useAdminStore();
const readerStore = useReaderStore();
const tokenStore = useTokenStore();
const route = useRoute(); // 获取路由参数

const ruleFormRef = ref();

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 1, max: 10, message: "用户名长度需在 1 到 10 个字符之间", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 10, message: "密码长度需在 6 到 10 个字符之间", trigger: "blur" },
  ]
};

const loginDto = ref({
  username: "",
  password: "",
});

// 页面加载时自动填充注册过来的账号密码
onMounted(() => {
  if (route.query.u && route.query.p) {
    loginDto.value.username = route.query.u;
    loginDto.value.password = route.query.p;
    ElMessage.success({
      message: "已为您自动填充注册信息，请直接登录",
      appendTo: '.login-page-wrapper'
    });
  }
});

// 读者登录逻辑
const login = async function () {
  const loading = ElLoading.service({
    lock: true,
    text: '正在登录中...',
    background: 'rgba(0, 0, 0, 0.7)',
  })

  try {
    const result = await loginService(loginDto.value);
    
    if (!result || !result.data) {
      throw new Error("服务器返回数据为空！");
    }

    const returnReader = result.data;
    
    readerStore.setReader(returnReader);
    tokenStore.setToken(returnReader.token);
    adminStore.setIsAdmin(false);

    await router.push("/book");
    
    ElNotification.success({
      title: "登录成功",
      message: "欢迎回来," + (returnReader.nickname || "读者"),
      duration: 1500,
    });

  } catch (err) {
    if (err.message === "服务器返回数据为空！") {
      ElMessage({
        message: err.message,
        type: 'error',
        appendTo: '.login-page-wrapper'
      });
    }
  } finally {
    loading.close();
  }
};

// 管理员登录逻辑
const adminLogin = async function () {
  const loading = ElLoading.service({
    lock: true,
    text: '管理员登录中...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  
  try {
    const result = await adminLoginService(loginDto.value);
    const returnAdmin = result.data;
    
    adminStore.setAdmin(returnAdmin);
    tokenStore.setToken(returnAdmin.token);
    adminStore.setIsAdmin(true);

    await router.push("/admin/book");
    ElNotification.success({
      title: "登录成功",
      message: "管理员 " + returnAdmin.nickname,
      duration: 3000,
    });
  } catch(err) {
    // 错误由 request.js 处理，此处保持静默或手动处理特殊情况
  } finally {
    loading.close();
  }
};

const submitForm = async (formEl) => {
  if (!formEl) return;
  formEl.validate(async (valid) => {
    if (valid) {
      if (isAdmin.value) {
        await adminLogin();
      } else {
        await login();
      }
    } else {
      return false;
    }
  });
};

let isAdmin = ref(false);
</script>

<template>
  <div class="login-page-wrapper">
    <el-form
        ref="ruleFormRef"
        status-icon
        label-width="120px"
        class="centered-form"
        :model="loginDto"
        :rules="rules"
        title="登录"
    >
      <el-form-item label="用户名" prop="username" class="form-row" required>
        <el-input v-model="loginDto.username"/>
      </el-form-item>
      
      <el-form-item label="密码" prop="password" class="form-row" required>
        <el-input
            v-model="loginDto.password"
            type="password"
            show-password
        />
      </el-form-item>
      
      <div class="button-row">
        <el-form-item>
          <el-button type="primary" @click="submitForm(ruleFormRef)">登录</el-button>
          <el-button type="success" @click="router.push('/register')">注册</el-button>
        </el-form-item>

        <el-switch
            v-model="isAdmin"
            class="mb-2"
            style="--el-switch-on-color: #13ce66; --el-switch-off-color: #fd8250"
            inline-prompt
            size="large"
            active-text="管理员"
            inactive-text="读者"
        />
      </div>
    </el-form>
  </div>
</template>

<style scoped>

.login-page-wrapper {
  position: fixed; 
  top: 0;
  left: 0;
  height: 100vh;
  width: 100vw;
  
  display: flex;
  justify-content: center;
  align-items: center;

  background-image: url("https://wx2.sinaimg.cn/large/005uLWhqgy1hjd2naj3vsj30zu25o7du.jpg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  
  z-index: 1; 
}

.centered-form {
  position: relative; 
  z-index: 10; 

  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 400px;
  height: auto; 
  padding: 40px; 
  margin: 0 auto;
  flex-direction: column;
  
  background: rgba(255, 255, 255, 0.2); 
  border-radius: 12px;
  backdrop-filter: blur(5px); 
}

.form-row {
  width: 100%;
}

.button-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
</style>