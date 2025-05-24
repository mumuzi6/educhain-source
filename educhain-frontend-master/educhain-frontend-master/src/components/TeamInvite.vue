<template>
  <div>
    <van-button 
      v-if="team.status === 1 && isCreator" 
      type="primary" 
      size="small" 
      @click="handleShowInvite"
    >
      邀请好友
    </van-button>
    
    <van-dialog v-model:show="showInviteDialog" title="邀请好友加入队伍">
      <div class="invite-content">
        <p class="tips">复制以下文本分享给好友：</p>
        <div class="invite-text">
          <p>【队伍邀请】</p>
          <p>我邀请你加入"{{ team.name }}"队伍</p>
          <p>点击链接加入:</p>
          <p class="invite-link">{{ inviteLink }}</p>
        </div>
        <van-button block type="primary" @click="copyInviteText">复制邀请文本</van-button>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { Toast } from 'vant';
import myAxios from '../plugins/myAxios';
import { getCurrentUser } from '../services/user';
import type { TeamType } from '../models/team';

interface TeamInviteProps {
  team: TeamType;
}

const props = defineProps<TeamInviteProps>();
const showInviteDialog = ref(false);
const inviteLink = ref('');
const currentUser = ref();
const inviteCode = ref('');
const inviteText = ref('');

// 判断当前用户是否是创建者
const isCreator = computed(() => {
  return currentUser.value?.id === props.team.userId;
});

// 初始化获取当前用户
const init = async () => {
  currentUser.value = await getCurrentUser();
};
init();

// 显示邀请对话框
const handleShowInvite = async () => {
  try {
    // 获取邀请码
    const res = await myAxios.get(`/team/invite-code/${props.team.id}`) as any;
    if (res?.data && res?.code === 0) {
      inviteCode.value = res.data;
      // 生成完整的邀请链接
      const baseUrl = window.location.origin;
      inviteLink.value = `${baseUrl}/team/join?teamId=${props.team.id}&inviteCode=${inviteCode.value}`;
      showInviteDialog.value = true;
    } else {
      Toast.fail('获取邀请码失败');
    }
  } catch (error) {
    console.error('获取邀请码失败', error);
    Toast.fail('获取邀请码失败');
  }
};

// 复制邀请文本到剪贴板
const copyInviteText = () => {
  const text = `【队伍邀请】
我邀请你加入"${props.team.name}"队伍
点击链接加入: 
${inviteLink.value}`;

  navigator.clipboard.writeText(text)
    .then(() => Toast.success('邀请文本已复制'))
    .catch(() => Toast.fail('复制失败，请手动复制'));
};
</script>

<style scoped>
.invite-content {
  padding: 16px;
}

.tips {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.invite-text {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
  word-break: break-all;
  font-size: 14px;
  color: #333;
  border: 1px solid #eee;
}

.invite-text p {
  margin: 5px 0;
}

.invite-link {
  word-break: break-all;
  color: #1989fa;
  font-weight: bold;
}
</style> 