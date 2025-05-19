<template>
  <van-skeleton title avatar :row="3" :loading="props.loading" v-for="user in props.userList" :key="user.id">
    <van-card
        :desc="user.profile || '这个人很懒，没有留下简介'"
        :title="`${user.username}（${user.planetCode || '未设置编号'}）`"
        :thumb="user.avatarUrl"
    >
      <template #tags>
        <van-tag plain type="danger" v-for="(tag, index) in user.tags" :key="index" style="margin-right: 8px; margin-top: 8px">
          {{ tag }}
        </van-tag>
      </template>
      <template #footer>
        <van-button size="mini">联系我</van-button>
      </template>
    </van-card>
  </van-skeleton>
</template>

<script setup lang="ts">
import {UserType} from "../models/user";
import {onMounted, watch} from 'vue';

interface UserCardListProps {
  loading: boolean;
  userList: UserType[];
}

const props = withDefaults(defineProps<UserCardListProps>(), {
  loading: true,
  // @ts-ignore
  userList: [] as UserType[],
});

// 调试: 监控userList变化
watch(() => props.userList, (newValue) => {
  console.log('UserCardList组件接收到的userList:', newValue);
}, { deep: true, immediate: true });

onMounted(() => {
  console.log('UserCardList组件挂载完成，当前userList:', props.userList);
});

</script>

<style scoped>

</style>
