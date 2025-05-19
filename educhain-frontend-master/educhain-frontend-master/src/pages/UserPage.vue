<template>
  <div class="user-page">
    <!-- 页面骨架屏 -->
    <template v-if="loading">
      <van-skeleton title avatar :row="3" />
      <van-skeleton title :row="2" style="margin-top: 16px" />
      <van-skeleton title :row="2" style="margin-top: 16px" />
    </template>

    <template v-else-if="user">
      <!-- 用户信息卡片 -->
      <div class="user-card" :class="{ 'dark-mode': isDarkMode }">
        <div class="profile-completion-bar">
          <div class="completion-text">资料完整度: {{ profileCompletionPercentage }}%</div>
          <van-progress 
            :percentage="profileCompletionPercentage" 
            :color="profileCompletionColor" 
            track-color="#f5f5f5" 
            pivot-text=""
            stroke-width="4" 
          />
        </div>
        
        <div class="user-card-header">
          <div class="avatar-container">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="用户头像" class="avatar" />
            <div v-else class="avatar-placeholder">
              <van-icon name="contact" size="48" />
            </div>
            <van-icon
              name="photograph"
              class="avatar-edit-button"
              @click="toEdit('avatarUrl', '头像', user.avatarUrl || '')"
            />
          </div>
          <div class="user-info">
            <h2 class="username">{{ user.username || '未设置昵称' }}</h2>
            <p class="user-account">账号: {{ user.userAccount }}</p>
            <div class="user-role-tag">
              <van-tag type="primary" v-if="user.userRole === 1">管理员</van-tag>
              <van-tag type="success" v-else>普通用户</van-tag>
            </div>
            <div class="user-edit-link">
              <van-button size="small" icon="edit" plain hairline round to="/user/update">
                编辑资料
              </van-button>
            </div>
          </div>
        </div>

        <div class="user-details">
          <div class="detail-item" v-if="user.gender !== undefined">
            <van-icon name="contact" />
            <span>性别: {{ genderText }}</span>
            <van-icon name="edit" class="edit-icon" @click="toEdit('gender', '性别', user.gender)" />
          </div>
          <div class="detail-item" v-if="user.phone">
            <van-icon name="phone-o" />
            <span>电话: {{ user.phone }}</span>
            <van-icon name="edit" class="edit-icon" @click="toEdit('phone', '电话', user.phone)" />
          </div>
          <div class="detail-item" v-if="user.email">
            <van-icon name="envelop-o" />
            <span>邮箱: {{ user.email }}</span>
            <van-icon name="edit" class="edit-icon" @click="toEdit('email', '邮箱', user.email)" />
          </div>
          <div class="detail-item" v-if="user.planetCode">
            <van-icon name="medal-o" />
            <span>星球编号: {{ user.planetCode }}</span>
          </div>
          <div class="detail-item join-time">
            <van-icon name="clock-o" />
            <span>加入时间: {{ formattedJoinTime }}</span>
          </div>
        </div>
      </div>

      <!-- 标签卡片 -->
      <div class="card-section" :class="{ 'dark-mode': isDarkMode }">
        <div class="section-header">
          <van-icon name="label-o" />
          <span>我的标签</span>
          <van-button 
            class="action-button" 
            size="mini" 
            icon="edit" 
            plain 
            round
            @click="toEdit('tags', '标签', user.tags || '')"
          >
            编辑
          </van-button>
        </div>
        <div class="section-content">
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
            <div v-else class="no-content">
              <van-empty description="暂无标签，点击编辑添加" />
            </div>
          </div>
        </div>
      </div>

      <!-- 队伍卡片 -->
      <div class="card-section" :class="{ 'dark-mode': isDarkMode }">
        <div class="section-header">
          <van-icon name="friends-o" />
          <span>我的队伍</span>
          <van-button 
            class="action-button" 
            size="mini" 
            icon="search" 
            plain 
            round
            to="/team"
          >
            查找队伍
          </van-button>
        </div>
        <div class="section-content">
          <div class="team-summary" v-if="teamStats">
            <div class="stats-item">
              <span class="stats-number">{{ teamStats.createdTeamsCount }}</span>
              <span class="stats-label">创建的队伍</span>
            </div>
            <div class="stats-item">
              <span class="stats-number">{{ teamStats.joinedTeamsCount }}</span>
              <span class="stats-label">加入的队伍</span>
            </div>
          </div>
          
          <van-grid :column-num="2" :border="false" :gutter="10" class="team-grid">
            <van-grid-item to="/user/team/create" class="team-grid-item">
              <div class="team-action-card created">
                <van-icon name="add-o" size="28" color="#1989fa" />
                <span class="grid-text">创建的队伍</span>
              </div>
            </van-grid-item>
            <van-grid-item to="/user/team/join" class="team-grid-item">
              <div class="team-action-card joined">
                <van-icon name="star-o" size="28" color="#ff9800" />
                <span class="grid-text">加入的队伍</span>
              </div>
            </van-grid-item>
          </van-grid>

          <!-- 最近的队伍展示 -->
          <div class="recent-teams" v-if="recentTeams.length > 0">
            <h3 class="subsection-title">最近的队伍</h3>
            <div class="team-cards-container">
              <router-link
                v-for="team in recentTeams" 
                :key="team.id"
                :to="`/team?id=${team.id}`"
                class="team-card"
              >
                <div class="team-card-header">
                  <span class="team-name">{{ team.name }}</span>
                  <div class="team-status">
                    <van-tag round :type="getTeamStatusType(team.status)">
                      {{ getTeamStatusText(team.status) }}
                    </van-tag>
                  </div>
                </div>
                <div class="team-card-content">
                  <div class="team-description">{{ team.description || '无描述' }}</div>
                  <div class="team-info">
                    <div class="team-members">
                      <van-icon name="friends-o" />
                      <span>{{ team.hasJoinNum }}/{{ team.maxNum }}</span>
                    </div>
                    <div class="team-time" v-if="team.expireTime">
                      <van-icon name="underway-o" />
                      <span>{{ formatExpireTime(team.expireTime) }}</span>
                    </div>
                  </div>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统设置卡片 -->
      <div class="card-section" :class="{ 'dark-mode': isDarkMode }">
        <div class="section-header">
          <van-icon name="setting-o" />
          <span>系统设置</span>
        </div>
        <div class="section-content">
          <van-cell-group inset>
            <van-cell center title="深色模式">
              <template #right-icon>
                <van-switch v-model="isDarkMode" size="24" />
              </template>
            </van-cell>
            <van-cell title="消息通知" is-link to="/notifications" icon="volume-o" />
            <van-cell title="修改信息" is-link to="/user/update" icon="edit" />
            <van-cell title="退出登录" is-link @click="onLogout" icon="close" />
          </van-cell-group>
        </div>
      </div>

      <!-- 返回顶部 -->
      <van-back-top />
  </template>

    <!-- 用户未登录或出错时的展示 -->
    <div v-else class="error-container">
      <van-empty description="获取用户信息失败，请重新登录">
        <van-button round type="primary" to="/user/login">返回登录</van-button>
      </van-empty>
    </div>

    <!-- 加载反馈 -->
    <van-overlay :show="isSubmitting" class="loading-overlay">
      <div class="loading-content">
        <van-loading type="spinner" color="#1989fa" />
        <span>处理中...</span>
      </div>
    </van-overlay>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {computed, onMounted, ref, watch} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast, Dialog} from "vant";
