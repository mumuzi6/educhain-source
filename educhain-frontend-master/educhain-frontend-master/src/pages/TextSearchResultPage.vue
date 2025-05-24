<template>
  <div class="search-results-container">
    <!-- 搜索条件展示 -->
    <div class="search-criteria">
      <div class="search-text">
        <span class="search-title">搜索关键词:</span>
        <van-tag 
          type="primary" 
          size="medium" 
          style="margin: 2px 4px"
        >{{ searchText }}</van-tag>
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
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const searchText = route.query.searchText as string;

const userList = ref([]);
const loading = ref(true);
const isFirstLoad = ref(true);  // 是否是首次加载
const currentPage = ref(1);
const pageSize = ref(10);
const totalUsers = ref(0);

// 加载用户数据
const loadUserData = async (page = 1) => {
  loading.value = true;
  
  if (!searchText) {
    Toast.fail('搜索关键词不能为空');
    loading.value = false;
    isFirstLoad.value = false;
    return;
  }
  
  try {
    const userListData = await myAxios.get('/user/search/text', {
      params: {
        searchText: searchText,
        pageSize: pageSize.value,
        pageNum: page
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
    console.error('/user/search/text error', error);
    Toast.fail('请求失败，请稍后重试');
    userList.value = [];
  } finally {
    loading.value = false;
    isFirstLoad.value = false;  // 首次加载完成
  }
};

// 页码变化处理
const handlePageChange = (page: number) => {
  loadUserData(page);
};

// 组件挂载时加载数据
onMounted(() => {
  loadUserData(1);
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
  padding: 10px;
  border-radius: 8px;
}

.search-title {
  font-weight: 500;
  margin-right: 5px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-text {
  margin-top: 10px;
  color: #666;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.back-button {
  margin-top: 10px;
}

@media screen and (max-width: 600px) {
  .search-results-container {
    padding: 10px;
  }
}
</style> 