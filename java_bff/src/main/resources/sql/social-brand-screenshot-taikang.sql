-- =============================================================
-- 雇主品牌雷达 · 截图批量识别入库（第 6 家：泰康保险集团 company_id=6）
-- 来源：20 张小红书笔记截图 + 配套官方 JD / 宣传长图（共 4 条独立笔记）
-- 约定：与既有 1-5 家一致 —— source_type=MANUAL_SCREENSHOT，
--       note_url=screenshot://xhs/{companyId}/{hash}（无直链时占位，前端降级显示文本）
-- 清洗：主题标签仅取 SocialBrandService.TOPIC_TAGS 白名单；归属类型 LLM 判定
-- 幂等：note_url 唯一键 + ON DUPLICATE KEY UPDATE；快照按 metric_source 防重复
-- 执行：cmd /c "mysql -uroot -p*** --default-character-set=utf8mb4 < social-brand-screenshot-taikang.sql"
-- 日期：2026-10-27
-- =============================================================

USE sunshine;

SET @now = NOW();

-- -------------------------------------------------------------
-- 1. 笔记主体（4 条）
-- -------------------------------------------------------------
INSERT INTO social_note
(platform, note_url, title, content_summary, published_at,
 first_observed_at, last_observed_at, company_id, account_id,
 owner_type, source_type, verification_status, topic_tags, missing_reason,
 clean_source, clean_basis, cleaned_at, remark, created_at, updated_at)
VALUES
-- A. 多企业产品经理岗汇总（泰康人寿为第 1 家，配套 JD 页 1/6-4/6）
('XIAOHONGSHU', 'screenshot://xhs/6/b628767407a9', '今天投这5个产品经理校招岗',
 '博主汇总 10 月中旬开启的 5 个产品经理类校招岗：①泰康人寿科技创新中心-产品交付（北京，需求部门人寿总公司，JD 编号 J89169，发布于 2026-09-10，招 7 人、HC 在同类岗位中偏多；职责为产品整体规划、需求分析、功能设计与迭代优化，与研发/设计/运营协同推动从概念到上线，做市场调研与竞品分析并制定产品路线图）；②春秋航空-IT 产品类培训生（上海，本科及以上，管理类/计算机类/统计类相关专业，做内部 IT 产品与业务系统，行业信息收集、业务流程与系统规划）；③Manteia 数据科技-产品经理初级/中级（厦门，医学信息与医学 AI 产品，含需求分析、方案设计、验收与交付培训）；④隆基绿能-储能产品工程师（工商储，苏州）；⑤阿特拉斯·科普柯集团-毕业生项目（业务流程优化/数字化转型/AI 应用，天津）。作者点评：泰康是保险+医养大健康生态，场景丰富，AI 含量不高但保险数据与智能应用有空间，业务复杂合规严、产品交付偏项目制；春秋航空属传统企业 IT 入门岗，航空 IT 节奏与互联网不同、AI 非重点。',
 NULL, @now, @now, 6, NULL,
 'USER_DISCUSSION', 'MANUAL_SCREENSHOT', 'PENDING', '校园招聘,岗位与专业要求', NULL,
 'LLM', 'LLM 截图解析', @now,
 '截图录入；1/6-4/6 为配套 JD 页（泰康人寿 J89169、春秋航空、Manteia 初级/中级），另含泰康段落正文特写', @now, @now),

-- B. 泰康资产 27 届尖子班岗位锐评
('XIAOHONGSHU', 'screenshot://xhs/6/eae2488abd2f', '锐评泰康资产27校招尖子班各部门和岗位',
 '逐条锐评泰康资产 2027 届校招「尖子班」各部门与岗位：①投研类（固收/信用/资产配置/产品研发）为核心板块，险资长钱属性决定追求收益与风控优先，固收、信用是传统强项且项目资源足，资产配置能接触大类资产跨品类框架，但权益研究盘子不及头部公募；优点是资金久期长、不必被短期净值追着跑，短板是风格偏审慎，缺扎实固收/配置实习易在初筛被卡。②交易类（权益/固收交易）：固收交易存在感更强、可深度参与头寸与流动性研判，权益交易多按指令执行、以优化冲击成本为主，险资交易整体偏执行导向。③市场类：机构销售依托养老金、企业年金资源、客户底子厚但拓客周期长；账户管理偏后台对接与流程维护，稳定压力小但业务纵深有限。④风控类（风险管理、法律事务）是险资管天然优势赛道，制度体系完善、履历含金量足，缺点是条线分工细、易局限在单一模块。⑤科技类（数据科学/软件工程/数据开发）近年加码 AI 与数据方向，但科技团队是业务支撑部门、非独立量化团队，直接做策略开发的机会不多。⑥运营与支持（运营管理、投资支持、人力资源）稳定性突出、流程成熟、加班相对可控。',
 NULL, @now, @now, 6, NULL,
 'USER_DISCUSSION', 'MANUAL_SCREENSHOT', 'PENDING', '岗位与专业要求,职业发展', NULL,
 'LLM', 'LLM 截图解析', @now,
 '截图录入；配套泰康资产 2027 届校招宣传长图 1/3-2/3（开放岗位清单：投研/科技/市场/风控/交易/运营与支持）；截图显示发布 09-17，共 9 条评论', @now, @now),