import {getCurrentUser} from "../services/user";
import {parseTags} from "../utils/tagUtils";
import {formatDate, formatTimeToNow, compareDates} from "../utils/timeUtils";

// 状态变量
const user = ref();
const router = useRouter();
const loading = ref(true);
const isSubmitting = ref(false);
const isDarkMode = ref(false);

// 队伍相关
interface Team {
  expireTime: any;
  id: number;
  name: string;
  description: string;
  status: number;
  hasJoinNum: number;
  maxNum: number;
  createTime: string;
}

const recentTeams = ref<Team[]>([]);
const teamStats = ref({
  createdTeamsCount: 0,
  joinedTeamsCount: 0
});

// 标签相关
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

// 从JSON字符串转换为标签数组
const userTags = computed(() => {
  return parseTags(user.value?.tags);
});

// 性别显示
const genderText = computed(() => {
  if (user.value?.gender === 0) {
    return '男';
  } else if (user.value?.gender === 1) {
    return '女';
  } else {
    return '未设置';
  }
});

// 格式化加入时间
const formattedJoinTime = computed(() => {
  if (!user.value?.createTime) {
    return '未知';
  }
  // 使用timeUtils工具函数，自动处理时区问题
  return formatDate(user.value.createTime);
});

// 计算资料完整度
const profileCompletionPercentage = computed(() => {
  if (!user.value) return 0;
  
  const fields = [
    user.value.username,
    user.value.avatarUrl,
    user.value.gender !== undefined,
    user.value.phone,
    user.value.email,
    user.value.tags && userTags.value && userTags.value.length > 0
  ];
  
  const completedFields = fields.filter(Boolean).length;
  return Math.round((completedFields / fields.length) * 100);
});

