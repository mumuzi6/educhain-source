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
            { pattern: /^[a-zA-Z0-9_]{4,}$/, message: '用户名至少4个字符，且不能包含特殊字符' }
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
            { pattern: /.{8,}/, message: '密码至少8个字符' }
          ]"
      />
      <van-field
          v-model="checkPassword"
          type="password"
          name="checkPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validatePasswordMatch, message: '两次输入的密码不一致' }
          ]"
      />
      <van-field
          v-model="planetCode"
          name="planetCode"
          label="星球编号"
          placeholder="请输入星球编号"
          :rules="[
            { required: true, message: '请填写星球编号' }
          ]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        注册
      </van-button>
    </div>
    <div style="margin: 16px; text-align: center;">
      <router-link to="/user/login">已有账号? 点击登录</router-link>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import BaseResponse from "../models/baseResponse";

const router = useRouter();
const route = useRoute();

const userAccount = ref('');
const userPassword = ref('');
const checkPassword = ref('');
const planetCode = ref('');

// 验证密码匹配函数
const validatePasswordMatch = (val: string) => val === userPassword.value;

const onSubmit = async () => {
  try {
    const res = await myAxios.post<any, BaseResponse<number>>('/user/register', {
      userAccount: userAccount.value,
      userPassword: userPassword.value,
      checkPassword: checkPassword.value,
      planetCode: planetCode.value
    });
    if (res?.code === 0) {
      Toast.success('注册成功');
      // 注册成功后跳转到登录页
      router.push('/user/login');
    } else {
      Toast.fail(res?.description || '注册失败，请重试');
    }
  } catch (error) {
    console.error('注册错误', error);
    Toast.fail('注册失败，请稍后再试');
  }
};
</script>

<style scoped>

</style> 