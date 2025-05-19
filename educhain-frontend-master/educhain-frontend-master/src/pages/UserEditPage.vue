<template>
  <van-form @submit="onSubmit">
      <template v-if="editUser.editKey === 'gender'">
        <van-field name="gender" label="性别">
          <template #input>
            <van-dropdown-menu>
              <van-dropdown-item v-model="genderValue" :options="genderOptions" />
            </van-dropdown-menu>
          </template>
        </van-field>
      </template>
      <template v-else>
        <van-field
            v-model="editUser.currentValue"
            :name="editUser.editKey"
            :label="editUser.editName"
            :placeholder="`请输入${editUser.editName}`"
        />
      </template>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import { ref, computed } from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../services/user";
import {genderEnum} from "../constants/user";

const route = useRoute();
const router = useRouter();

const editUser = ref({
  editKey: route.query.editKey,
  currentValue: route.query.currentValue,
  editName: route.query.editName,
})

// 性别值，确保是数字类型
const genderValue = ref(Number(editUser.value.currentValue) || 0);

// 性别选项
const genderOptions = [
  { text: '男', value: 0 },
  { text: '女', value: 1 },
];

const onSubmit = async () => {
  const currentUser = await getCurrentUser();

  if (!currentUser) {
    Toast.fail('用户未登录');
    return;
  }

  console.log(currentUser, '当前用户')

  // 处理不同字段的值
  const submitValue = editUser.value.editKey === 'gender' 
    ? genderValue.value
    : editUser.value.currentValue;

  const res = await myAxios.post('/user/update', {
    'id': currentUser.id,
    [editUser.value.editKey as string]: submitValue,
  })
  console.log(res, '更新请求');
  if (res.code === 0 && res.data > 0) {
    Toast.success('修改成功');
    router.back();
  } else {
    Toast.fail('修改错误');
  }
};

</script>

<style scoped>

</style>