// 资料完整度颜色
const profileCompletionColor = computed(() => {
  const percentage = profileCompletionPercentage.value;
  if (percentage < 30) return '#ee0a24';
  if (percentage < 60) return '#ff9800';
  if (percentage < 90) return '#07c160';
  return '#1989fa';
});

// 监听深色模式变化
watch(isDarkMode, (newValue) => {
  if (newValue) {
    document.body.classList.add('dark-theme');
    localStorage.setItem('darkMode', 'true');
  } else {
    document.body.classList.remove('dark-theme');
    localStorage.setItem('darkMode', 'false');
  }
});

// 初始化
onMounted(async () => {
  // 恢复深色模式设置
  const savedDarkMode = localStorage.getItem('darkMode');
  if (savedDarkMode === 'true') {
    isDarkMode.value = true;
    document.body.classList.add('dark-theme');
  }

  try {
    // 获取用户信息
  user.value = await getCurrentUser();
    
    // 获取用户队伍数据
    if (user.value) {
      await fetchTeamData();
    }
  } catch (error) {
    console.error('获取用户信息失败', error);
    Toast.fail('获取用户信息失败');
  } finally {
    loading.value = false;
  }
});

// 获取队伍相关数据
const fetchTeamData = async () => {
  try {
    // 获取创建的队伍数量
    const createdTeamsRes = await myAxios.get('/team/list/my/create');
    const createdTeams = createdTeamsRes.data || [];
    
    // 获取加入的队伍数量 
    const joinedTeamsRes = await myAxios.get('/team/list/my/join');
    const joinedTeams = joinedTeamsRes.data || [];
    
    // 更新队伍统计信息
    teamStats.value = {
      createdTeamsCount: createdTeams.length,
      joinedTeamsCount: joinedTeams.length
    };
    
    // 获取最近的队伍（最多4个）
    const allTeams = [...createdTeams, ...joinedTeams];
    
    // 使用timeUtils中的比较函数进行排序，自动处理时区问题
    recentTeams.value = allTeams
      .sort((a, b) => compareDates(a.createTime, b.createTime))
      .slice(0, 4) as Team[];
  } catch (error) {
    console.error('获取队伍数据失败', error);
  }
};

// 跳转到编辑页面
const toEdit = (editKey: string, editName: string, currentValue: string | number) => {
  router.push({
    path: '/user/edit',
    query: {
      editKey,
      editName,
      currentValue: String(currentValue),
    }
  });
};

// 退出登录
const onLogout = () => {
  Dialog.confirm({
    title: '提示',
    message: '确定要退出登录吗？',
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
  })
    .then(async () => {
      isSubmitting.value = true;
      try {
        await myAxios.post('/user/logout');
        // 清除本地缓存
        localStorage.removeItem('darkMode');
        Toast.success('已退出登录');
        router.push('/user/login');
      } catch (error) {
        console.error('退出失败', error);
        Toast.fail('退出失败');
      } finally {
        isSubmitting.value = false;
      }
    })
    .catch(() => {
      // 取消，不做任何操作
    });
};

// 获取队伍状态类型
const getTeamStatusType = (status: number): TagType | undefined => {
  switch(status) {
    case 0: return 'primary';
    case 1: return 'success';
    case 2: return 'warning';
    default: return 'default';
  }
};

// 获取队伍状态文本
const getTeamStatusText = (status: number): string => {
  switch(status) {
    case 0: return '公开';
    case 1: return '私有';
    case 2: return '加密';
    default: return '未知';
  }
};

// 格式化过期时间
const formatExpireTime = (expireTime: string) => {
  // 使用timeUtils工具函数，自动处理时区问题
  return formatTimeToNow(expireTime);
};
</script>

<style scoped>
/* 基础样式 */
.user-page {
  padding: 12px;
  background-color: #f7f8fa;
  min-height: 100vh;
  transition: all 0.3s ease;
}

