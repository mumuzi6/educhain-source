<template>
  <div
      id="teamCardList"
  >
    <van-card
        v-for="team in props.teamList"
        :thumb="team.coverUrl || teamDefaultImage"
        :desc="team.description"
        :title="`${team.name}`"
    >
      <template #tags>
        <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
          {{
            teamStatusEnum[team.status]
          }}
        </van-tag>
      </template>
      <template #bottom>
        <div>
          {{ `队伍人数: ${team.hasJoinNum || 0}/${team.maxNum}` }}
        </div>
        <div v-if="team.expireTime">
          {{ '过期时间: ' + formatDate(team.expireTime, 'YYYY年MM月DD日 HH:mm') }}
        </div>
        <div>
          {{ '创建时间: ' + formatDate(team.createTime, 'YYYY年MM月DD日 HH:mm') }}
        </div>
      </template>
      <template #footer>
        <van-button size="small" type="primary" v-if="team.userId !== currentUser?.id && !team.hasJoin" plain
                    @click="preJoinTeam(team)">
          加入队伍
        </van-button>
        <van-button v-if="team.userId === currentUser?.id" size="small" plain
                    @click="doUpdateTeam(team.id)">更新队伍
        </van-button>
        <!-- 仅加入队伍可见 -->
        <van-button v-if="team.userId !== currentUser?.id && team.hasJoin" size="small" plain
                    @click="doQuitTeam(team.id)">退出队伍
        </van-button>
        <van-button v-if="team.userId === currentUser?.id" size="small" type="danger" plain
                    @click="doDeleteTeam(team.id)">解散队伍
        </van-button>
        <!-- 添加邀请功能按钮 -->
        <team-invite v-if="team.userId === currentUser?.id && team.status === 1" :team="team" />
      </template>
    </van-card>
    <van-dialog v-model:show="showPasswordDialog" title="请输入密码" show-cancel-button @confirm="doJoinTeam" @cancel="doJoinCancel">
      <van-field v-model="password" placeholder="请输入密码"/>
    </van-dialog>
  </div>

</template>

<script setup lang="ts">
import {TeamType} from "../models/team";
import {teamStatusEnum} from "../constants/team";
import ikun from '../assets/ikun.png';
import myAxios from "../plugins/myAxios";
import {Dialog, Toast} from "vant";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user";
import {useRouter} from "vue-router";
import {formatDate} from "../utils/timeUtils";
import TeamInvite from "./TeamInvite.vue";

interface TeamCardListProps {
  teamList: TeamType[];
}

const props = withDefaults(defineProps<TeamCardListProps>(), {
  // @ts-ignore
  teamList: [] as TeamType[],
});

const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref(0);
const currentUser = ref();

const router = useRouter();


const teamDefaultImage = ikun;

onMounted(async () => {
  currentUser.value = await getCurrentUser();
})

const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}

const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 加入队伍
 */
const doJoinTeam = async () => {
  if (!joinTeamId.value) {
    return;
  }
  try {
    const res = await myAxios.post('/team/join', {
      teamId: joinTeamId.value,
      password: password.value
    }) as any;
    if (res?.code === 0) {
      Toast.success('加入成功');
      doJoinCancel();
    } else {
      Toast.fail('加入失败' + (res.description ? `，${res.description}` : ''));
    }
  } catch (error) {
    console.error('加入队伍失败', error);
    Toast.fail('加入失败');
  }
}

/**
 * 跳转至更新队伍页
 * @param id
 */
const doUpdateTeam = (id: number) => {
  router.push({
    path: '/team/update',
    query: {
      id,
    }
  })
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  try {
    const res = await myAxios.post('/team/quit', {
      teamId: id
    }) as any;
    if (res?.code === 0) {
      Toast.success('操作成功');
    } else {
      Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
    }
  } catch (error) {
    console.error('退出队伍失败', error);
    Toast.fail('操作失败');
  }
}

/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  try {
    const res = await myAxios.post('/team/delete', {
      id,
    }) as any;
    if (res?.code === 0) {
      Toast.success('操作成功');
    } else {
      Toast.fail('操作失败' + (res.description ? `，${res.description}` : ''));
    }
  } catch (error) {
    console.error('解散队伍失败', error);
    Toast.fail('操作失败');
  }
}

</script>

<style scoped>
#teamCardList :deep(.van-image__img) {
  height: 128px;
  object-fit: unset;
}
</style>
