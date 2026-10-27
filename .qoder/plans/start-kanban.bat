@echo off
chcp 65001 >nul
rem ============================================
rem  Plan 看板一键启动脚本（V1.2：项目根服务器 + 链路索引自动生成）
rem  用法: 双击本文件 或 在终端运行 start-kanban.bat
rem  效果: 生成链路索引 → 启动本地静态服务器 → 自动打开浏览器
rem ============================================

rem 进入项目根作为服务器根（保证 .qoder 报告与 docs 需求包等全量可达）
cd /d "%~dp0..\.."

echo [1/2] 生成链路索引 (kanban-links.json) ...
where node >nul 2>nul
if %errorlevel%==0 (
    node "%~dp0build-kanban-links.js"
    if errorlevel 1 echo [WARN] 链路索引生成失败（看板链路徽标将不展示）
) else (
    echo [WARN] 未找到 node，跳过索引生成（看板链路徽标将不展示）
)

echo [2/2] 启动本地静态服务器 (http://localhost:8080) ...
start "" "http://localhost:8080/.qoder/plans/kanban.html"

rem 优先使用 python，无 python 时尝试 py launcher
where python >nul 2>nul
if %errorlevel%==0 (
    python -m http.server 8080
) else (
    where py >nul 2>nul
    if %errorlevel%==0 (
        py -m http.server 8080
    ) else (
        echo [ERROR] 未找到 python，请安装 Python 或手动执行:
        echo   cd /d "%~dp0..\.."
        echo   python -m http.server 8080
        pause
    )
)