/* 骨架屏样式 */
.van-skeleton {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 卡片通用样式 */
.user-card, .card-section {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  animation: fadeIn 0.5s ease;
}

/* 资料完整度条 */
.profile-completion-bar {
  margin-bottom: 16px;
}

.completion-text {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

/* 用户卡片头部 */
.user-card-header {
  display: flex;
  align-items: center;
}

.avatar-container {
  position: relative;
  width: 80px;
  height: 80px;
  margin-right: 16px;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.avatar-edit-button {
  position: absolute;
  right: -5px;
  bottom: -5px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #1989fa;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  font-size: 16px;
  z-index: 10;
}

.avatar-edit-button:hover {
  transform: scale(1.1);
}

.user-info {
  flex: 1;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 4px 0;
  color: #323233;
}

.user-account {
  font-size: 14px;
  color: #969799;
  margin: 0 0 8px 0;
}

.user-role-tag {
  margin-bottom: 8px;
}

.user-edit-link {
  margin-top: 8px;
}

/* 用户详细信息 */
.user-details {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed #ebedf0;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  position: relative;
}

.detail-item .van-icon {
  margin-right: 8px;
  color: #1989fa;
}

.detail-item span {
  color: #646566;
  flex: 1;
}

.edit-icon {
  color: #969799;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-icon:hover {
  color: #1989fa;
  transform: scale(1.2);
}

.join-time {
  color: #969799;
  font-size: 12px;
}

/* 卡片部分样式 */
.section-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f7f8fa;
  font-weight: bold;
  color: #323233;
  position: relative;
}

.section-header .van-icon {
  margin-right: 8px;
  color: #1989fa;
}

.action-button {
  position: absolute;
  right: 16px;
}

.section-content {
  padding: 16px;
}

/* 标签样式 */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
}

.custom-tag {
  margin: 2px 0;
  font-size: 13px;
  padding: 4px 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.no-content {
  width: 100%;
  color: #969799;
}

/* 队伍相关样式 */
.team-summary {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #ebedf0;
}

.stats-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stats-number {
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
}

.stats-label {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.team-grid {
  margin: 16px 0;
}

.team-grid-item {
  transition: all 0.3s ease;
}

.team-grid-item:hover {
  transform: translateY(-4px);
}

.team-action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.team-action-card.created {
  background-color: rgba(25, 137, 250, 0.05);
}

.team-action-card.joined {
  background-color: rgba(255, 152, 0, 0.05);
}

.grid-text {
  font-size: 14px;
  color: #646566;
  margin-top: 8px;
}

.subsection-title {
  font-size: 15px;
  font-weight: bold;
  margin: 16px 0 12px;
  color: #323233;
}

.recent-teams {
  margin-top: 20px;
}

.team-cards-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.team-card {
  flex: 1;
  min-width: calc(50% - 12px);
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(100, 101, 102, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  animation: fadeInUp 0.5s;
  border: 1px solid #f0f0f0;
}

.team-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(100, 101, 102, 0.12);
}

.team-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.team-name {
  font-weight: bold;
  font-size: 16px;
  color: #323233;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.team-description {
  color: #646566;
  font-size: 14px;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 42px;
}

.team-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #969799;
}

.team-members, .team-time {
  display: flex;
  align-items: center;
}

.team-members .van-icon, .team-time .van-icon {
  margin-right: 4px;
  font-size: 14px;
}

.dark-mode .team-card {
  background-color: #2c2c2e;
  border-color: #3a3a3c;
}

.dark-mode .team-name {
  color: #f5f5f7;
}

.dark-mode .team-description {
  color: #d1d1d6;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 600px) {
  .team-card {
    min-width: 100%;
  }
}

/* 错误状态 */
.error-container {
  height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 加载状态 */
.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-content {
  padding: 20px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-content span {
  margin-top: 10px;
  color: #323233;
}

/* 深色模式 */
.dark-mode {
  background-color: #1c1c1e;
  color: #f5f5f7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.dark-mode .section-header {
  border-bottom-color: #2c2c2e;
  color: #f5f5f7;
}

.dark-mode .username {
  color: #f5f5f7;
}

.dark-mode .avatar-placeholder,
.dark-mode .join-time,
.dark-mode .stats-label,
.dark-mode .user-account {
  color: #98989d;
}

.dark-mode .detail-item span {
  color: #d1d1d6;
}

.dark-mode .team-action-card.created {
  background-color: rgba(25, 137, 250, 0.1);
}

.dark-mode .team-action-card.joined {
  background-color: rgba(255, 152, 0, 0.1);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 600px) {
  .user-page {
    padding: 8px;
  }
  
  .user-card, .card-section {
    padding: 12px;
  }
  
  .user-card-header {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-container {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .team-summary {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    padding-left: 24px;
  }
  
  .detail-item .van-icon:first-child {
    position: absolute;
    left: 0;
    top: 2px;
  }
  
  .detail-item span {
    margin: 4px 0;
  }
  
  .edit-icon {
    position: absolute;
    right: 0;
    top: 2px;
  }
}
</style>
