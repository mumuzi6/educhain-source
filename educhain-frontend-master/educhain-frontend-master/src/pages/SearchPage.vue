<template>
  <!-- 搜索模式切换 -->
  <van-tabs v-model:active="searchMode" sticky>
    <van-tab title="标签搜索" name="tag">
      <form action="/">
        <van-search
            v-model="searchText"
            show-action
            placeholder="请输入要搜索的标签"
            @search="onSearch"
            @cancel="onCancel"
        />
      </form>
      
      <!-- 标签选择历史记录 -->
      <van-divider content-position="left">历史标签组合</van-divider>
      <div v-if="tagHistory.length === 0" class="empty-history">暂无历史记录</div>
      <div v-else class="history-container">
        <van-swipe :width="300" :show-indicators="false" :loop="false">
          <van-swipe-item v-for="(historyItem, index) in tagHistory" :key="index">
            <div class="history-item">
              <div class="history-tags">
                <van-tag 
                  v-for="tag in historyItem.tags" 
                  :key="tag" 
                  type="primary" 
                  size="medium" 
                  style="margin: 2px"
                >{{ tag }}</van-tag>
              </div>
              <div class="history-actions">
                <van-button size="mini" type="primary" @click="useHistoryTags(historyItem.tags)">使用</van-button>
                <van-button size="mini" plain @click="removeHistory(index)">删除</van-button>
              </div>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>
      
      <!-- 已选标签区域 -->
      <van-divider content-position="left">已选标签</van-divider>
      <div v-if="activeIds.length === 0" class="empty-selected">请选择标签</div>
      <div v-else class="selected-tags-container">
        <div class="selected-tags">
          <van-col v-for="tag in activeIds" :key="tag">
            <van-tag closeable size="medium" type="primary" @close="doClose(tag)" style="margin: 2px">
              {{ tag }}
            </van-tag>
          </van-col>
        </div>
        <div class="clear-tags-btn">
          <van-button 
            size="small" 
            type="danger" 
            icon="delete" 
            @click="clearAllTags"
            class="clear-button"
          >清空所有</van-button>
        </div>
      </div>
      
      <van-divider content-position="left">选择标签</van-divider>
      <div class="tag-selector">
        <van-tree-select
            v-model:active-id="activeIds"
            v-model:main-active-index="activeIndex"
            :items="tagList"
        />
      </div>
      
      <div class="search-button-container">
        <van-button block type="primary" :loading="loading" @click="doSearchResult">
          <template #loading>
            <van-loading type="spinner" />
          </template>
          搜索
        </van-button>
      </div>
    </van-tab>

    <van-tab title="用户名/账号搜索" name="text">
      <van-search
          v-model="usernameSearchText"
          show-action
          placeholder="请输入用户名或账号"
          @search="doTextSearch" 
          @cancel="cancelTextSearch"
      />

      <div class="text-search-tips">
        <p>提示：可输入用户名或用户账号进行搜索</p>
      </div>
      
      <!-- 用户名搜索历史记录 -->
      <template v-if="userSearchHistory.length > 0">
        <van-divider content-position="left">历史搜索</van-divider>
        <div class="username-history">
          <div v-for="(item, index) in userSearchHistory" :key="index" class="history-tag">
            <van-tag
              type="primary" 
              size="medium"
              @click="useHistorySearch(item)"
              plain
            >
              {{ item }}
              <van-icon name="cross" @click.stop="removeUserSearchHistory(index)" />
            </van-tag>
          </div>
        </div>
      </template>
      
      <div class="search-button-container">
        <van-button block type="primary" :loading="loading" @click="doTextSearch">
          <template #loading>
            <van-loading type="spinner" />
          </template>
          搜索
        </van-button>
      </div>
    </van-tab>
  </van-tabs>
</template>

<script setup lang="ts">
import {ref, onMounted, watch} from 'vue';
import {useRouter} from "vue-router";
import { Toast, Dialog } from 'vant';

const router = useRouter()
const loading = ref(false);
const searchText = ref('');
const searchMode = ref('tag');
const usernameSearchText = ref('');
const userSearchHistory = ref<string[]>([]);

// 定义标签数据的接口
interface TagChild {
  text: string;
  id: string;
}

interface TagItem {
  text: string;
  children: TagChild[];
}

interface HistoryItem {
  tags: string[];
  timestamp: number;
}

// 标签历史记录
const tagHistory = ref<HistoryItem[]>([]);

