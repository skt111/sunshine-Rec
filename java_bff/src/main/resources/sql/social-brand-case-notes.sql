-- =============================================================
-- 雇主品牌雷达 · 案例笔记入库（跨行业标杆：蔚来 / 海信）
-- 来源：02-雇主品牌建设·模块页.html「新媒体爆款雷达」小红书列（第 1、2 条）
-- 证据等级：B（公众号「案例拆解原文」→ 校方/媒体对官方动作的报道）
-- 说明：小红书平台内笔记无网页直链，note_url 存其案例拆解原文链接以便追溯
-- 幂等：target_company 唯一键 company_name / social_note 唯一键 note_url
-- 日期：2026-10-27
-- =============================================================

USE sunshine;

SET @now = NOW();

-- 1. 两家跨行业标杆企业（矩阵分组「跨行业标杆」对应 CROSS_INDUSTRY）
INSERT INTO target_company
(company_name, company_aliases, company_category, recruitment_entity, enabled, notes, created_at, updated_at)
VALUES
('蔚来', 'NIO', 'CROSS_INDUSTRY', '集团总部', 1,
 '来源：02-雇主品牌建设·模块页.html 新媒体爆款雷达（跨行业标杆）', @now, @now),
('海信集团', '海信 / Hisense', 'CROSS_INDUSTRY', '集团总部', 1,
 '来源：02-雇主品牌建设·模块页.html 新媒体爆款雷达（跨行业标杆）', @now, @now)
ON DUPLICATE KEY UPDATE
  company_aliases = VALUES(company_aliases),
  company_category = VALUES(company_category),
  recruitment_entity = VALUES(recruitment_entity),
  enabled = VALUES(enabled),
  notes = VALUES(notes),
  updated_at = @now;

-- 2. 两条小红书案例笔记
INSERT INTO social_note
(platform, note_url, title, content_summary, published_at,
 first_observed_at, last_observed_at, company_id, account_id,
 owner_type, source_type, verification_status, topic_tags, missing_reason,
 clean_source, clean_basis, cleaned_at, remark, created_at, updated_at)
VALUES
('XIAOHONGSHU',
 'http://mp.weixin.qq.com/s?__biz=MzU4NjA4NjMwNw==&mid=2248431946&idx=1&sn=203e9b2823940f1285462dd22387e226&chksm=ffb8a940b7a6a435b4795736e37445f740986e13b570d6ab9a7ba372d3f11e717be02bfb06f2',
 '蔚来招聘 ·「蔚来人 MBTI 工位图鉴」系列',
 '· 账号简介直写「00 后接管的蔚来招聘官方账号」，正文按 MBTI 分型展示员工真实工位，评论区晒工位 + 报 MBTI 抽 NIO Life 玩偶。\n· 招聘信息只占一条，其余内容全是「人」，把岗位翻译成具体的人与工作场景。\n· 阳光可借鉴：阳光最难被理解的精算、AI 应用开发、核保医学三类岗，可用「阳光人工位图鉴」一张图讲清工位上摆什么（IFoA 教材／双屏代码／医学词典），比 JD 更易被记住。\n· 互动机制可直接抄：晒工位 + 抽小礼，预算为零。',
 NULL, @now, @now,
 (SELECT id FROM target_company WHERE company_name = '蔚来'), NULL,
 'OFFICIAL', 'MANUAL', 'PENDING', '企业文化,员工体验', '案例库未披露点赞/收藏/评论指标',
 'RULE', '人工整理自案例库（B 级证据：公众号案例拆解原文）', @now,
 '来源：02-雇主品牌建设·模块页.html「新媒体爆款雷达」小红书第 1 条；平台内笔记无网页直链，链接为案例拆解原文', @now, @now),

('XIAOHONGSHU',
 'http://mp.weixin.qq.com/s?__biz=MzU4NjA4NjMwNw==&mid=2248431946&idx=1&sn=203e9b2823940f1285462dd22387e226&chksm=ffb8a940b7a6a435b4795736e37445f740986e13b570d6ab9a7ba372d3f11e717be02bfb06f2#hisense',
 '海信集团招聘 ·「哈利打工日记」一天 plog',
 '· 以卡通 IP「哈利」的一天为时间轴：8:30 打卡 → 9:30 学同行 → 11:30 食堂 → 15:30 脑暴 → 18:00 健身房，把「上班的一天」做成连续剧。\n· 校招信息只在标签里出现；同账号另有世界杯 IP 校园派对等玩法。\n· 阳光可借鉴：视频号「奋斗青春最阳光」已有 67 条原创底子，缺的正是这种「可连载」形式；建议把镜头放进阳光数科 × 蚂蚁数科的 AI 保险应用联合实验室，拍「一天」而不是拍「宣传片」。\n· 909 亿保费体量意味着这些不是摆拍，是真实业务流。',
 NULL, @now, @now,
 (SELECT id FROM target_company WHERE company_name = '海信集团'), NULL,
 'OFFICIAL', 'MANUAL', 'PENDING', '企业文化,员工体验', '案例库未披露点赞/收藏/评论指标',
 'RULE', '人工整理自案例库（B 级证据：公众号案例拆解原文）', @now,
 '来源：02-雇主品牌建设·模块页.html「新媒体爆款雷达」小红书第 2 条；平台内笔记无网页直链，链接为案例拆解原文', @now, @now)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content_summary = VALUES(content_summary),
  company_id = VALUES(company_id),
  owner_type = VALUES(owner_type),
  source_type = VALUES(source_type),
  verification_status = VALUES(verification_status),
  topic_tags = VALUES(topic_tags),
  missing_reason = VALUES(missing_reason),
  clean_source = VALUES(clean_source),
  clean_basis = VALUES(clean_basis),
  cleaned_at = VALUES(cleaned_at),
  last_observed_at = VALUES(last_observed_at),
  remark = VALUES(remark),
  updated_at = VALUES(updated_at);

-- 3. 校验
SELECT n.id, tc.company_name, tc.company_category, n.title, n.topic_tags, n.source_type
FROM social_note n
JOIN target_company tc ON tc.id = n.company_id
WHERE tc.company_category = 'CROSS_INDUSTRY'
ORDER BY n.id;
