import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {
      // 关闭 vite-plugin-lazy-import（vxe-table 按需导入优化）：
      // 它重写 import 的方式会破坏 vue-router provide/inject 链，
      // 生产构建下 RouterView 拿不到响应式 route 导致整个内容区白屏
      vxeTableLazyImport: false,
    },
    vite: {
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            // 后端 Controller 已统一 /api 前缀，转发时保留前缀不 rewrite，与合并部署后的同源请求同构
            target: 'http://localhost:8080',
            ws: true,
          },
        },
      },
    },
  };
});