// 默认标签列表（作为备用数据）
const defaultTagList: TagItem[] = [{
  text: '性别',
  children: [
    {text: '男', id: '男'},
    {text: '女', id: '女'},
  ],
},
  {
    text: '年级',
    children: [
      {text: '大一', id: '大一'},
      {text: '大二', id: '大二'},
      {text: '大三', id: '大三'},
      {text: '大四', id: '大四'},
      {text: '研一', id: '研一'},
      {text: '研二', id: '研二'},
      {text: '研三', id: '研三'},
    ],
  },
  {
    text: '编程语言',
    children: [
      {text: 'Java', id: 'Java'},
      {text: 'Python', id: 'Python'},
      {text: 'C++', id: 'C++'},
      {text: 'JavaScript', id: 'JavaScript'},
      {text: 'Go', id: 'Go'},
    ],
  },
  {
    text: '兴趣',
    children: [
      {text: '游戏', id: '游戏'},
      {text: '音乐', id: '音乐'},
      {text: '动漫', id: '动漫'},
      {text: '唱歌', id: '唱歌'},
      {text: '作曲', id: '作曲'},
      {text: '开发', id: '开发'},
      {text: '写作', id: '写作'},
    ],
  },
  {
    text: '运动',
    children: [
      {text: '篮球', id: '篮球'},
      {text: '足球', id: '足球'},
      {text: '羽毛球', id: '羽毛球'},
      {text: '乒乓球', id: '乒乓球'},
      {text: '游泳', id: '游泳'},
    ],
  },
]

// 标签列表
const originTagList = ref<TagItem[]>([...defaultTagList]);
const tagList = ref<TagItem[]>([...defaultTagList]);

// 已选中的标签
const activeIds = ref<string[]>([]);
const activeIndex = ref(0);

// 从本地存储加载历史记录
const loadTagHistory = () => {
  const savedHistory = localStorage.getItem('tagSearchHistory');
  if (savedHistory) {
    try {
      tagHistory.value = JSON.parse(savedHistory);
      // 限制历史记录数量，只保留最近的10条
      if (tagHistory.value.length > 10) {
        tagHistory.value = tagHistory.value.slice(0, 10);
      }
    } catch (e) {
      console.error('加载标签历史记录失败', e);
      tagHistory.value = [];
    }
  }
};

// 从本地存储加载用户搜索历史记录
const loadUserSearchHistory = () => {
  const savedHistory = localStorage.getItem('userSearchHistory');
  if (savedHistory) {
    try {
      userSearchHistory.value = JSON.parse(savedHistory);
      // 限制历史记录数量，只保留最近的10条
      if (userSearchHistory.value.length > 10) {
        userSearchHistory.value = userSearchHistory.value.slice(0, 10);
      }
    } catch (e) {
      console.error('加载用户搜索历史记录失败', e);
      userSearchHistory.value = [];
    }
  }
};

// 保存历史记录到本地存储
const saveTagHistory = () => {
  localStorage.setItem('tagSearchHistory', JSON.stringify(tagHistory.value));
};

// 保存用户搜索历史记录到本地存储
const saveUserSearchHistory = () => {
  localStorage.setItem('userSearchHistory', JSON.stringify(userSearchHistory.value));
};

// 使用历史标签组合
const useHistoryTags = (tags: string[]) => {
  activeIds.value = [...tags];
  Toast('已应用历史标签组合');
};

// 使用历史用户搜索
const useHistorySearch = (text: string) => {
  usernameSearchText.value = text;
  Toast('已应用历史搜索');
};

// 移除历史记录
const removeHistory = (index: number) => {
  Dialog.confirm({
    title: '提示',
    message: '确定要删除这条历史记录吗？',
  }).then(() => {
    tagHistory.value.splice(index, 1);
    saveTagHistory();
    Toast('已删除');
  });
};

// 移除用户搜索历史记录
const removeUserSearchHistory = (index: number) => {
  userSearchHistory.value.splice(index, 1);
  saveUserSearchHistory();
  Toast('已删除');
};

// 添加标签组合到历史记录
const addToHistory = (tags: string[]) => {
  if (tags.length === 0) return;
  
  // 检查是否已存在相同的标签组合
  const sortedTags = [...tags].sort();
  const tagString = sortedTags.join(',');
  
  const exists = tagHistory.value.some(item => 
    [...item.tags].sort().join(',') === tagString
  );
  
  if (!exists) {
    // 新增历史记录，并放在最前面
    tagHistory.value.unshift({
      tags: [...tags],
      timestamp: Date.now()
    });
    
    // 限制历史记录最多10条
    if (tagHistory.value.length > 10) {
      tagHistory.value.pop();
    }
    
    saveTagHistory();
  } else {
    // 如果已存在，将其移到最前面
    const existingIndex = tagHistory.value.findIndex(item => 
      [...item.tags].sort().join(',') === tagString
    );
    
    if (existingIndex > 0) {
      const item = tagHistory.value.splice(existingIndex, 1)[0];
      item.timestamp = Date.now();
      tagHistory.value.unshift(item);
      saveTagHistory();
    }
  }
};

