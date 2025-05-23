<template>
  <div class="chat-list-page">
    <!-- 页面骨架屏 -->
    <template v-if="loading">
      <van-skeleton title :row="3" v-for="i in 5" :key="i" style="margin-bottom: 12px" />
    </template>

    <template v-else>
      <van-nav-bar
        title="我的消息"
        left-arrow
        @click-left="onClickLeft"
        fixed
        class="custom-nav-bar"
      />
      
      <div class="chat-list-content">
        <!-- 未读消息提示 -->
        <div class="unread-badge" v-if="unreadCount > 0">
          <van-badge :content="unreadCount" color="#1989fa" />
          <span>未读消息</span>
        </div>
        
        <!-- 会话列表 -->
        <div v-if="conversations.length === 0" class="empty-conversations">
          <van-empty description="暂无聊天记录" />
        </div>
        
        <van-cell-group class="conversation-list" v-else>
          <van-cell 
            v-for="item in conversations" 
            :key="item.id"
            @click="goToChat(item)"
            clickable
            class="conversation-item"
            :class="{ 'unread': item.isRead === 0 && item.toId === currentUser?.id }"
          >
            <template #icon>
              <div class="avatar-container">
                <van-image 
                  :src="getUserAvatarUrl(item)"
                  alt="头像"
                  @error="handleImageError"
                  class="user-avatar"
                  fit="cover"
                  round
                />
                <div class="unread-dot" v-if="item.isRead === 0 && item.toId === currentUser?.id"></div>
              </div>
            </template>
            <template #title>
              <div class="conversation-header">
                <span class="username">{{ getUserName(item) }}</span>
                <span class="conversation-time">{{ formatTime(item.createTime) }}</span>
              </div>
            </template>
            <template #label>
              <div class="conversation-preview">{{ item.content }}</div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getCurrentUser } from '../services/user';
import { getConversations, getUnreadCount } from '../services/message';
import { formatRelativeTime } from '../utils/timeUtils';

// 定义消息类型接口
interface MessageType {
  id: number;
  fromId: number;
  toId: number;
  fromUsername?: string;
  toUsername?: string;
  fromAvatarUrl?: string;
  toAvatarUrl?: string;
  content: string;
  isRead: number;
  createTime: string | Date;
}

// 定义用户类型接口
interface UserType {
  id: number;
  username?: string;
  avatarUrl?: string;
  [key: string]: any;
}

const router = useRouter();
const loading = ref(true);
const currentUser = ref<UserType | null>(null);
const conversations = ref<MessageType[]>([]);
const unreadCount = ref(0);

// 默认头像
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';

// 返回上一页
const onClickLeft = () => {
  router.back();
};

// 处理图片加载错误
const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  if (target) {
    target.src = defaultAvatar;
  }
};

// 获取用户头像
const getUserAvatarUrl = (item: MessageType) => {
  if (currentUser.value?.id === item.fromId) {
    return item.toAvatarUrl || defaultAvatar;
  }
  return item.fromAvatarUrl || defaultAvatar;
};

// 获取用户名称
const getUserName = (item: MessageType) => {
  if (currentUser.value?.id === item.fromId) {
    return item.toUsername || '未知用户';
  }
  return item.fromUsername || '未知用户';
};

// 格式化时间 - 使用相对时间格式
const formatTime = (time: string | Date) => {
  return formatRelativeTime(time);
};

// 跳转到聊天页面
const goToChat = (item: MessageType) => {
  const userId = currentUser.value?.id === item.fromId ? item.toId : item.fromId;
  router.push({
    path: '/chat',
    query: { userId }
  });
};

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    // 获取当前用户
    const userData = await getCurrentUser();
    currentUser.value = userData;
    
    if (!currentUser.value) {
      Toast.fail('请先登录');
      router.push('/user/login');
      return;
    }
    
    // 获取会话列表
    const conversationsData = await getConversations();
    conversations.value = conversationsData || [];
    
    // 获取未读消息数量
    unreadCount.value = await getUnreadCount();
  } catch (error) {
    console.error('加载数据失败', error);
    Toast.fail('加载数据失败');
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await loadData();
});
</script>

<style scoped>
.chat-list-page {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-top: 46px; /* 导航栏高度 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.custom-nav-bar {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.chat-list-content {
  padding: 10px 0;
}

.unread-badge {
  display: flex;
  align-items: center;
  margin: 0 12px 10px;
  background-color: rgba(25, 137, 250, 0.07);
  padding: 8px 12px;
  border-radius: 6px;
}

.unread-badge span {
  margin-left: 8px;
  font-size: 13px;
  color: #1989fa;
  font-weight: normal;
}

.empty-conversations {
  margin-top: 60px;
}

.conversation-list {
  margin-top: 8px;
  background-color: white;
  overflow: hidden;
}

.conversation-item {
  padding: 12px 12px 12px 12px !important;
  transition: background-color 0.15s ease;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.05) !important;
}

.conversation-item:active {
  background-color: #f5f5f5 !important;
}

.conversation-item.unread {
  background-color: #f0f9ff !important;
}

.avatar-container {
  position: relative;
  margin-right: 12px;
  flex-shrink: 0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f5f5;
}

.unread-dot {
  position: absolute;
  top: -1px;
  right: -1px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #ff4d4f;
  border: 1.5px solid white;
}

.conversation-header {
  display: flex;
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 4px;
  width: 100%;
}

.username {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  line-height: 1.3;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 6px;
}

.conversation-time {
  font-size: 12px;
  font-weight: 400;
  color: #606266;
  white-space: nowrap;
  padding-right: 2px;
  flex-shrink: 0;
}

.conversation-preview {
  font-size: 13px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  text-align: left;
}

/* 覆盖van-cell组件的默认样式 */
:deep(.van-cell__title) {
  flex: 1;
  min-width: 0;
  padding-right: 0;
}

:deep(.van-cell) {
  align-items: flex-start;
  padding: 0;
}
</style> 