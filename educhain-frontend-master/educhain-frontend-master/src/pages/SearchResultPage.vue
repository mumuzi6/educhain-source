<template>
  <div class="search-results-container">
    <!-- 搜索条件展示 -->
    <div class="search-criteria">
      <div class="search-tags">
        <span class="search-title">搜索标签:</span>
        <van-tag 
          v-for="tag in displayTags" 
          :key="tag" 
          type="primary" 
          size="medium" 
          style="margin: 2px 4px"
        >{{ tag }}</van-tag>
      </div>
      <van-button 
        size="small" 
        plain 
        icon="search" 
        to="/search"
        class="new-search-btn"
      >新搜索</van-button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="isFirstLoad && loading" class="loading-container">
      <van-loading type="spinner" color="#1989fa" size="36px" />
      <p class="loading-text">正在搜索，请稍候...</p>
    </div>
    
    <!-- 用户列表 -->
    <user-card-list :user-list="userList" :loading="!isFirstLoad && loading" />
    
    <!-- 空结果提示 -->
    <van-empty 
      v-if="!loading && (!userList || userList.length < 1)" 
      description="搜索结果为空" 
      image="search"
    >
      <template #bottom>
        <van-button round type="primary" to="/search" class="back-button">返回搜索</van-button>
      </template>
    </van-empty>
    
    <!-- 分页控件 -->
    <div class="pagination-container" v-if="userList && userList.length > 0">
      <van-pagination 
        v-model="currentPage" 
        :total-items="totalUsers" 
        :items-per-page="pageSize"
        :show-page-size="3"
        force-ellipses
        @change="handlePageChange" 
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, computed} from 'vue';
import {useRoute} from "vue-router";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import qs from 'qs';
import UserCardList from "../components/UserCardList.vue";
import type { LocationQueryValue, LocationQueryValueRaw } from 'vue-router';

const route = useRoute();
const routeTags = route.query.tags;

// 处理查询参数为字符串数组
const processTagsParam = (tags: LocationQueryValue | LocationQueryValueRaw): string[] => {
  if (Array.isArray(tags)) {
    return tags.map(tag => String(tag));
  } else if (tags) {
    return [String(tags)];
  }
  return [];
};

// 显示用的标签数组
const displayTags = computed<string[]>(() => {
  return processTagsParam(routeTags);
});

const userList = ref([]);
const loading = ref(true);
const isFirstLoad = ref(true);  // 是否是首次加载
const currentPage = ref(1);
const pageSize = ref(10);
const totalUsers = ref(0);

// 加载用户数据
const loadUserData = async (page = 1) => {
  loading.value = true;
  
  // 确保tags是字符串数组
  const tagList = processTagsParam(routeTags);
  
  try {
    const userListData = await myAxios.get('/user/search/tags/page', {
      params: {
        tagNameList: tagList,
        pageSize: pageSize.value,
        pageNum: page
      },
      paramsSerializer: params => {
        return qs.stringify(params, {indices: false});
      }
    });
    
    if (userListData?.data) {
      // 使用分页数据
      const pageData = userListData.data;
      
      // 处理用户标签数据
      if (pageData.records) {
        pageData.records.forEach((user: any) => {
          if (user.tags) {
            user.tags = JSON.parse(user.tags);
          }
        });
        
        userList.value = pageData.records;
        totalUsers.value = pageData.total;
        currentPage.value = pageData.current;
      } else {
        userList.value = [];
      }
    }
  } catch (error) {
    console.error('/user/search/tags/page error', error);
    Toast.fail('请求失败，请稍后重试');
    userList.value = [];
  } finally {
    loading.value = false;
    isFirstLoad.value = false;  // 首次加载完成
  }
};

// 页码变化处理
const handlePageChange = (page: number) => {
  // 滚动到页面顶部
  window.scrollTo({ top: 0, behavior: 'smooth' });
  
  currentPage.value = page;
  loadUserData(page);
};

onMounted(() => {
  loadUserData();
});

</script>

<style scoped>
.search-results-container {
  padding: 16px;
}

.search-criteria {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background-color: #f7f8fa;
  padding: 8px 12px;
  border-radius: 8px;
}

.search-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  flex: 1;
}

.search-title {
  font-weight: bold;
  margin-right: 8px;
}

.new-search-btn {
  flex-shrink: 0;
  margin-left: 8px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-text {
  margin-top: 16px;
  color: #969799;
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  padding: 10px 0;
}

.back-button {
  margin-top: 16px;
}

/* 响应式布局 */
@media screen and (max-width: 600px) {
  .search-results-container {
    padding: 8px;
  }
  
  .search-criteria {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .new-search-btn {
    margin-top: 8px;
    margin-left: 0;
    align-self: flex-end;
  }
}
</style>
