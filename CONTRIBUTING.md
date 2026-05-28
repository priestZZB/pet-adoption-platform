# 贡献指南

感谢你考虑为宠物领养救助平台贡献代码! 请花几分钟阅读以下指南。

---

## 分支管理

- `main` — 稳定版本，仅接受来自 `develop` 的合并请求
- `develop` — 日常开发分支
- `feature/<功能名称>` — 新功能开发分支，从 `develop` 拉出
- `hotfix/<修复描述>` — 紧急修复分支，从 `main` 拉出
- `release/<版本号>` — 发布准备分支

## 开发流程

1. 从 `develop` 分支拉取最新代码并创建功能分支
2. 在功能分支上完成开发
3. 确保测试通过，代码符合规范
4. 向 `develop` 分支提交 Pull Request

## Pull Request 规范

### 标题格式

```
<类型>(<范围>): <简短描述>
```

类型:
- `feat` — 新功能
- `fix` — Bug 修复
- `refactor` — 代码重构
- `docs` — 文档更新
- `style` — 代码格式调整
- `test` — 测试相关
- `chore` — 构建/工具链相关

示例:
```
feat(adopt): 添加领养考试自动评分功能
fix(mall): 修复购物车数量为负数的问题
docs(readme): 更新部署说明
```

### PR 描述应包含

- 变更内容
- 变更原因
- 测试方式
- 相关的 Issue 编号 (如有)

## 代码规范

### Java

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 注解简化代码
- Service 层面向接口编程，Controller 层不直接调用 Mapper
- 统一使用 `Result<T>` 作为 API 响应格式
- 业务异常抛出 `BusinessException`，由全局异常处理器统一处理
- 使用 `@Log` 注解记录关键操作日志

### SQL / MyBatis

- Mapper XML 文件放在 `mapper/xml/` 目录下
- 使用驼峰命名自动映射，无需手写 resultMap
- 分页查询统一使用 PageHelper，禁止手动拼接 LIMIT

### Vue 3

- 遵循 Vue 3 组合式 API 风格
- 组件文件使用 PascalCase 命名
- API 请求统一放在 `src/api/` 目录下
- 使用 Pinia 管理全局状态
- 使用 ECharts 时按需导入，避免全量引入

## 提交信息规范

每条提交信息应清晰说明变更内容，建议格式:

```
<类型>: <描述>

- 具体变更 1
- 具体变更 2
```

示例:
```
fix: 修复领养考试分数计算错误

- 修正多选题计分逻辑，漏选不再扣分
- 添加边界值测试用例
```

## 环境配置

开发前请参考 `.env.example` 配置本地环境变量，敏感配置放在 `application-secret.yml` 中（此文件已配置到 .gitignore，不会提交到版本库）。

## 问题反馈

如发现 Bug 或有功能建议，请通过 GitHub Issues 提交，并附上:
- 问题的复现步骤
- 预期的行为与实际行为
- 运行环境信息（操作系统、JDK 版本等）
- 相关的日志或截图
