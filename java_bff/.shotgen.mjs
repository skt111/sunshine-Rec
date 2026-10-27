import fs from 'node:fs';
import crypto from 'node:crypto';

const [jsonPath, companyId, sqlPath] = process.argv.slice(2);
const notes = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
const OBS = '2026-10-27 18:58:00';
const esc = (s) => String(s == null ? '' : s).replace(/\\/g, '\\\\').replace(/'/g, "''");
const lines = ['USE sunshine;'];

for (const n of notes) {
  const url = 'screenshot://xhs/' + companyId + '/' + crypto.createHash('md5').update(n.title + '|' + n.author).digest('hex').slice(0, 12);
  lines.push(
    'INSERT INTO social_note (platform, note_url, title, content_summary, first_observed_at, last_observed_at, ' +
    'company_id, account_id, owner_type, source_type, verification_status, topic_tags, missing_reason, clean_source, clean_basis, cleaned_at, remark, created_at, updated_at) VALUES (' +
    `'XIAOHONGSHU', '${esc(url)}', '${esc(n.title)}', '${esc(n.summary)}', '${OBS}', '${OBS}', ` +
    `${companyId}, NULL, '${n.owner || 'USER_DISCUSSION'}', 'MANUAL_SCREENSHOT', 'PENDING', '${esc(n.tags)}', NULL, 'LLM', 'LLM 截图解析', '${OBS}', '${esc(n.remark)}', '${OBS}', '${OBS}'` +
    ');'
  );
  lines.push(
    'INSERT INTO social_note_metric_snapshot (note_id, like_count, favorite_count, comment_count, share_count, view_count, metric_observed_at, metric_source, created_at) ' +
    `SELECT id, ${n.likes}, ${n.fav}, ${n.comments}, NULL, NULL, '${OBS}', 'SCREENSHOT_2026-10-27', '${OBS}' FROM social_note WHERE note_url = '${esc(url)}';`
  );
}

fs.writeFileSync(sqlPath, lines.join('\n'), 'utf8');
console.log('SQL written: ' + sqlPath + ' (' + notes.length + ' notes)');
