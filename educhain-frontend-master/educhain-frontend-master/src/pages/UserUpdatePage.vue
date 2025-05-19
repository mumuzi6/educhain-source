<template>
  <template v-if="user">
    <van-cell title="昵称" is-link to="/user/edit" :value="user.username"  @click="toEdit('username', '昵称', user.username)"/>
    <van-cell title="账号" :value="user.userAccount"/>
    <van-cell title="头像" is-link @click="toEdit('avatarUrl', '头像', user.avatarUrl || '')">
      <img style="height: 48px" :src="user.avatarUrl"/>
    </van-cell>
    <van-cell title="性别" is-link :value="getGenderText(user.gender)" @click="toEdit('gender', '性别', user.gender)"/>
    <van-cell title="电话" is-link to="/user/edit" :value="user.phone" @click="toEdit('phone', '电话', user.phone)"/>
    <van-cell title="邮箱" is-link to="/user/edit" :value="user.email" @click="toEdit('email', '邮箱', user.email)"/>
    <van-cell title="标签" is-link @click="toEdit('tags', '标签', user.tags || '')">
      <template #value>
        <div class="tags-wrapper">
          <template v-if="userTags && userTags.length > 0">
            <van-tag 
              v-for="(tag, index) in userTags" 
              :key="tag" 
              :type="getTagType(index)"
              :color="tagStyles[index % tagStyles.length].color"
              class="custom-tag"
              round
            >
              {{ tag }}
            </van-tag>
          </template>
          <span v-else class="no-tags">暂无标签</span>
        </div>
      </template>
    </van-cell>
    <van-cell title="星球编号" :value="user.planetCode"/>
    <van-cell title="注册时间" :value="formatDate(user.createTime)"/>
  </template>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {computed, onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../services/user";
import {getGenderText} from "../constants/user";
import {parseTags} from "../utils/tagUtils";
import {formatDate} from "../utils/timeUtils";

type TagType = 'primary' | 'success' | 'danger' | 'warning' | 'default';

interface TagStyle {
  type: TagType | '';
  color: string;
}

// 标签样式配置
const tagStyles: TagStyle[] = [
  { type: 'primary', color: '' },
  { type: 'success', color: '' },
  { type: 'danger', color: '' },
  { type: 'warning', color: '' },
  { type: '', color: '#8e44ad' },  // 紫色
  { type: '', color: '#3498db' },  // 蓝色
  { type: '', color: '#1abc9c' },  // 青绿色
  { type: '', color: '#e67e22' },  // 橙色
];

// 获取标签类型，处理空字符串的情况
const getTagType = (index: number): TagType | undefined => {
  const type = tagStyles[index % tagStyles.length].type;
  return type === '' ? undefined : type;
};

const user = ref();

// 从JSON字符串转换为标签数组
const userTags = computed(() => {
  return parseTags(user.value?.tags);
});

onMounted(async () => {
  user.value = await getCurrentUser();
})

const router = useRouter();

const toEdit = (editKey: string, editName: string, currentValue: string | number) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue,
    }
  })
}
</script>

<style scoped>
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: -2px 0;
}

.custom-tag {
  margin: 2px 0;
  font-size: 13px;
  padding: 2px 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.no-tags {
  color: #999;
  font-size: 14px;
  font-style: italic;
}
</style>