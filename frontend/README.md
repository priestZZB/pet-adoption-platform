# 宠物领养救助平台 — 前端

宠物领养救助平台的前端工程，基于 Vue 3 + Vite 构建。

## 技术栈

- Vue 3 (组合式 API)
- Vite (构建工具)
- Vue Router (路由)
- Pinia (状态管理)
- ECharts (数据可视化)
- ESLint (代码规范)

## 启动

```sh
npm install
npm run dev
```

## 构建

```sh
npm run build
```

构建产物输出到 `dist/` 目录。

## 目录结构

```
src/
├── api/              # API 接口封装
├── assets/styles/    # 全局样式
├── components/       # 公共组件
├── composables/      # 组合式函数
├── layouts/          # 页面布局
├── router/           # 路由配置
├── stores/           # Pinia 状态管理
├── utils/            # 工具函数
└── views/            # 页面视图
    ├── admin/        # 管理后台
    ├── adopt/        # 领养流程
    ├── ai/           # AI 助手
    ├── auth/         # 登录 / 注册
    ├── chat/         # 即时通讯
    ├── donate/       # 送养
    ├── home/         # 首页
    ├── mall/         # 商城
    ├── notice/       # 公告
    ├── pet/          # 宠物
    ├── user/         # 用户中心
    └── volunteer/    # 志愿者
```

## 开发须知

- API 请求统一在 `src/api/` 中封装，返回 Promise，组件中直接调用
- 全局状态统一使用 Pinia，避免组件间层层传参
- 组件名使用 PascalCase，文件名使用 kebab-case
- 路由配置文件为 `src/router/index.js`，懒加载页面组件
