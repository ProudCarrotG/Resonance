<script setup>
import { ref } from 'vue';
import { testPing } from './api/system';

const backendMessage = ref('还没有请求后端');

const handleTestPing = async () => {
  try {
    const res = await testPing();
    
    // 注意：在 request.js 的响应拦截器中，我们已经返回了 res.data
    // 这里的 res 直接对应后端 ApiResponse 的结构 { code: 200, message: "...", data: "pong" }
    backendMessage.value = `接口通信成功！核心数据：${res.data}`;
    
  } catch (error) {
    // 统一的错误处理兜底
    console.error('测试接口失败:', error);
    backendMessage.value = '请求失败，请打开 F12 查看控制台日志';
  }
};
</script>

<template>
  <div style="padding: 20px; font-family: sans-serif;">
    <h1>🎵 Resonance / 共鸣</h1>
    <button @click="handleTestPing" style="padding: 10px; cursor: pointer;">
      测试后端 (企业级 Axios)
    </button>
    
    <div v-if="backendMessage" style="margin-top: 20px; color: #4CAF50;">
      <strong>响应结果：</strong>{{ backendMessage }}
    </div>
  </div>
</template>

<style scoped>
</style>