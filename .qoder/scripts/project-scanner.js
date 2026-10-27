const fs = require('fs');
const path = require('path');

/**
 * ProjectRadar V4.3 - 全栈语义扫描器 (含保底扫描增强)
 * 核心特性：多语言正则表达式支持、Prompt 幻觉防呆拦截、增量保护与自愈、保底扫描
 * 模板: harness-cli-qoder | 生成: 2026-10-26
 * 双轨/纯 Qoder 自适应: 存在 .claude/plan/ 时输出 .claude/plan/ (双轨共享),
 * 否则输出 .qoder/plan/ (纯 Qoder 项目)
 */
// 输出路径自适应 (双轨: .claude/plan 共享; 纯 Qoder: .qoder/plan)
const IS_DUAL_TRACK = fs.existsSync('.claude/plan');
const RADAR_OUTPUT = IS_DUAL_TRACK ? '.claude/plan/.project_radar.json' : '.qoder/plan/.project_radar.json';
const CONTRACT_DIR = IS_DUAL_TRACK ? '.claude/api-contract' : '.qoder/api-contract';

const CONFIG = {
    output: RADAR_OUTPUT,
    // 排除无关目录，防止污染 AI 索引
    ignore: ['node_modules', '.git', 'dist', 'target', 'build', '.claude', '.qoder', 'venv', '.venv', 'tests', 'assets', 'public'],
    patterns: {
        java: {
            ext: /\.java$/,
            route: /@(Get|Post|Put|Delete|Request)Mapping\s*\(\s*["']([^"']+)["']/g,
            class: /public class (\w+)/,
            desc: /\/\*\*([\s\S]*?)\*\// // 匹配 JSDoc
        },
        vue: {
            ext: /\.(vue|ts|js)$/,
            route: /path:\s*['"]([^'"]+)['"]/g,
            component: /name:\s*['"]([^'"]+)['"]/,
            desc: /|\/\*\*([\s\S]*?)\*\// // 兼容 HTML 注释和 JSDoc
        },
        python: {
            ext: /\.py$/,
            route: /@router\.(get|post|put|delete)\s*\(\s*["']([^"']+)["']/g,
            func: /def\s+(\w+)\s*\(/,
            desc: /(?:"""|''')([\s\S]*?)(?:"""|''')/ // 匹配 Python Docstring
        }
    },
    // 🛡️ [P1-Fix] 配置文件搜索路径 (多路径支持: 纯 Qoder 优先, 双轨备选)
    configPaths: [
        '.qoder/config/project-structure.json',       // 优先路径 (纯 Qoder, init 生成骨架)
        '.claude/config/project-structure.json',       // 备选路径 (双轨, 用户手动配置)
        '.claude/skills/init-wolf/project-structure.json'  // 备选路径 (双轨, init-wolf 输出)
    ]
};

/**
 * 🛡️ [P1-Fix] 加载项目配置 (多路径搜索)
 * 按优先级依次搜索配置文件，找到第一个有效配置即返回
 */
function loadProjectConfig() {
    for (const configRelPath of CONFIG.configPaths) {
        const configPath = path.join(process.cwd(), configRelPath);
        if (fs.existsSync(configPath)) {
            try {
                const config = JSON.parse(fs.readFileSync(configPath, 'utf-8'));
                console.log(`📂 使用配置: ${configRelPath}`);
                return config;
            } catch (e) {
                console.warn(`⚠️ 配置解析失败 (${configRelPath}), 尝试下一个路径`);
            }
        }
    }
    console.log('📂 使用默认配置 (无项目配置文件)');
    return null;
}

// 运行期项目配置缓存 (由 main 流程 loadProjectConfig 后赋值, 契约子目录名动态化)
let __cfg = null;

/**
 * 获取扫描目录 (从配置读取或使用默认)
 */
function getScanDirs(config) {
    if (config && config.stacks) {
        return {
            frontend: config.stacks.frontend?.enabled ? `./${config.stacks.frontend.dir || 'frontend'}` : null,
            java_bff: config.stacks.java?.enabled ? `./${config.stacks.java.dir || 'java-bff'}` : null,
            python_ai: config.stacks.python?.enabled ? `./${config.stacks.python.dir || 'python-ai'}` : null
        };
    }
    // 默认配置
    return {
        frontend: './frontend',
        java_bff: './java-bff',
        python_ai: './python-ai'
    };
}

/**
 * 递归遍历文件 (跨平台)
 */
function getAllFiles(dirPath, arrayOfFiles) {
    if (!fs.existsSync(dirPath)) return arrayOfFiles || [];
    const files = fs.readdirSync(dirPath);
    arrayOfFiles = arrayOfFiles || [];

    files.forEach(file => {
        // 严格排除 ignore 列表中的目录
        if (CONFIG.ignore.some(ig => file === ig || dirPath.includes(path.sep + ig))) return;

        const fullPath = path.join(dirPath, file);
        try {
            if (fs.statSync(fullPath).isDirectory()) {
                arrayOfFiles = getAllFiles(fullPath, arrayOfFiles);
            } else {
                arrayOfFiles.push(fullPath);
            }
        } catch (e) {}
    });
    return arrayOfFiles;
}

/**
 * 🚨 [新增] 保底扫描 - 无注释也扫描
 * 当标准扫描结果不完整时，强制扫描所有 Controller/Router 文件
 */
function fallbackScan(dir, type, config) {
    if (!fs.existsSync(dir)) return [];
    const results = [];
    const files = getAllFiles(dir);
    const pattern = CONFIG.patterns[type];

    if (!pattern) return [];

    files.forEach(file => {
        if (!pattern.ext.test(file)) return;

        try {
            const content = fs.readFileSync(file, 'utf-8');
            const localRoutes = [];

            // 提取路由映射 (仅路由，不依赖注释)
            // 🐛 [修复] 捕获组语义: vue 组1=路径(单组); java/python 组1=方法名, 组2=路径
            // 必须按类型取路径: vue 取组1, java/python 取组2 (组1 || 组2 会误取到方法名)
            const routeRegex = new RegExp(pattern.route);
            let match;
            while ((match = routeRegex.exec(content)) !== null) {
                const route = type === 'vue' ? (match[1] || match[2]) : (match[2] || match[1]);
                if (route) localRoutes.push(route);
            }

            if (localRoutes.length > 0) {
                // 提取类名/函数名作为 identifier
                const identifier = content.match(pattern.class || pattern.component || pattern.func)?.[1] || 'Unknown';

                // 🚨 保底：无注释时使用默认文本
                let summary = `缺少模块注释，请人工或 AI 补充 (文件: ${path.basename(file)})`;

                // 尝试提取注释 (如果有)
                if (pattern.desc) {
                    const commentMatch = content.match(pattern.desc);
                    if (commentMatch && commentMatch[1]) {
                        const rawComment = commentMatch[1] || commentMatch[2] || "";
                        const isPrompt = (rawComment.match(/\{/g) || []).length >= 2 ||
                            /你是|You are|system_prompt/i.test(rawComment);

                        if (!isPrompt && rawComment.trim().length > 10) {
                            summary = rawComment
                                .replace(/\*+/g, '')
                                .split('\n')
                                .map(line => line.trim())
                                .filter(line => line && !line.startsWith('@') && !line.startsWith('!--'))
                                .join(' | ')
                                .slice(0, 200);
                        }
                    }
                }

                // 🚀 [契约链路缝合] 自动生成 contract_file 路径 (双轨/纯 Qoder 自适应 + 端目录动态化)
                let contract_file = null;
                if (type === 'java') {
                    const dir = (__cfg && __cfg.stacks && __cfg.stacks.java && __cfg.stacks.java.dir) || 'java-bff';
                    contract_file = `${CONTRACT_DIR}/${dir}/${identifier}-api.md`;
                } else if (type === 'python') {
                    const dir = (__cfg && __cfg.stacks && __cfg.stacks.python && __cfg.stacks.python.dir) || 'python-ai';
                    contract_file = `${CONTRACT_DIR}/${dir}/${identifier}-api.md`;
                }

                results.push({
                    file: path.relative(process.cwd(), file).replace(/\\/g, '/'),
                    routes: localRoutes,
                    identifier: identifier,
                    summary: summary,
                    contract_file: contract_file
                });
            }
        } catch (e) {}
    });

    return results;
}

/**
 * 🚨 [新增] 合并标准扫描和保底扫描结果
 * 标准扫描结果优先 (带注释的优先)
 */
function mergeScanResults(standardResults, fallbackResults) {
    const merged = new Map();

    // 先加入标准结果 (带注释的优先)
    standardResults.forEach(item => {
        const key = item.file;
        merged.set(key, item);
    });

    // 再加入保底结果 (补充无注释的)
    const warningMsg = "缺少模块注释";
    fallbackResults.forEach(item => {
        const key = item.file;
        if (!merged.has(key)) {
            merged.set(key, item);
        }
        // 如果已存在但保底结果更完整，则覆盖
        else if (merged.get(key).summary?.includes(warningMsg) &&
                 !item.summary?.includes(warningMsg)) {
            merged.set(key, item);
        }
    });

    return Array.from(merged.values());
}

/**
 * 标准扫描函数
 */
function scan(dir, type) {
    if (!fs.existsSync(dir)) return [];
    const results = [];
    const files = getAllFiles(dir);

    files.forEach(file => {
        const pattern = CONFIG.patterns[type];
        if (!pattern.ext.test(file)) return;

        try {
            const content = fs.readFileSync(file, 'utf-8');
            let match;
            const localRoutes = [];

            // 提取路由映射
            // 🐛 [修复] 捕获组语义: vue 组1=路径(单组); java/python 组1=方法名, 组2=路径
            const routeRegex = new RegExp(pattern.route);
            while ((match = routeRegex.exec(content)) !== null) {
                const route = type === 'vue' ? (match[1] || match[2]) : (match[2] || match[1]);
                if (route) localRoutes.push(route);
            }

            if (localRoutes.length > 0) {
                let summary = "";
                if (pattern.desc) {
                    const jsdocMatch = content.match(pattern.desc);
                    if (jsdocMatch) {
                        // 兼容多种捕获组 (Vue 可能落在组 2)
                        const rawComment = jsdocMatch[1] || jsdocMatch[2] || "";

                        // 🚨 [核心防线]：Prompt 特征过滤 (大括号堆叠 或 LLM 常见提示词)
                        const isPrompt = (rawComment.match(/\{/g) || []).length >= 2 ||
                            /你是|You are|system_prompt/i.test(rawComment);

                        if (!isPrompt) {
                            summary = rawComment
                                .replace(/\*+/g, '') // 移除星号
                                .split('\n')
                                .map(line => line.trim())
                                .filter(line => line && !line.startsWith('@') && !line.startsWith('!--'))
                                .join(' | ')
                                .slice(0, 200);
                        } else {
                            console.warn(`⚠️ [雷达警告] 在 ${file} 中提取到了疑似 LLM Prompt，已自动丢弃，请补充文件头部规范注释！`);
                            summary = "缺少模块注释，请人工或 AI 补充";
                        }
                    }
                }

                // 🚀 [契约链路缝合] 自动生成 contract_file 路径 (双轨/纯 Qoder 自适应 + 端目录动态化)
                const identifier = content.match(pattern.class || pattern.component || pattern.func)?.[1] || 'Unknown';
                let contract_file = null;

                if (type === 'java') {
                    const dir = (__cfg && __cfg.stacks && __cfg.stacks.java && __cfg.stacks.java.dir) || 'java-bff';
                    contract_file = `${CONTRACT_DIR}/${dir}/${identifier}-api.md`;
                } else if (type === 'python') {
                    const dir = (__cfg && __cfg.stacks && __cfg.stacks.python && __cfg.stacks.python.dir) || 'python-ai';
                    contract_file = `${CONTRACT_DIR}/${dir}/${identifier}-api.md`;
                }

                results.push({
                    file: path.relative(process.cwd(), file).replace(/\\/g, '/'),
                    routes: localRoutes,
                    identifier: identifier,
                    summary: summary,
                    contract_file: contract_file
                });
            }
        } catch (e) {}
    });
    return results;
}

try {
    console.log('🚀 ProjectRadar V4.3 Starting (Enhanced Fallback Mode)...');

    // 0. 加载项目配置
    const projectConfig = loadProjectConfig();
    __cfg = projectConfig;
    const scanDirs = getScanDirs(projectConfig);
    console.log('📁 Scan directories:', JSON.stringify(scanDirs));

    // 1. 加载旧索引以保护手动修改
    let oldData = { stacks: { frontend: [], java_bff: [], python_ai: [] } };
    if (fs.existsSync(CONFIG.output)) {
        try {
            oldData = JSON.parse(fs.readFileSync(CONFIG.output, 'utf-8'));
        } catch (e) {
            console.warn("⚠️ 旧索引损坏，将全量重新扫描。");
        }
    }

    const outputDir = path.dirname(CONFIG.output);
    if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

    // 2. 执行三端扫描 (标准 + 保底)
    const newStacks = {
        frontend: [],
        java_bff: [],
        python_ai: []
    };

    // Java 端扫描
    if (scanDirs.java_bff) {
        console.log('☕ Scanning Java:', scanDirs.java_bff);
        const standard = scan(scanDirs.java_bff, 'java');
        const fallback = fallbackScan(scanDirs.java_bff, 'java', projectConfig);
        newStacks.java_bff = mergeScanResults(standard, fallback);
        console.log(`   Found ${newStacks.java_bff.length} Java controllers`);
    }

    // Python 端扫描
    if (scanDirs.python_ai) {
        console.log('🐍 Scanning Python:', scanDirs.python_ai);
        const standard = scan(scanDirs.python_ai, 'python');
        const fallback = fallbackScan(scanDirs.python_ai, 'python', projectConfig);
        newStacks.python_ai = mergeScanResults(standard, fallback);
        console.log(`   Found ${newStacks.python_ai.length} Python routers`);
    }

    // 前端扫描
    if (scanDirs.frontend) {
        console.log('💻 Scanning Frontend:', scanDirs.frontend);
        const standard = scan(scanDirs.frontend, 'vue');
        const fallback = fallbackScan(scanDirs.frontend, 'vue', projectConfig);
        newStacks.frontend = mergeScanResults(standard, fallback);
        console.log(`   Found ${newStacks.frontend.length} Vue components`);
    }

    // 3. 增量合并：手动维护的 summary 优先级最高
    const warningMsg = "缺少模块注释，请人工或 AI 补充";
    for (const stack in newStacks) {
        newStacks[stack] = newStacks[stack].map(newItem => {
            const oldItem = oldData.stacks[stack]?.find(o => o.file === newItem.file);
            // 🚨 若旧数据存在、不是警告状态，且新数据质量不如旧数据，则使用旧数据
            if (oldItem && oldItem.summary && oldItem.summary !== warningMsg &&
                (!newItem.summary || newItem.summary.length < oldItem.summary.length)) {
                newItem.summary = oldItem.summary;
            }
            return newItem;
        });
    }

    // 4. 计算全栈血缘 (简单路径片段匹配)
    const lineage = (newStacks.frontend || []).map(f => {
        const possible_api = (newStacks.java_bff || []).filter(j => {
            return j.routes.some(jr => f.routes.some(fr => {
                if (!fr || typeof fr !== 'string') return false;
                const seg = fr.split('/').filter(Boolean).pop();
                return seg && jr.includes(seg);
            }));
        }).map(j => j.file);
        return { view: f.file, possible_api };
    }).filter(l => l.possible_api.length > 0);

    // 5. 持久化写盘
    const data = {
        timestamp: new Date().toISOString(),
        stacks: newStacks,
        lineage,
        config: projectConfig ? { version: projectConfig.version, project: projectConfig.project } : null
    };

    fs.writeFileSync(CONFIG.output, JSON.stringify(data, null, 2));
    console.log(`✅ ProjectRadar Sync Success: ${CONFIG.output}`);
    console.log(`📊 Summary: Java=${newStacks.java_bff.length}, Python=${newStacks.python_ai.length}, Frontend=${newStacks.frontend.length}`);
} catch (err) {
    console.error('❌ Radar Failed:', err.message);
}
