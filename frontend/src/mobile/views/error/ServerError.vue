<template>
  <div class="page">
    <div class="content">
      <div class="code">500</div>
      <h2>服务器错误</h2>
      <p>服务器开小差了，请稍后重试</p>
      <button @click="$router.push('')">返回首页</button>
    </div>
  </div>
</template>

<style scoped>
.page { min-height: 100vh; min-height: 100dvh; display: flex; align-items: center; justify-content: center; background: #f7f1e5; }
.content { text-align: center; padding: 32px; }
.code { font-size: 72px; font-weight: 700; color: #d1e7dd; line-height: 1; margin-bottom: 8px; }
h2 { font-size: 20px; font-weight: 600; color: #5a4a42; margin: 0 0 8px; }
p { font-size: 14px; color: #a09080; margin: 0 0 24px; }
button { height: 44px; padding: 0 32px; border: none; border-radius: 10px; background: linear-gradient(135deg, #c19a6b, #b0895a); color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; }
</style>
