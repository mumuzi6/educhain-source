<template>
  <div class="team-join-page">
    <van-nav-bar
      title="加入队伍"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    <div class="content">
      <template v-if="loading">
        <van-loading type="spinner" size="24px">加载中...</van-loading>
      </template>
      <template v-else-if="team">
        <div class="team-info">
          <img class="team-cover" :src="team.coverUrl || defaultCover" alt="队伍封面" />
          <h2>{{ team.name }}</h2>
          <div class="team-desc">{{ team.description || '暂无描述' }}</div>
          
          <van-cell-group inset style="margin: 16px 0">
            <van-cell title="队伍类型">
              <template #default>
                <div class="team-type-tag">
                  <van-tag plain type="danger">
                    {{ teamStatusEnum[team.status] }}
                  </van-tag>
                </div>
              </template>
            </van-cell>
            <van-cell title="人数" :value="`${team.hasJoinNum || 0}/${team.maxNum}`" />
            <van-cell v-if="team.expireTime" title="过期时间" :value="formatDate(team.expireTime, 'YYYY年MM月DD日 HH:mm')" />
            <van-cell title="创建时间" :value="formatDate(team.createTime, 'YYYY年MM月DD日 HH:mm')" />
          </van-cell-group>

          <div class="action-bar">
            <van-button 
              round 
              type="primary" 
              block 
              :disabled="joining || team.hasJoin" 
              :loading="joining" 
              @click="handleJoinTeam"
            >
              {{ team.hasJoin ? '已加入' : '加入队伍' }}
            </van-button>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="error-info">
          <van-empty description="队伍不存在或已过期" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Toast, Dialog } from 'vant';
import myAxios from "../plugins/myAxios";
import { TeamType } from "../models/team";
import { teamStatusEnum } from "../constants/team";
import { formatDate } from "../utils/timeUtils";
import ikun from '../assets/ikun.png';

const route = useRoute();
const router = useRouter();
const teamId = route.query.teamId as string;
const inviteCode = route.query.inviteCode as string;
const team = ref<TeamType>();
const loading = ref(true);
const joining = ref(false);
const defaultCover = ikun;

onMounted(async () => {
  if (!teamId || !inviteCode) {
    Toast.fail('邀请链接无效');
    loading.value = false;
    return;
  }
  
  try {
    // 添加时间戳防止缓存
    const timestamp = new Date().getTime();
    const res = await myAxios.get(`/team/${teamId}?inviteCode=${inviteCode}&_t=${timestamp}`) as any;
    if (res?.code === 0 && res.data) {
      team.value = res.data;
    } else {
      Toast.fail(res?.description || '获取队伍信息失败');
    }
  } catch (error) {
    console.error('获取队伍信息失败', error);
    Toast.fail('获取队伍信息失败');
  } finally {
    loading.value = false;
  }
});

const handleJoinTeam = async () => {
  if (joining.value) return;
  
  Dialog.confirm({
    title: '确认加入',
    message: `确定加入 "${team.value?.name}" 队伍吗？`,
  })
    .then(async () => {
      joining.value = true;
      try {
        console.log('发送加入请求，teamId:', teamId, 'inviteCode:', inviteCode);
        const res = await myAxios.post('/team/join', {
          teamId: parseInt(teamId),
          inviteCode
        }) as any;
        
        if (res?.code === 0) {
          Toast.success('加入成功');
          router.push('/team/list/my/join');
        } else {
          Toast.fail('加入失败' + (res.description ? `，${res.description}` : ''));
        }
      } catch (error) {
        console.error('加入队伍失败', error);
        Toast.fail('加入失败');
      } finally {
        joining.value = false;
      }
    })
    .catch(() => {
      // 取消操作
    });
};

const onClickLeft = () => {
  router.push('/team/list');
};
</script>

<style scoped>
.team-join-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding: 16px;
}

.team-info {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  padding: 16px;
  text-align: center;
}

.team-cover {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 16px;
}

.team-desc {
  color: #666;
  margin: 12px 0;
  padding: 0 16px;
}

.action-bar {
  margin-top: 24px;
  padding: 0 16px;
}

.error-info {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 确保标签垂直居中 */
:deep(.van-cell) {
  align-items: center;
  line-height: normal;
  min-height: 44px;
}

:deep(.van-tag) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.team-type-tag {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}

/* 确保所有单元格的值右对齐且垂直居中 */
:deep(.van-cell__value) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  overflow: visible;
}
</style> 