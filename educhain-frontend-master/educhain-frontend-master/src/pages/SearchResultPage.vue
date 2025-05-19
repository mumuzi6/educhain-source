<template>
  <user-card-list :user-list="userList" :loading="loading" />
  <van-empty v-if="!userList || userList.length < 1" description="搜索结果为空" />
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {useRoute} from "vue-router";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import qs from 'qs';
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const {tags} = route.query;

// 调试: 打印接收到的查询参数
console.log('查询参数tags:', tags, typeof tags);

const userList = ref([]);
const loading = ref(true); // 添加loading状态控制

onMounted(async () => {
  // 确保tags是数组
  let tagList = tags;
  if (typeof tags === 'string') {
    tagList = [tags];
  }
  
  // 调试: 打印处理后的tagList
  console.log('处理后的tagList:', tagList);
  
  const userListData = await myAxios.get('/user/search/tags', {
    params: {
      tagNameList: tagList
    },
    paramsSerializer: params => {
      const result = qs.stringify(params, {indices: false});
      console.log('序列化后的请求参数:', result); // 调试: 查看序列化结果
      return result;
    }
  })
      .then(function (response) {
        console.log('/user/search/tags succeed', response);
        console.log('原始响应数据:', JSON.stringify(response)); // 调试: 打印原始响应
        return response?.data;
      })
      .catch(function (error) {
        console.error('/user/search/tags error', error);
        Toast.fail('请求失败');
      })
  console.log('获取到的用户列表数据:', userListData); // 调试: 查看从响应中提取的data
  if (userListData) {
    try {
      userListData.forEach(user => {
        console.log('处理前的用户tags:', user.tags, typeof user.tags); // 调试: 查看tags处理前
        if (user.tags) {
          user.tags = JSON.parse(user.tags);
          console.log('处理后的用户tags:', user.tags); // 调试: 查看tags处理后
        }
      })
      userList.value = userListData;
      console.log('最终设置的userList:', JSON.stringify(userList.value)); // 调试: 最终的数据
    } catch (error) {
      console.error('处理用户数据时出错:', error);
    }
  }
  // 数据加载完成后，将loading设为false
  loading.value = false;
})

</script>

<style scoped>

</style>
