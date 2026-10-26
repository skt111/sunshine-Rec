export const mockSchools = [
  { id: 'pku', name: '北京大学', city: '北京', websiteUrl: 'https://www.pku.edu.cn/', employmentUrl: 'https://scc.pku.edu.cn/', enabled: true, mockData: true },
  { id: 'tsinghua', name: '清华大学', city: '北京', websiteUrl: 'https://www.tsinghua.edu.cn/', employmentUrl: 'https://career.tsinghua.edu.cn/', enabled: true, mockData: true },
  { id: 'fudan', name: '复旦大学', city: '上海', websiteUrl: 'https://www.fudan.edu.cn/', employmentUrl: 'https://career.fudan.edu.cn/', enabled: true, mockData: true },
  { id: 'nankai', name: '南开大学', city: '天津', websiteUrl: 'https://www.nankai.edu.cn/', employmentUrl: 'https://job.nankai.edu.cn/', enabled: true, mockData: true },
  { id: 'nju', name: '南京大学', city: '南京', websiteUrl: 'https://www.nju.edu.cn/', employmentUrl: 'https://scc.nju.edu.cn/', enabled: true, mockData: true },
  { id: 'hust', name: '华中科技大学', city: '武汉', websiteUrl: 'https://www.hust.edu.cn/', employmentUrl: 'https://job.hust.edu.cn/', enabled: true, mockData: true },
  { id: 'sysu', name: '中山大学', city: '广州', websiteUrl: 'https://www.sysu.edu.cn/', employmentUrl: 'https://career.sysu.edu.cn/', enabled: true, mockData: true },
  { id: 'uestc', name: '电子科技大学', city: '成都', websiteUrl: 'https://www.uestc.edu.cn/', employmentUrl: 'https://job.uestc.edu.cn/', enabled: true, mockData: true },
  { id: 'xjtu', name: '西安交通大学', city: '西安', websiteUrl: 'https://www.xjtu.edu.cn/', employmentUrl: 'https://job.xjtu.edu.cn/', enabled: true, mockData: true }
]

var eventTitles = ['2027 届校园招聘宣讲会', '秋季大型双选会', '重点行业专场招聘会', '线上就业指导分享会', '企业开放日与校招咨询', '毕业生就业市场专场']
var companies = ['中国人寿', '中国平安', '招商银行', '华泰证券', '字节跳动', '华为', '腾讯', '宁德时代']
var dates = ['2026-10-27', '2026-10-28', '2026-10-30', '2026-11-02', '2026-11-05', '2026-11-08', '2026-11-12', '2026-11-16', '2026-09-18', '2026-09-25']

function makeEvent (id, school, date, index) {
  var typeList = ['ON_CAMPUS_TALK', 'OFF_CAMPUS_TALK', 'ON_CAMPUS_FAIR', 'OFF_CAMPUS_FAIR', 'OTHER', 'PENDING_REVIEW']
  var type = typeList[index % typeList.length]
  var venueType = type === 'OTHER' ? '线上' : (index % 3 === 0 ? '线上线下混合' : '线下')
  var status = date < '2026-10-26' ? (index % 5 === 0 ? '已取消' : '已结束') : (index % 7 === 0 ? '进行中' : '未开始')
  if (type === 'PENDING_REVIEW') status = '待核验'
  return { id: id, schoolId: school.id, schoolName: school.name, schoolCity: school.city, eventTitle: eventTitles[index % eventTitles.length], eventType: type, venueType: venueType, standardCategory: type, eventDate: date, startTime: index % 4 === 0 ? '09:30' : '14:00', endTime: index % 5 === 0 ? '' : '16:30', venueName: venueType === '线上' ? '线上会议室（待发布）' : school.name + ' · ' + (index % 2 === 0 ? '大学生活动中心' : '就业服务大厅'), eventAddress: venueType === '线上' ? '' : school.city + '市校本部', organizer: school.name + '就业指导中心', companyNames: [companies[index % companies.length], companies[(index + 3) % companies.length]], registrationDeadline: index % 4 === 0 ? '2026-10-29 18:00' : '', registrationUrl: index % 4 === 0 ? 'https://example.com/demo-registration/' + id : '', eventDescription: type === 'PENDING_REVIEW' ? '活动信息缺少明确举办地点，等待人工核验。' : '面向应届毕业生的模拟就业活动，展示页面结构与筛选交互。', sourceListUrl: school.employmentUrl, sourceUrl: school.employmentUrl + 'demo-event-' + id, publishedAt: '2026-10-' + String(10 + index % 15).padStart(2, '0') + ' 09:00', updatedAt: '2026-10-26 09:' + String(10 + index).padStart(2, '0'), eventStatus: status, verificationStatus: type === 'PENDING_REVIEW' ? '待人工核验' : '已核验', mockData: true, changeNote: index % 3 === 0 ? '更新时间较近，建议持续观察。' : '暂无变更记录。' }
}

export const mockEmploymentEvents = mockSchools.reduce(function (all, school, schoolIndex) {
  dates.forEach(function (date, index) { all.push(makeEvent(schoolIndex + '-' + index, school, date, schoolIndex * 3 + index)) })
  return all
}, [])

export const categoryLabels = { ALL: '全部活动', ON_CAMPUS_TALK: '校内宣讲会', OFF_CAMPUS_TALK: '校外宣讲会', ON_CAMPUS_FAIR: '校内双选会', OFF_CAMPUS_FAIR: '校外双选会', OTHER: '其他就业活动', PENDING_REVIEW: '待人工核验' }

