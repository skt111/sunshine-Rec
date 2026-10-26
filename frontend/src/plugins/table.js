import Vue from 'vue'
import XEUtils from 'xe-utils'
import {
  VXETable,
  Header,
  Column,
  Colgroup,
  Table,
  Footer,
} from 'vxe-table'
import zhCN from 'vxe-table/lib/locale/lang/zh-CN'

// 导入默认的国际化（如果项目中使用多语言，则应该导入到 vue-i18n 中）
VXETable.setup({
  i18n: key => XEUtils.get(zhCN, key)
})

// 先按需导入依赖的模块
Vue.use(Header)
Vue.use(Column)
Vue.use(Colgroup)
// 最后安装核心库
Vue.use(Table)
Vue.use(Footer)
import 'vxe-table/lib/style.css'//引用全局样式