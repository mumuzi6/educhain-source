import axios, {AxiosInstance} from "axios";

const isDev = process.env.NODE_ENV === 'development';

const myAxios: AxiosInstance = axios.create({
    // 修改baseURL，确保与服务端域名匹配，避免跨域cookie问题
    baseURL: '/api',
    timeout: 30000,
    withCredentials: true, // 确保跨域请求时携带cookies
});

// 移除默认设置，避免重复
// myAxios.defaults.withCredentials = true; 

// Add a request interceptor
myAxios.interceptors.request.use(function (config) {
    console.log('我要发请求啦', config)
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

// Add a response interceptor
myAxios.interceptors.response.use(function (response) {
    console.log('我收到你的响应啦', response)
    // 未登录则跳转到登录页
    if (response?.data?.code === 40100) {
        // 简化重定向策略，直接跳转到登录页面
        window.location.href = '/user/login';
    }
    // Do something with response data
    return response.data;
}, function (error) {
    // Do something with response error
    return Promise.reject(error);
});

export default myAxios;
