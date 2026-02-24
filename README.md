# Resonance
通过一个链接，让所有人实时同步听同一首歌，进度完全一致。  无论相隔多远，都能一起听歌、一起感动。

# 共鸣 Resonance

通过一个链接，让所有人实时同步听同一首歌，进度完全一致。

无论相隔多远，都能一起听歌、一起感动。

## 核心功能

- 生成一个音乐播放链接
- 访问链接的人自动同步房主/播放者的当前歌曲 + 播放进度
- 支持暂停、切歌、拖动进度时所有人同步跟随
- 极简前后端实现，轻量易部署

## 技术栈（示例）

- 前端：React / Vue / Next.js + TailwindCSS
- 后端：Node.js + Express / NestJS / Go / Spring Boot
- 实时同步：WebSocket / Socket.IO / SSE
- 音乐来源：自有上传 / 第三方音乐API / YouTube / 网易云等

## 快速开始

```bash
# 克隆项目
git clone https://github.com/你的用户名/resonance.git

# 进入目录
cd resonance

# 安装依赖（以后端为例）
npm install

# 启动后端
npm run server

# 启动前端
npm run dev
