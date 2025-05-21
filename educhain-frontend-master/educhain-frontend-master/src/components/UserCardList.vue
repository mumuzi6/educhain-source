<template>
  <div class="user-card-list">
    <!-- 正在加载的骨架屏 -->
    <template v-if="props.loading">
      <div class="user-card-skeleton" v-for="i in 3" :key="i">
        <van-skeleton title avatar :row="3" />
      </div>
    </template>
    
    <!-- 用户卡片列表 -->
    <template v-else>
      <div class="user-card-item" v-for="user in props.userList" :key="user.id">
        <van-card
            :desc="user.username ? `${user.username}的个人空间` : '这个人很懒，没有留下简介'"
            :title="`${user.username}（${user.planetCode || '未设置编号'}）`"
            :thumb="user.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
            class="user-card"
        >
          <template #tags>
            <div class="user-tags">
              <van-tag 
                plain 
                type="danger" 
                v-for="(tag, index) in user.tags" 
                :key="index" 
                class="user-tag"
              >
                {{ tag }}
              </van-tag>
            </div>
          </template>
          <template #footer>
            <div class="card-footer">
              <van-button size="small" type="primary" icon="chat-o">联系我</van-button>
              <van-button size="small" icon="star-o">收藏</van-button>
            </div>
          </template>
        </van-card>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {User} from "../models/user";

interface UserCardListProps {
  loading: boolean;
  userList: User[];
}

const props = withDefaults(defineProps<UserCardListProps>(), {
  loading: true,
  // @ts-ignore
  userList: [] as User[],
});
</script>

<style scoped>
.user-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card-item, .user-card-skeleton {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(100, 101, 102, 0.08);
  margin-bottom: 12px;
}

.user-card {
  background-color: #fff;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
}

.user-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.card-footer {
  display: flex;
  gap: 8px;
}

/* 响应式布局 */
@media screen and (max-width: 600px) {
  .user-card-item, .user-card-skeleton {
    margin-bottom: 8px;
  }
  
  .card-footer {
    flex-direction: column;
    gap: 4px;
    align-items: flex-start;
  }
  
  .card-footer .van-button {
    width: 100%;
  }
}

@media screen and (min-width: 768px) {
  .user-card-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }
}
</style>