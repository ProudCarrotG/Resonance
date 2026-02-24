# 后端环境搭建指南

## 环境管理工具选择

### 推荐使用Miniforge
Miniforge是Anaconda的轻量级替代方案，推荐使用：
- 更快的安装速度
- 更小的安装包
- 默认使用conda-forge社区维护的包
- 跨平台支持

#### 安装Miniforge
```bash
# Windows (PowerShell)
curl -O https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-Windows-x86_64.exe
# 或下载安装包后双击安装

# macOS/Linux
curl -L -O "https://github.com/conda-forge/miniforge/releases/latest/download/Miniforge3-$(uname)-$(uname -m).sh"
bash Miniforge3-$(uname)-$(uname -m).sh
```

#### 初始化Miniforge
```bash
# 安装完成后初始化conda
conda init bash
# 或根据shell类型选择
# conda init zsh
# coninit init powershell

# 重启终端或执行
source ~/.bashrc  # Linux/macOS
# 或重启PowerShell (Windows)
```

## 使用Conda创建虚拟环境

### 1. 创建Conda虚拟环境
```bash
# 创建名为resonance的Python环境
conda create -n resonance python=3.11 -y

# 激活环境
conda activate resonance
```

### 2. 升级pip和安装基础工具
```bash
# 升级pip到最新版本
python -m pip install --upgrade pip

# 安装conda-pack（可选，用于环境打包）
conda install conda-pack -y
```

### 3. 安装项目依赖
```bash
# 进入backend目录
cd backend

# 使用pip安装requirements.txt中的依赖
pip install -r requirements.txt
```

### 4. 验证安装
```bash
# 检查Python版本
python --version

# 检查已安装的包
pip list

# 测试FastAPI是否正常安装
python -c "import fastapi; print(f'FastAPI version: {fastapi.__version__}')"

# 测试Socket.IO是否正常安装
python -c "import socketio; print(f'Socket.IO version: {socketio.__version__}')"
```

## 环境管理命令

### 激活/停用环境
```bash
# 激活环境
conda activate resonance

# 停用环境
conda deactivate
```

### 查看环境信息
```bash
# 查看所有环境
conda env list

# 查看当前环境信息
conda info --envs

# 查看环境中安装的包
conda list
```

### 导出/导入环境
```bash
# 导出环境配置（可选）
conda env export > environment.yml

# 从配置文件创建环境（可选）
conda env create -f environment.yml
```

## 常见问题解决

### 1. Conda命令不存在
```bash
# 确保已安装Miniforge或Anaconda
# 添加conda到PATH（根据实际安装路径）

# Miniforge默认路径
# Windows: 
#   C:\Users\用户名\miniforge3\Scripts\conda.exe
#   C:\Users\用户名\miniforge3\condabin\conda.exe
# macOS/Linux:
#   ~/miniforge3/bin/conda
#   ~/miniforge3/condabin/conda

# 临时添加到PATH (Windows PowerShell)
$env:PATH += ";C:\Users\用户名\miniforge3\condabin"

# 临时添加到PATH (Linux/macOS)
export PATH="$HOME/miniforge3/condabin:$PATH"
```

### 2. Miniforge安装问题
```bash
# 如果下载慢，使用国内镜像
# Windows: 手动下载安装包
# https://mirrors.tuna.tsinghua.edu.cn/anaconda/miniforge/

# macOS/Linux: 使用wget下载
wget https://mirrors.tuna.tsinghua.edu.cn/anaconda/miniforge/Miniforge3-Linux-x86_64.sh
```

### 3. pip安装失败
```bash
# 使用国内镜像源
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple/

# 或者配置永久镜像源
pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple/
```

### 4. 权限问题
```bash
# Windows: 以管理员身份运行PowerShell
# macOS/Linux: 使用sudo（不推荐）或配置用户权限
```

## Miniforge vs Anaconda 对比

| 特性 | Miniforge | Anaconda |
|------|-----------|----------|
| 安装包大小 | ~50MB | ~500MB |
| 默认包源 | conda-forge | anaconda |
| 启动速度 | 快 | 较慢 |
| 社区支持 | 社区维护 | 商业支持 |
| 推荐场景 | 开发、轻量使用 | 科学计算、企业使用 |

## 环境迁移

### 导出环境（供其他开发者使用）
```bash
# 激活环境
conda activate resonance

# 导出环境配置
conda env export > environment.yml

# 导出pip依赖（可选）
pip freeze > pip-requirements.txt
```

### 其他开发者导入环境
```bash
# 从environment.yml创建环境
conda env create -f environment.yml

# 激活环境
conda activate resonance

# 如果需要，安装额外的pip依赖
pip install -r pip-requirements.txt
```

## 开发环境启动

### 启动开发服务器
```bash
# 确保在backend目录下
cd backend

# 激活环境
conda activate resonance

# 启动开发服务器
python run.py
```

### 访问地址
- 后端API: http://localhost:8000
- API文档: http://localhost:8000/docs
- Socket.IO: ws://localhost:8000

## IDE配置

### VS Code配置
1. 安装Python扩展
2. 选择Python解释器：`conda activate resonance` 路径下的python
3. 配置launch.json用于调试

### PyCharm配置
1. File → Settings → Project → Python Interpreter
2. 选择已创建的conda环境
3. 配置运行配置

## 依赖包说明

### 核心依赖（第一阶段必需）
- `fastapi==0.104.1` - Web框架
- `uvicorn[standard]==0.24.0` - ASGI服务器
- `python-socketio==5.10.0` - Socket.IO服务器
- `websockets==12.0` - WebSocket支持
- `python-multipart==0.0.6` - 文件上传支持
- `aiofiles==23.2.1` - 异步文件操作

### 第二阶段依赖
- `sqlalchemy==2.0.23` - ORM框架
- `aiosqlite==0.19.0` - 异步SQLite驱动

### 开发工具
- `python-dotenv==1.0.0` - 环境变量管理
- `pydantic==2.5.0` - 数据验证
- `pytest==7.4.3` - 测试框架
- `loguru==0.7.2` - 日志记录

### 音频处理（后期使用）
- `pydub==0.25.1` - 音频处理
- `librosa==0.10.1` - 音频分析
