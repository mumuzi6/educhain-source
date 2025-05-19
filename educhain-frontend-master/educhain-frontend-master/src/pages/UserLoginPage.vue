<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[
            { required: true, message: '请填写用户名' },
            { min: 4, message: '用户名至少4个字符' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名不能包含特殊字符' }
          ]"
      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[
            { required: true, message: '请填写密码' },
            { min: 8, message: '密码至少8个字符' }
          ]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";

const router = useRouter();
const route = useRoute();

const userAccount = ref('');
const userPassword = ref('');

const onSubmit = async () => {
  try {
    // 输出用户名和密码用于调试
    console.log('尝试登录:', {
      userAccount: userAccount.value,
      userPassword: userPassword.value
    });
    
    const res = await myAxios.post('/user/login', {
      userAccount: userAccount.value,
      userPassword: userPassword.value,
    })
    console.log(res, '用户登录');
    // 重要：必须同时检查code为0且data不为null
    if (res.code === 0 && res.data) {
      Toast.success('登录成功');
      
      // 登录成功后，获取当前用户信息来验证session是否正常
      try {
        // 先等待一小段时间，确保session已经完全设置好
        await new Promise(resolve => setTimeout(resolve, 500));
        const currentUserRes = await myAxios.get('/user/current');
        if (currentUserRes.code === 0) {
          console.log('成功获取当前用户信息', currentUserRes);
          // 现在可以安全跳转了
          window.location.href = '/';
        } else {
          console.error('获取当前用户信息失败', currentUserRes);
          Toast.fail('登录状态异常，请重试');
        }
      } catch (error) {
        console.error('获取当前用户错误', error);
        Toast.fail('登录状态异常，请重试');
      }
    } else {
      // 如果code=0但data为null，表示用户名密码不匹配
      Toast.fail('用户名或密码不正确，请确认后重试');
      console.log('登录失败详情:', res);
    }
  } catch (error) {
    console.error('登录错误', error);
    Toast.fail('登录失败，请稍后再试');
  }
};

</script>

<style scoped>

</style>
