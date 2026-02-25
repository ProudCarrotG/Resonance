import axios from 'axios';

// 创建 axios 实例
const request = axios.create({
    // 使用 Vite 环境变量中的基础地址
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000, // 请求超时时间：10秒
});

// 请求拦截器：可以在请求发送前做一些处理，比如添加 Token
request.interceptors.request.use(
    (config) => {
        // 示例：如果有 token，可以在这里统一携带
        // const token = localStorage.getItem('token');
        // if (token) {
        //   config.headers['Authorization'] = `Bearer ${token}`;
        // }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器：统一处理后端返回的数据和异常
request.interceptors.response.use(
    (response) => {
        const res = response.data;
        // 对应你后端的 ApiResponse 结构：code, message, data
        if (res.code !== 200) {
            // 可以在这里统一弹出错误提示（如引入 Element Plus/Ant Design 的 Message 组件）
            console.error('业务异常:', res.message);
            return Promise.reject(new Error(res.message || 'Error'));
        }
        return res;
    },
    (error) => {
        console.error('网络或服务器异常:', error.message);
        return Promise.reject(error);
    }
);

export default request;