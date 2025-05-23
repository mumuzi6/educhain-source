<template>
  <div class="chat-page">
    <!-- 页面骨架屏 -->
    <template v-if="loading">
      <van-skeleton title avatar :row="3" />
      <van-skeleton title :row="10" style="margin-top: 16px" />
    </template>

    <template v-else-if="otherUser">
      <!-- 聊天头部 -->
      <van-nav-bar
        :title="otherUser.username || '聊天'"
        left-arrow
        @click-left="onClickLeft"
        fixed
      />
      
      <!-- 聊天消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-messages">
          <van-empty description="暂无消息记录，发送一条消息开始聊天吧" />
        </div>
        <template v-else>
          <div 
            v-for="message in messages" 
            :key="message.id" 
            class="message-item"
            :class="{ 'message-self': message.fromId === currentUser?.id }"
          >
            <div class="message-avatar">
              <van-image 
                :src="message.fromId === currentUser?.id ? currentUser.avatarUrl : otherUser.avatarUrl" 
                alt="头像"
                @error="handleImageError"
                fit="cover"
                round
              />
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatMessageTime(message.createTime) }}</div>
            </div>
          </div>
        </template>
      </div>
      
      <!-- 消息输入栏 -->
      <div class="chat-input">
        <van-field
          v-model="messageContent"
          placeholder="请输入消息"
          :border="false"
          autosize
          class="input-field"
          @keydown.enter.prevent="handleSendMessage"
        >
          <template #button>
            <van-button 
              size="small" 
              type="primary" 
              class="send-button" 
              :disabled="!messageContent.trim()" 
              @click="handleSendMessage"
            >
              发送
            </van-button>
          </template>
        </van-field>
      </div>
    </template>
    
    <!-- 用户不存在或出错 -->
    <div v-else class="error-container">
      <van-empty description="用户不存在或无法聊天">
        <van-button round type="primary" to="/">返回首页</van-button>
      </van-empty>
    </div>
    
    <!-- 加载反馈 -->
    <van-overlay :show="isSubmitting" class="loading-overlay">
      <div class="loading-content">
        <van-loading type="spinner" color="#1989fa" />
        <span>发送中...</span>
      </div>
    </van-overlay>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getCurrentUser } from '../services/user';
import { getMessagesByUser, sendMessage, markMessageAsRead } from '../services/message';
import { formatDate } from '../utils/timeUtils';
import myAxios from '../plugins/myAxios';
import type BaseResponse from "../models/baseResponse";

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
  username: string;
  avatarUrl?: string;
  [key: string]: any;
}

const route = useRoute();
const router = useRouter();

// 状态变量
const currentUser = ref<UserType | null>(null);
const otherUser = ref<UserType | null>(null);
const messages = ref<MessageType[]>([]);
const messageContent = ref('');
const loading = ref(true);
const isSubmitting = ref(false);
const messagesContainer = ref<HTMLElement | null>(null);

// 获取路由参数中的用户ID
const userId = route.query.userId ? Number(route.query.userId) : null;

// 默认头像
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg';

// 处理图片加载错误
const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  if (target) {
    target.src = defaultAvatar;
  }
};

// 返回上一页
const onClickLeft = () => {
  router.back();
};

// 格式化消息时间
const formatMessageTime = (time: string | Date) => {
  if (!time) return '';
  return formatDate(time, 'MM-DD HH:mm');
};

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 发送消息
const handleSendMessage = async () => {
  if (!messageContent.value.trim() || !otherUser.value?.id) {
    return;
  }
  
  isSubmitting.value = true;
  try {
    const content = messageContent.value.trim();
    const toId = otherUser.value.id;
    
    const messageId = await sendMessage(toId, content);
    if (messageId) {
      // 清空输入框
      messageContent.value = '';
      
      // 重新加载消息
      await loadMessages();
      
      // 滚动到底部
      scrollToBottom();
    } else {
      Toast.fail('发送失败');
    }
  } catch (error) {
    console.error('发送消息出错', error);
    Toast.fail('发送失败');
  } finally {
    isSubmitting.value = false;
  }
};

// 加载消息
const loadMessages = async () => {
  if (!userId || !currentUser.value) return;
  
  try {
    const data = await getMessagesByUser(userId);
    messages.value = data || [];
    
    // 标记消息为已读
    for (const message of messages.value) {
      if (message.toId === currentUser.value.id && message.isRead === 0) {
        await markMessageAsRead(message.id);
      }
    }
    
    // 滚动到底部
    scrollToBottom();
  } catch (error) {
    console.error('加载消息失败', error);
    Toast.fail('加载消息失败');
  }
};

// 获取用户信息
const loadUserInfo = async () => {
  if (!userId) {
    loading.value = false;
    return;
  }
  
  try {
    // 获取当前登录用户
    const userData = await getCurrentUser();
    currentUser.value = userData;
    
    if (!currentUser.value) {
      Toast.fail('请先登录');
      router.push('/user/login');
      return;
    }
    
    // 获取对方用户信息，使用正确的API路径
    try {
      const res = await myAxios.get(`/user/${userId}`) as BaseResponse<UserType>;
      if (res.code === 0) {
        otherUser.value = res.data;
        
        // 加载消息
        await loadMessages();
      } else {
        Toast.fail('获取用户信息失败');
      }
    } catch (error) {
      console.error('获取对方用户信息失败', error);
      Toast.fail('获取用户信息失败');
    }
  } catch (error) {
    console.error('获取当前用户信息失败', error);
    Toast.fail('获取用户信息失败');
  } finally {
    loading.value = false;
  }
};

// 初始化
onMounted(async () => {
  await loadUserInfo();
});

// 监听路由变化，重新加载数据
watch(() => route.query.userId, async (newVal) => {
  if (newVal && Number(newVal) !== userId) {
    loading.value = true;
    await loadUserInfo();
  }
});
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-top: 46px; /* 导航栏高度 */
  padding-bottom: 60px; /* 底部输入框高度 */
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  min-height: calc(100vh - 106px);
}

.empty-messages {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-self {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 12px;
}

.message-avatar .van-image {
  width: 100%;
  height: 100%;
}

.message-content {
  max-width: 70%;
}

.message-self .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-text {
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #fff;
  word-break: break-word;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.message-self .message-text {
  background-color: #1989fa;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.chat-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 8px 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.input-field {
  background-color: #f5f5f5;
  border-radius: 18px;
  padding: 4px 8px;
}

.send-button {
  margin-left: 8px;
  border-radius: 18px;
}

.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 20px;
  border-radius: 8px;
}
</style> 