// 添加用户搜索到历史记录
const addUserSearchToHistory = (text: string) => {
  if (!text.trim()) return;
  
  // 如果已存在，先移除
  const index = userSearchHistory.value.findIndex(item => item === text);
  if (index !== -1) {
    userSearchHistory.value.splice(index, 1);
  }
  
  // 添加到最前面
  userSearchHistory.value.unshift(text);
  
  // 限制最多10条记录
  if (userSearchHistory.value.length > 10) {
    userSearchHistory.value.pop();
  }
  
  saveUserSearchHistory();
};

// 在组件挂载时初始化标签数据和加载历史记录
onMounted(async () => {
  // 使用默认标签数据
  originTagList.value = [...defaultTagList];
  tagList.value = [...defaultTagList];
  
  // 加载历史记录
  loadTagHistory();
  loadUserSearchHistory();
});

/**
 * 搜索过滤
 * @param val
 */
const onSearch = (val: string) => {
  if (!val) {
    tagList.value = [...originTagList.value];
    return;
  }
  
  tagList.value = originTagList.value
    .filter(parentTag => 
      // 搜索分类名称
      parentTag.text.includes(val) || 
      // 或者分类中有匹配的子标签
      parentTag.children.some(child => child.text.includes(val))
    )
    .map(parentTag => {
      // 如果分类名称匹配，返回完整的分类
      if (parentTag.text.includes(val)) {
        return {...parentTag};
      }
      // 否则只返回匹配的子标签
    const tempChildren = [...parentTag.children];
    const tempParentTag = {...parentTag};
      tempParentTag.children = tempChildren.filter(item => item.text.includes(val));
    return tempParentTag;
  });
}

const onCancel = () => {
  searchText.value = '';
  tagList.value = [...originTagList.value];
};

const cancelTextSearch = () => {
  usernameSearchText.value = '';
};

// 移除标签
const doClose = (tag: string) => {
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag;
  })
}

/**
 * 清空所有已选标签
 */
const clearAllTags = () => {
  if (activeIds.value.length === 0) return;
  
  Dialog.confirm({
    title: '提示',
    message: '确定要清空所有已选标签吗？',
  }).then(() => {
    activeIds.value = [];
    Toast('已清空所有标签');
  });
}

/**
 * 执行标签搜索
 */
const doSearchResult = () => {
  if (activeIds.value.length === 0) {
    Toast('请至少选择一个标签');
    return;
  }
  
  // 添加到历史记录
  addToHistory([...activeIds.value]);
  
  loading.value = true;
  router.push({
    path: '/user/list',
    query: {
      tags: activeIds.value
    }
  }).finally(() => {
    loading.value = false;
  });
}

/**
 * 执行用户名/账号搜索
 */
const doTextSearch = () => {
  if (!usernameSearchText.value.trim()) {
    Toast('请输入要搜索的用户名或账号');
    return;
  }
  
  // 添加到搜索历史
  addUserSearchToHistory(usernameSearchText.value);
  
  loading.value = true;
  router.push({
    path: '/user/text-search',
    query: {
      searchText: usernameSearchText.value
    }
  }).finally(() => {
    loading.value = false;
  });
}
</script>

<style scoped>
.empty-history, .empty-selected {
  color: #999;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
}

.history-container {
  padding: 0 16px;
  margin-bottom: 10px;
}

.history-item {
  border: 1px solid #ebedf0;
  border-radius: 8px;
  padding: 8px;
  margin-right: 10px;
  width: 290px;
}

.history-tags {
  margin-bottom: 8px;
  min-height: 30px;
}

.history-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.selected-tags-container {
  padding: 0 16px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  flex: 1;
}

.clear-tags-btn {
  margin-left: 8px;
}

.clear-button {
  margin-top: 2px;
}

.tag-selector {
  max-height: 400px;
  overflow-y: auto;
}

.search-button-container {
  padding: 16px;
  position: sticky;
  bottom: 0;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.text-search-tips {
  padding: 10px 16px;
  color: #666;
  font-size: 14px;
  background-color: #f5f7fa;
  margin: 0 16px 16px;
  border-radius: 8px;
}

.username-history {
  padding: 0 16px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  margin-bottom: 5px;
}

/* 移动设备响应式布局 */
@media screen and (max-width: 600px) {
  .history-container {
    padding: 0 8px;
  }
  
  .selected-tags-container {
    padding: 0 8px;
  }
  
  .search-button-container {
    padding: 12px 8px;
  }
  
  .history-item {
    width: 260px;
  }
}
</style>
