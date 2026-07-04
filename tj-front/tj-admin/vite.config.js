// 项目配置页面
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import svgLoader from "vite-svg-loader";
import vueJsx from "@vitejs/plugin-vue-jsx";
import path from "path";

const CWD = process.cwd();

// 配置参考 https://vitejs.dev/config/
export default defineConfig((mode) => {
  const { VITE_BASE_URL } = loadEnv(mode, CWD);
  return {
    base: "./",
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    plugins: [vue(), vueJsx(), svgLoader()],
    server: {
      port: 18081,
      host: "0.0.0.0",
      proxy: {
        // ============ API 请求全部转发到网关 10010 ============
        '/as': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/us': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/ms': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/cs': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/ds': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/os': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/es': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/rs': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/prs': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/sms': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/ts': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/ais': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/chat': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/file': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        '/session': {
          target: 'http://localhost:10010',
          changeOrigin: true,
        },
        // ============ 静态资源代理 ============
        // /img-tx/** 路由到网关 /ms/**，再由网关转发到 media-service
        '/img-tx': {
          target: 'http://localhost:10010',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/img-tx/, '/ms/files'),
        },
        // /img-minio/** 直连 minio 存储服务
        '/img-minio': {
          target: 'http://192.168.227.128:9000/',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/img-minio/, ''),
        },
      },
    },
  };
});
