<template>
  <nav :class="focusId.indexOf('L03')>-1?'lineMenu topMenu':'topMenu'">
    <ul
      v-if="menus.length>0"
      :class="focusId.indexOf('L03')>-1?'lineLi':''"
    >
      <li
        v-for="(item,idx) in menus"
        :key="'item'+idx"
        class="titSub"
        v-if="!item.hide"
      >
      <router-link
        :to="item.hrefPath"
        @click.native="whichSide(item)"
        :class="item.checked?'active':''"
      >{{item.name}}</router-link>
       </li>
    </ul>
  </nav>
</template>
<script>
/*-----------
菜单menus的数据结构：
    "id": 菜单的id,
    "pid": 父菜单的id,
    "name": 菜单显示名称,
    "url":跳转的路由,根据URL是否存在，展示是否带路由菜单,
    "open":是否展示子菜单，true展开，false隐藏,
    "nodes":子菜单，对象数组[{},{},...,{}],
    "isThree":是否有三级菜单，一级菜单ul的class=list-first,二级菜单是list-second,三级菜单是list-third，用于样式调整,
    "focusId":聚焦的菜单id集"L060101,
通过控制open对子菜单做展开和收起操作,以及控制图标的展示内容。
------------*/
export default {
  name: "Menu",
  props: ["menus", "focusId"],
  data() {
    return {};
  },
  created() {
    this.initMenu();
  },
  watch: {
    focusId() {
      this.initMenu();
    },
    menus() {
      this.initMenu();
    }
  },
  mounted() {},
  methods: {
    //左侧菜单数据处理
    initMenu() {
      let that = this;
      if (this.menus.length > 0) {
        that.setMenu(that.focusId);
      }
    },
    setMenu(data) {
      this.menus.map(item => {
        if (item.code == data) {
          item.checked = true;
        } else {
          item.checked = false;
        }
      });
    },
    //数据查询
    whichSide(data, type) {
      this.setMenu(data.code);
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.topMenu{
  font-size: 20px;
    height: 85px;
  float:left;
  width:100%;
  background:url('../assets/img/common/top.jpg') no-repeat;
  background-size: 100% 100%;
}
.lineMenu{
  background:url('../assets/img/common/topLine.jpg') no-repeat;
  background-size: 100% 100%;
}
ul {
  float: left;
  position: relative;
  width:100%;
  list-style:none;
  li {
    text-align:center;
    color:#fff;
    position: relative;
    float: left;
    width: 20%;
    &.level1 {
      cursor: pointer;
      border-bottom: 1px solid #23335a;
    }
    a{    padding: 34px 20px 18px 20px;
    color: #fff;
    display: block;
    width: 160px;
    margin: 0 auto;
    }
  }
}
/* .lineLi li{
  a{
    width:180px;
  }
} */
.active {
  border-bottom: 3px solid #fff;
}
</style>
