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
            :placeholder="addTeamData.expireTime ? String(addTeamData.expireTime) : '点击选择过期时间'"
            @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom">
          <van-datetime-picker
              :model-value="typeof addTeamData.expireTime === 'string' ? new Date(addTeamData.expireTime) : addTeamData.expireTime"
              @update:model-value="(val) => addTeamData.expireTime = val"
              @confirm="showPicker = false"
              type="datetime"
              title="请选择过期时间"
              :min-date="minDate"
          />
        </van-popup>
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

import {useRoute, useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";

const router = useRouter();
const route = useRoute();

// 展示日期选择器
const showPicker = ref(false);
// 图片预览
const showImagePreview = ref(false);
// 默认错误图片
const defaultErrorImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/empty-image-default.png';

const minDate = new Date();

const id = route.query.id;

// 需要用户填写的表单数据，添加必要的类型定义
const addTeamData = ref({
  name: '',
  description: '',
  expireTime: undefined as undefined | Date | string,
  maxNum: 3,
  password: '',
  status: '0',
  coverUrl: '' // 初始化封面URL
});

// 预览图片
const previewImage = () => {
  if (addTeamData.value.coverUrl) {
    showImagePreview.value = true;
  }
}

// 获取之前的队伍信息
onMounted(async () => {
  // 转换id为数字并检查有效性
  const teamId = Number(id);
  if (isNaN(teamId) || teamId <= 0) {
    Toast.fail('加载队伍失败');
    return;
  }
  
  try {
    const res = await myAxios.get("/team/get", {
      params: {
        id: teamId,
      }
    });
    // 使用类型断言处理响应数据
    const data = res as any;
    if (data?.code === 0) {
      addTeamData.value = data.data;
    } else {
      Toast.fail('加载队伍失败，请刷新重试');
    }
  } catch (error) {
    console.error('获取队伍信息失败', error);
    Toast.fail('加载队伍失败，请刷新重试');
  }
});

// 提交
const onSubmit = async () => {
  try {
    const postData = {
      ...addTeamData.value,
      status: Number(addTeamData.value.status)
    }
    // 使用类型断言处理响应数据
    const res = await myAxios.post("/team/update", postData) as any;
    if (res?.code === 0 && res.data){
      Toast.success('更新成功');
      router.push({
        path: '/team',
        replace: true,
      });
    } else {
      Toast.fail('更新失败');
    }
  } catch (error) {
    console.error('更新队伍失败', error);
    Toast.fail('更新失败');
  }
}
</script>

<style scoped>
#teamPage {

}
</style>
