<template>
  <div id="teamAddPage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名"
            placeholder="请输入队伍名"
            :rules="[{ required: true, message: '请输入队伍名' }]"
        />
        <van-field
            v-model="addTeamData.description"
            rows="4"
            autosize
            label="队伍描述"
            type="textarea"
            placeholder="请输入队伍描述"
        />
        <van-field
            v-model="addTeamData.coverUrl"
            name="coverUrl"
            label="封面图片"
            placeholder="请输入封面图片URL"
        >
          <template #extra v-if="addTeamData.coverUrl">
            <van-image
              width="50"
              height="50"
              :src="addTeamData.coverUrl"
              @click.stop="previewImage"
              :error="defaultErrorImage"
            />
          </template>
        </van-field>
        <van-field
            is-link
            readonly
            name="datetimePicker"
            label="过期时间"
            :placeholder="addTeamData.expireTime ?? '点击选择过期时间'"
            @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom">
          <van-datetime-picker
              v-model="addTeamData.expireTime"
              @confirm="showPicker = false"
              type="datetime"
              title="请选择过期时间"
              :min-date="minDate"
          />
        </van-popup>
        <van-field name="stepper" label="最大人数">
          <template #input>
            <van-stepper v-model="addTeamData.maxNum" max="10" min="3"/>
          </template>
        </van-field>
        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status" direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
            v-if="Number(addTeamData.status) === 2"
            v-model="addTeamData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入队伍密码"
            :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
    <van-image-preview v-model:show="showImagePreview" :images="[addTeamData.coverUrl]" />
  </div>
</template>

<script setup lang="ts">

import {useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";

const router = useRouter();
// 展示日期选择器
const showPicker = ref(false);
// 图片预览
const showImagePreview = ref(false);
// 默认错误图片
const defaultErrorImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/empty-image-default.png';

const initFormData = {
  "name": "",
  "description": "",
  "expireTime": null,
  "maxNum": 3,
  "password": "",
  "status": 0,
  "coverUrl": "", // 添加封面URL字段
}

const minDate = new Date();

// 需要用户填写的表单数据
const addTeamData = ref({...initFormData})

// 预览图片
const previewImage = () => {
  if (addTeamData.value.coverUrl) {
    showImagePreview.value = true;
  }
}

// 提交
const onSubmit = async () => {
  const postData = {
    ...addTeamData.value,
    status: Number(addTeamData.value.status)
  }
  try {
    // 使用类型断言处理响应数据
    const res = await myAxios.post("/team/add", postData) as any;
    if (res?.code === 0 && res.data){
      Toast.success('添加成功');
      router.push({
        path: '/team',
        replace: true,
      });
    } else {
      Toast.fail('添加失败');
    }
  } catch (error) {
    console.error('添加队伍失败', error);
    Toast.fail('添加失败');
  }
}
</script>

<style scoped>
#teamPage {

}
</style>
