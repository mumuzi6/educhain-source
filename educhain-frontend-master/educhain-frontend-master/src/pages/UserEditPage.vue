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
      <template v-else-if="editUser.editKey === 'tags'">
        <van-field name="tags" label="标签">
          <template #input>
            <div class="tags-container">
              <div class="tags-wrapper">
                <template v-if="tagList.length > 0">
                  <van-tag 
                    v-for="(tag, index) in tagList" 
                    :key="index" 
                    :type="getTagType(index)"
                    :color="tagStyles[index % tagStyles.length].color"
                    class="custom-tag"
                    closeable
                    round
                    @close="removeTag(index)"
                  >
                    {{ tag }}
                  </van-tag>
                </template>
                <div v-else class="no-tags">暂无标签，添加一些吧！</div>
              </div>
              <div class="add-tag">
                <van-field
                  v-model="newTag"
                  placeholder="添加新标签"
                  @keydown.enter.prevent="addTag"
                  class="tag-input"
                >
                  <template #right-icon>
                    <van-icon 
                      name="cross" 
                      class="clear-icon" 
                      v-if="newTag" 
                      @click="newTag = ''" 
                    />
                  </template>
                </van-field>
                <van-button 
                  size="small" 
                  type="primary" 
                  @click="addTag" 
                  :disabled="!newTag.trim()"
                  class="add-button"
                  round
                >
                  添加
                </van-button>
              </div>
              <div class="tag-tips">
                <p>提示：添加标签可以帮助你更好地被志同道合的伙伴找到</p>
              </div>
            </div>
          </template>
        </van-field>
      </template>
      <template v-else-if="editUser.editKey === 'avatarUrl'">
        <van-field name="avatarUrl" label="头像URL">
          <template #input>
            <div class="avatar-container">
              <div class="avatar-preview">
                <img v-if="avatarUrl" :src="avatarUrl" alt="头像预览" />
                <div v-else class="no-avatar">暂无头像</div>
              </div>
              <div class="avatar-actions">
                <van-field
                  v-model="avatarUrl"
                  placeholder="请输入图片URL地址"
                  class="url-input"
                  clearable
                  @input="validateUrl"
                />
                <div class="avatar-tips">
                  <p>请输入有效的图片URL地址，支持jpg, png, gif等常见格式</p>
                  <p>推荐：可使用<a href="https://imgse.com/" target="_blank">图床网站</a>获取图片URL</p>
                </div>
                <div v-if="urlError" class="url-error">
                  {{ urlError }}
                </div>
              </div>
            </div>
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
import { ref, computed, onMounted } from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../services/user";
import {genderEnum} from "../constants/user";
import BaseResponse from "../models/baseResponse";
import {parseTags, stringifyTags} from "../utils/tagUtils";

const route = useRoute();
const router = useRouter();

interface EditUser {
  editKey: string;
  currentValue: string | number;
  editName: string;
}

const editUser = ref<EditUser>({
  editKey: route.query.editKey as string || '',
  currentValue: route.query.currentValue as string || '',
  editName: route.query.editName as string || '',
});

// 性别值，确保是数字类型
const genderValue = ref(Number(editUser.value.currentValue) || 0);

// 性别选项
const genderOptions = [
  { text: '男', value: 0 },
  { text: '女', value: 1 },
];

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

// 标签相关
const newTag = ref('');
const tagList = ref<string[]>([]);

// 头像相关
const avatarUrl = ref('');
const urlError = ref('');

// 初始化表单数据
onMounted(() => {
  // 初始化标签
  if (editUser.value.editKey === 'tags' && editUser.value.currentValue) {
    try {
      const tagsValue = typeof editUser.value.currentValue === 'string' 
        ? editUser.value.currentValue 
        : String(editUser.value.currentValue);
      
      tagList.value = parseTags(tagsValue);
    } catch (error) {
      console.error('解析标签失败', error);
      tagList.value = [];
    }
  }

  // 初始化头像
  if (editUser.value.editKey === 'avatarUrl') {
    avatarUrl.value = editUser.value.currentValue as string;
  }
});

// 添加标签
const addTag = () => {
  const tag = newTag.value.trim();
  if (!tag) return;
  
  if (tagList.value.includes(tag)) {
    Toast('标签已存在');
    return;
  }
  
  tagList.value.push(tag);
  newTag.value = '';
  Toast({
    message: '标签添加成功',
    icon: 'success',
  });
};

// 移除标签
const removeTag = (index: number) => {
  const removedTag = tagList.value[index];
  tagList.value.splice(index, 1);
  Toast({
    message: `已移除标签: ${removedTag}`,
    icon: 'cross',
  });
};

// URL验证
const validateUrl = (url: string) => {
  urlError.value = '';
  
  if (!url) {
    return;
  }
  
  try {
    const urlObj = new URL(url);
    // 检查协议是否为http或https
    if (!['http:', 'https:'].includes(urlObj.protocol)) {
      urlError.value = '请使用http或https开头的URL';
    }
  } catch (e) {
    urlError.value = '请输入有效的URL';
  }
};

const onSubmit = async () => {
  const currentUser = await getCurrentUser();

  if (!currentUser) {
    Toast.fail('用户未登录');
    return;
  }

  // 验证URL
  if (editUser.value.editKey === 'avatarUrl' && avatarUrl.value) {
    validateUrl(avatarUrl.value);
    if (urlError.value) {
      Toast.fail(urlError.value);
      return;
    }
  }

  console.log(currentUser, '当前用户')

  // 处理不同字段的值
  let submitValue;
  if (editUser.value.editKey === 'gender') {
    submitValue = genderValue.value;
  } else if (editUser.value.editKey === 'tags') {
    // 将标签数组转为JSON字符串
    submitValue = stringifyTags(tagList.value);
  } else if (editUser.value.editKey === 'avatarUrl') {
    submitValue = avatarUrl.value;
  } else {
    submitValue = editUser.value.currentValue;
  }

  const res = await myAxios.post<any, BaseResponse<number>>('/user/update', {
    'id': currentUser.id,
    [editUser.value.editKey as string]: submitValue,
  })
  console.log(res, '更新请求');
  if (res?.code === 0 && res?.data > 0) {
    Toast.success('修改成功');
    router.back();
  } else {
    Toast.fail('修改错误');
  }
};

</script>

<style scoped>
.tags-container {
  width: 100%;
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f8f8;
  border-radius: 8px;
}

.custom-tag {
  margin: 4px 0;
  font-size: 13px;
  padding: 4px 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.no-tags {
  color: #999;
  font-size: 14px;
  font-style: italic;
  padding: 10px 0;
  width: 100%;
  text-align: center;
}

.add-tag {
  display: flex;
  margin: 16px 0;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tag-input {
  flex: 1;
  margin-right: 8px;
}

.add-button {
  font-weight: bold;
  padding: 0 16px;
}

.clear-icon {
  padding: 4px;
  color: #999;
}

.tag-tips {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  line-height: 1.5;
  padding: 0 4px;
}

/* 头像样式 */
.avatar-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border: 2px solid #eee;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-avatar {
  color: #999;
  font-size: 14px;
  text-align: center;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.url-input {
  width: 100%;
}

.avatar-tips {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-top: 8px;
  width: 100%;
}

.avatar-tips a {
  color: #1989fa;
  text-decoration: none;
}

.url-error {
  color: #ee0a24;
  font-size: 12px;
  margin-top: 4px;
  text-align: center;
  width: 100%;
}
</style>