-- C. 泰康资产测评与面试全流程（截图未露出标题与互动数据）
('XIAOHONGSHU', 'screenshot://xhs/6/e93fa881fc56', '泰康资产27届面试全流程拆解（一面/群面/实习/答辩/终面）',
 '拆解泰康资产 27 届（含暑期实习转正）测评与面试全流程，按年份略有差异、单面群面混合进行：①一面含自我介绍+单问题作答（限时约 1 分钟，需快速讲清个人差异化经历）、热词解释（随机抽词，1-2 分钟准备后即兴开讲，往年考过首发经济、低空经济、考公热、单身经济，也涉新型保险产品、政策制度与重大事件）、群面（无领导小组讨论，题目偏开放，如「关税对大类资产的影响」，建议观点有逻辑有深度并结合材料）、领导提问（并非人人被问到，偏专业与简历深挖）、单面（结合岗位与简历）。②实习：往年 6 周实习期，留用率 30%-50%，不同岗位有差异。③答辩：按实习岗位做课题答辩。④终面：答辩结束后由领导终面追问。作者在评论区答疑，提供长期陪跑与单次辅导。',
 NULL, @now, @now, 6, NULL,
 'USER_DISCUSSION', 'MANUAL_SCREENSHOT', 'PENDING', '面试与求职经验,实习招聘', '截图未展示点赞/收藏/评论数',
 'LLM', 'LLM 截图解析（标题按正文要点归纳）', @now,
 '截图录入；截图未露出笔记标题与互动指标，标题按正文归纳生成；配套泰康资产 2027 届热招岗位长图 2/3', @now, @now),

-- D. 泰康集团职级与薪酬拆解
('XIAOHONGSHU', 'screenshot://xhs/6/d4b4b13c4cd6', '泰康集团职级薪酬全揭秘！应届生能拿多少？',
 '拆解泰康集团职级与薪酬体系：采用 A-E 级「管理+专业」双序列职级（每级细分多档，如 B1-B9、C1-C6），专业序列可经绩效考核与技能匹配转管理序列，总部晋升机会多于分公司。2024-2025 应届生薪酬：精算/IT 月薪 8-12K、年总包 15-20 万（总部算法岗 base 可达 1.35 万/月）；销售/营销 7-10K、12-18 万；职能/管培生 8-10K、14-18 万（总部硕士转正首年 16-17 万）；泰康资产/基金 15-20K、25-35 万（TOP2 硕士研究员岗可达 30-35 万总包）。薪酬构成：基本工资 50-60% + 绩效奖金 20-30% + 年终奖（1-3 个月）+ 补贴 + 提成。福利：五险一金按最高标准缴纳 + 企业年金（六险二金）、补充医疗/重疾/交通意外险、配偶子女家属保障、北京/上海/深圳/武汉落户支持、年度体检与健身场馆、餐补交通通讯节日费、千人计划培养。风险提示：部分部门有「零点精神」加班较多，总部薪资比分公司高 10-20% 但压力更大，试用期 6 个月薪资打 8-9 折。校招每年数千人规模，2025 年强调大健康与金融科技。',
 NULL, @now, @now, 6, NULL,
 'USER_DISCUSSION', 'MANUAL_SCREENSHOT', 'PENDING', '薪酬福利,职业发展,校园招聘', NULL,
 'LLM', 'LLM 截图解析', @now,
 '截图录入；配套《泰康集团职级与薪酬深度分析报告》长图 1/14-10/14；截图显示发布 01-30，评论区有泰康在线官方账号互动', @now, @now)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content_summary = VALUES(content_summary),
  topic_tags = VALUES(topic_tags),
  owner_type = VALUES(owner_type),
  source_type = VALUES(source_type),
  verification_status = VALUES(verification_status),
  missing_reason = VALUES(missing_reason),
  clean_source = VALUES(clean_source),
  clean_basis = VALUES(clean_basis),
  cleaned_at = VALUES(cleaned_at),
  last_observed_at = VALUES(last_observed_at),
  remark = VALUES(remark),
  updated_at = VALUES(updated_at);

-- -------------------------------------------------------------
-- 2. 互动指标快照（C 无指标，不建快照，missing_reason 已说明）
-- -------------------------------------------------------------
INSERT INTO social_note_metric_snapshot
(note_id, like_count, favorite_count, comment_count, share_count, view_count, metric_observed_at, metric_source)
SELECT n.id, m.like_cnt, m.fav_cnt, m.cmt_cnt, NULL, NULL, @now, 'SCREENSHOT_2026-10-27'
FROM social_note n
JOIN (
            SELECT 'screenshot://xhs/6/b628767407a9' AS u, 103 AS like_cnt, 101 AS fav_cnt, 2 AS cmt_cnt
  UNION ALL SELECT 'screenshot://xhs/6/eae2488abd2f', 30, 28, 9
  UNION ALL SELECT 'screenshot://xhs/6/d4b4b13c4cd6', 87, 96, 3
) m ON m.u = n.note_url
WHERE NOT EXISTS (
  SELECT 1 FROM social_note_metric_snapshot s
  WHERE s.note_id = n.id AND s.metric_source = 'SCREENSHOT_2026-10-27'
);

-- -------------------------------------------------------------
-- 3. 校验（执行后人工核对）
-- -------------------------------------------------------------
SELECT n.id, n.note_url, n.title, n.topic_tags, n.owner_type, n.verification_status,
       s.like_count, s.favorite_count, s.comment_count
FROM social_note n
LEFT JOIN social_note_metric_snapshot s
       ON s.note_id = n.id AND s.metric_source = 'SCREENSHOT_2026-10-27'
WHERE n.company_id = 6
ORDER BY n.id;
