# 公司内部资产管理系统 (Company Internal Asset Management System)

一个基于现代化技术栈构建的企业级固定资产管理系统，旨在帮助企业高效管理固定资产的全生命周期，包括资产入库、借用归还、维修保养、报废处理及采购申请等核心业务流程。

## 🛠 技术栈

- **Frontend**: Vue 3 + Element Plus + Vite + Nginx
- **Backend**: Java Spring Boot + MyBatis Plus
- **Database**: MySQL 8.0 (UTF8MB4 support)
- **Containerization**: Docker & Docker Compose

## 🚀 快速启动 (How to Run)

本项目完全容器化，支持一键启动。

### 前置要求
- 确保已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 并已启动。

### 启动步骤
1. 打开终端（Terminal）或 PowerShell，进入项目根目录。
2. 执行以下命令构建并启动所有服务：

```bash
docker compose up -d --build
```

> **注意**: 首次启动可能需要几分钟时间下载镜像和构建应用，请耐心等待。

3. 检查容器状态：

```bash
docker compose ps
```

确保 `frontend`, `backend`, `mysql` 三个容器的状态均为 `Up` (或 `healthy`)。

## 🌐 Services (服务清单)

启动成功后，您可以通过以下地址访问各服务：

- **前端页面 (Frontend)**: [http://localhost:3000](http://localhost:3000)
- **后端接口 (Backend API)**: [http://localhost:8080/api](http://localhost:8080/api)
- **数据库 (MySQL)**: `localhost:3308` (宿主机映射端口)

## 🧪 测试账号 (Test Accounts)

系统初始化时已预置以下测试账号，密码统一为 `123456`：

| 用户名 | 角色 | 权限说明 |
| :--- | :--- | :--- |
| **admin** | ADMIN | 系统管理员，拥有所有权限 |
| **manager** | MANAGER | 财务/数据管理员，管理资产与报表 |
| **user** | EMPLOYEE | 普通员工，仅限查看和申请 |
| **dev1** | EMPLOYEE | 研发部员工测试账号 |

## 📸 功能介绍

1. **资产管理**: 资产台账的增删改查，支持按分类、部门筛选。
2. **借用管理**: 资产借出与归还流程，实时追踪资产去向。
3. **维修管理**: 记录资产维修详情、费用及状态。
4. **采购申请**: 员工提交资产采购申请，管理员审批。
5. **系统管理**: 用户管理与权限控制。

## ✅ Verification (基本验证方式)

### 方式一：浏览器验证 (推荐)
1. 打开浏览器访问 [http://localhost:3000](http://localhost:3000)。
2. 使用账号 `admin` / `123456` 登录。
3. 若能成功进入首页并看到资产列表数据，说明系统启动正常。

### 方式二：命令行验证 (API Check)
在终端执行以下命令检查后端健康状态：

```powershell
curl -I http://localhost:8080/api/asset/list
```

如果返回 `HTTP/1.1 200` 或 `403` (未登录)，说明后端服务已就绪。

---
**Happy Coding!** 🎉
