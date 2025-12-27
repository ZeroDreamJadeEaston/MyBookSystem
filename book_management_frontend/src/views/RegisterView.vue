<script setup>
import { ref } from "vue";
import router from "@/router";
import { ElMessage } from "element-plus";
import { adminRegisterService, registerService } from "@/methods/register.js";

const isAdmin = ref(false);
const formRef = ref();
let isMan = ref(true);

// 注册表单数据
const registerForm = ref({
  username: "",
  password: "",
  nickname: "",
  gender: "男",
  age: "",
  tel: "",
});

// 管理员邀请码
const inviteCode = ref("");

const adminForm = ref({
  username: "",
  password: "",
  nickname: "",
});

// 校验规则
const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 1, max: 10, message: "长度在 1 到 10 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 10, message: "长度在 6 到 10 个字符", trigger: "blur" },
  ],
  tel: [
    { required: true, message: "请输入电话", trigger: "blur" },
    // 这里只校验是不是11位数字，完全允许重复号码（除非后端报错）
    { pattern: /^\d{11}$/, message: "请输入11位电话号码", trigger: "blur" }
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 1, max: 16, message: "最长16位", trigger: "blur" }
  ],
  age: [
    { required: true, message: "请输入年龄", trigger: "blur" },
    { type: "number", message: "请输入有效数字", trigger: "blur" },
    { pattern: /^(?:[1-9][0-9]?|1[0-9]{2}|200)$/, message: "年龄在 1 到 200 之间", trigger: "blur" }
  ],
};

// 辅助函数：手动等待，解决异步跳转问题
const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// 核心提交逻辑
const submitForm = async () => {
  const form = formRef.value;
  if (!form) return;

  try {
    // 1. 等待前端格式校验（校验不通过会自动停止，跳到 catch）
    await form.validate();

    // 2. 执行注册请求
    if (isAdmin.value) {
      // 管理员需校验邀请码
      if (inviteCode.value !== "20051008") {
        ElMessage({
          message: "管理员邀请码错误，禁止注册！",
          type: 'error',
          appendTo: '.register-page-wrapper'
        });
        return; 
      }
      adminForm.value.username = registerForm.value.username;
      adminForm.value.password = registerForm.value.password;
      adminForm.value.nickname = registerForm.value.nickname;
      
      await adminRegisterService(adminForm.value);
    } else {
      // 读者注册
      registerForm.value.gender = isMan.value ? '男' : '女';
      await registerService(registerForm.value);
    }

    // 3. 注册成功提示
    ElMessage({
      message: "注册成功！正在跳转登录...",
      type: 'success',
      duration: 1000,
      appendTo: '.register-page-wrapper'
    });
    
    // 4. 等待 0.5 秒让用户看清提示
    await wait(500);

    // 5. 携带账号密码跳转到登录页
    router.push({
      path: "/login",
      query: {
          u: registerForm.value.username,
          p: registerForm.value.password
      }
    });

  } catch (err) {
     // 错误处理
     if (err.response) {
        // 如果后端报错例如“用户名已存在”，这里会显示
        // 如果后端允许电话重复，这里就不会报错，注册会成功
        ElMessage({
          message: err.response.data?.msg || "注册失败，请稍后再试",
          type: 'error',
          appendTo: '.register-page-wrapper'
        });
     } else {
        console.warn("表单校验未通过");
     }
  }
};

const goBack = () => {
  router.push("/login");
};
</script>

<template>
  <div class="register-page-wrapper">
    <el-form
      ref="formRef"
      :model="registerForm"
      status-icon
      :rules="rules"
      label-width="80px"
      class="centered-form"
    >
      <h2 style="text-align: center; margin-bottom: 25px; color: #fff; text-shadow: 0 2px 4px rgba(0,0,0,0.8);">注册新用户</h2>

      <el-form-item label="用户名" prop="username" class="form-row">
        <el-input v-model="registerForm.username" placeholder="1-10位字符" />
      </el-form-item>
      
      <el-form-item label="密码" prop="password" class="form-row">
        <el-input
          v-model="registerForm.password"
          type="password"
          show-password
          placeholder="6-10位字符"
        />
      </el-form-item>
      
      <el-form-item label="昵称" prop="nickname" class="form-row">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
      </el-form-item>

      <template v-if="!isAdmin">
          <el-form-item label="电话" prop="tel" class="form-row">
            <el-input v-model="registerForm.tel" placeholder="11位数字" />
          </el-form-item>
          <el-form-item label="年龄" prop="age" class="form-row">
            <el-input v-model.number="registerForm.age" placeholder="请输入年龄" />
          </el-form-item>
      </template>

      <template v-if="isAdmin">
          <el-form-item label="邀请码" class="form-row" required>
            <el-input 
                v-model="inviteCode" 
                placeholder="请输入管理员邀请码" 
                type="password"
                show-password
            />
          </el-form-item>
      </template>
      
      <el-form-item prop="gender" class="form-row" label="身份">
        <el-switch
          v-if="!isAdmin"
          class="mb-2 switch-margin"
          style="--el-switch-on-color: #409eff; --el-switch-off-color: #ff4949;"
          v-model="isMan"
          active-text="男"
          inactive-text="女"
          inline-prompt
          size="large"
        />

        <el-switch
          v-model="isAdmin"
          class="mb-2"
          style="--el-switch-on-color: #13ce66; --el-switch-off-color: #fd8250"
          inline-prompt
          active-text="管理员"
          inactive-text="读者"
          size="large"
        />
      </el-form-item>

      <div class="button-row">
        <el-form-item>
          <el-button type="primary" @click="submitForm" style="width: 120px;">立即注册</el-button>
          <el-button type="info" @click="goBack" style="width: 120px;">返回登录</el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<style scoped>
.register-page-wrapper {
  position: fixed; 
  top: 0;
  left: 0;
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("https://wx3.sinaimg.cn/large/006qH57Cly1hcl984op7aj33y82801l0.jpg");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1; 
}

/* 无框样式 */
.centered-form {
  position: relative; 
  z-index: 10; 
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  flex-direction: column;
}

:deep(.el-form-item__label) {
  color: #fff !important;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0,0,0,0.8);
}

.form-row {
  width: 100%;
  margin-bottom: 20px;
}

.button-row {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 10px;
}

.switch-margin {
    margin-right: 20px;
}
</style>