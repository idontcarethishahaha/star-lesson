export default {
  development: {
    // 开发环境接口请求 - 走 Vite 代理（/as /us /ais /chat /file /session 等全部转发到网关）
    host: '',
    // 开发环境 cdn 路径
    cdn: '',
  },
  test: {
    // 测试环境接口地址
    host: 'http://localhost:10010',
    // 测试环境 cdn 路径
    cdn: '',
  },
  product: {
    // 正式环境接口地址
    host: 'https://tjxt-user-t.itheima.net/api',
    // 正式环境 cdn 路径
    cdn: '',
  },
};
