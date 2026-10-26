<!--
 * @Descripttion: 
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
-->
<template>
  <div class="pageHome">
    <!-- 头部 -->
    <MainHeader></MainHeader>
    <!-- 菜单处理 -->
    <div :class="['mainMenus', isShow ? '' : 'iconWidth']">
      <Menus :isShow="isShow" :menuData="menuList" @toPage="showPage"></Menus>
    </div>
    <img
      :class="['fixImg', isShow ? 'fixMemu' : 'fixIcon']"
      @click="fixMenu"
      :src="
        isShow
          ? require('../../assets/img/menu/closeMenu.png')
          : require('../../assets/img/menu/openMenu.png')
      "
    />
    <!-- 内容嵌入 -->
    <div :class="['outerCon', 'addOuter', isShow ? '' : 'addWidth']">
      <!-- 导航 -->
      <ul class="navCon">
        <li v-for="(item, idx) in navList" :key="'nav' + idx">
          <a @click="toLink(item)"
            ><label v-if="idx!=0">/</label
            ><span :class="item.linkTo ? 'linkSpan' : ''">{{
              item.name
            }}</span></a
          >
        </li>
      </ul>
      <div :class="['mainCon', 'addGuid']" ref="mainRef" @scroll="conScroll">
        <router-view :key="keyRouter"></router-view>
      </div>
    </div>
    <!-- 底部 -->
    <!-- <MainFooter></MainFooter> -->
    <Loading ref="loading"></Loading>
  </div>
</template>

<script>
import Loading from "@/components/Loading";
import MainHeader from "./MainHeader";
import Menus from "./Menus";
import MainFooter from "./MainFooter";
import { postDataList } from "../../api/dataRequst";

export default {
  name: "MainPage",
  components: {
    Loading,
    Menus,
    MainHeader,
    MainFooter,
  },
  data() {
    return {
      menuList: [
        {
          id: "L3801", //当前菜单id
          pid: null, //父级菜单id
          linkTo: null,
          name: "配置管理",
          isOpen: false, //是否展开下一级
          isCheck: false, //是否选中
          imgIcon: require("../../assets/img/menu/setting.svg"),
          children: [
            {
              id: "L380101", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/standardRank",
              name: "标准职级配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380102", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/rankMap",
              name: "职级映射配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380103", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/longUnit",
              name: "长期单位配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380104", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/longYear",
              name: "长期周年配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380105", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/xtFixed",
              name: "信托固定字段配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380106", //当前菜单id
              pid: "L3801", //父级菜单id
              linkTo: "/config/otherBase",
              name: "其他基础配置",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
          ],
        },
        {
          id: "L3802", //当前菜单id
          pid: null, //父级菜单id
          linkTo: null,
          name: "年度业务",
          isOpen: false, //是否展开下一级
          isCheck: false, //是否选中
          imgIcon: require("../../assets/img/menu/operation.svg"),
          children: [
            {
              id: "L380201", //当前菜单id
              pid: "L3802", //父级菜单id
              linkTo: "/renewalManagement/newSigning",
              name: "年度进度管理",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
            {
              id: "L380202", //当前菜单id
              pid: "L3802", //父级菜单id
              linkTo: "/renewalManagement/annualInformation",
              name: "单位进度管理",
              isOpen: false, //是否展开下一级
              isCheck: false, //是否选中
            },
          ],
        },
      ], //菜单信息
      isShow: false, //控制菜单展示隐藏
      isNext: false,
      loadingRef: false,
    };
  },
  computed: {
    //切换路由
    keyRouter() {
      return this.$route.path + Math.random();
    },
    //导航信息
    navList() {
      let checkList = [];
      this.menuList.map((item) => {
        if (item.isCheck) {
          checkList.push(item);
          if (item.children && item.isOpen) {
            item.children.map((ele) => {
              if (ele.isCheck) {
                checkList.push(ele);
              }
            });
          }
        }
      });
      return checkList;
    },
  },
  watch: {
    loadingRef() {
      if (this.loadingRef) {
        this.$refs.loading.show();
      } else {
        this.$refs.loading.hide();
      }
    },
  },
  //遮罩层
  provide() {
    return {
      showLoading: () => {
        this.loadingRef = true;
      },
      hideLoading: () => {
        this.loadingRef = false;
      },
    };
  },
  created() {
    //菜单信息
    let menuData = JSON.parse(localStorage.getItem("menuData"));
    this.menuList = menuData && menuData.menu;
    this.$store.commit('changeInfo',{
        isShow:false
    });
  },
  mounted() {
  },
  methods: {
    conScroll() {
      this.$store.commit("saveScrollTop", {
        scrollTop: this.$refs.mainRef.scrollTop,
      });
    },
    linkTo(page) {
      //新打开页签
      let routeData = this.$router.resolve({
        path: page,
      });
      window.open(routeData.href, "_blank");
    },
    //注销
    async toQuit() {
      this.$refs.loading.show();
      try {
        let res = await postDataList("");
        let data = res.data;
        this.$refs.loading.hide();
        if (data.code == 200) {
          //处理跳转
        } else {
          this.$Message.info(data.message);
        }
      } catch (e) {
        this.$refs.loading.hide();
        this.$Message.error("请求失败");
      }
    },
    //页面跳转展示
    showPage(to) {
      //父组件调整
      this.$router.push(to.linkTo);
    },
    //固定菜单
    fixMenu() {
      this.isShow = !this.isShow;
      this.$store.commit('changeInfo',{
        isShow:this.isShow
      })
    },
    //导航跳转
    toLink(to) {
      if (!to.linkTo) {
        return false;
      }
      this.$router.push(to.linkTo);
    },
  },
  beforeDestroy() {},
};
</script>

<style lang="scss" scoped>
.pageHome {
  float: left;
  width: 100%;
  background: #f4f6fa;
  position: relative;
}

@mixin fw($val) {
  float: left;
  width: #{$val + "%"};
}

.menus {
  @include fw(100);
  margin-top: 10px;
}
//菜单
//出现
@keyframes fade-in {
  0% {
    width: 208px;
  }

  100% {
    width: 50px;
  }
}

@-webkit-keyframes fade-in {
  0% {
    width: 208px;
  }

  100% {
    width: 50px;
  }
}

@-ms-keyframes fade-in {
  0% {
    width: 208px;
  }

  100% {
    width: 50px;
  }
}

@-o-keyframes fade-in {
  0% {
    width: 208px;
  }

  100% {
    width: 50px;
  }
}

@-moz-keyframes fade-in {
  0% {
    width: 208px;
  }

  100% {
    width: 50px;
  }
}

//消失
@keyframes fade-out {
  0% {
    width: 50px;
  }

  100% {
    width: 208px;
  }
}

@-webkit-keyframes fade-out {
  0% {
    width: 50px;
  }

  100% {
    width: 208px;
  }
}

@-ms-keyframes fade-out {
  0% {
    width: 50px;
  }

  100% {
    width: 208px;
  }
}

@-o-keyframes fade-out {
  0% {
    width: 50px;
  }

  100% {
    width: 208px;
  }
}

@-moz-keyframes fade-out {
  0% {
    width: 50px;
  }

  100% {
    width: 208px;
  }
}
//固定图标
//出现
@keyframes fadeFix-in {
  0% {
    left: 208px;
  }

  100% {
    left: 50px;
  }
}

@-webkit-keyframes fadeFix-in {
  0% {
    left: 208px;
  }

  100% {
    left: 50px;
  }
}

@-ms-keyframes fadeFix-in {
  0% {
    left: 208px;
  }

  100% {
    left: 50px;
  }
}

@-o-keyframes fadeFix-in {
  0% {
    left: 208px;
  }

  100% {
    left: 50px;
  }
}

@-moz-keyframes fadeFix-in {
  0% {
    left: 208px;
  }

  100% {
    left: 50px;
  }
}

//消失
@keyframes fadeFix-out {
  0% {
    left: 50px;
  }

  100% {
    left: 208px;
  }
}

@-webkit-keyframes fadeFix-out {
  0% {
    left: 50px;
  }

  100% {
    left: 208px;
  }
}

@-ms-keyframes fadeFix-out {
  0% {
    left: 50px;
  }

  100% {
    left: 208px;
  }
}

@-o-keyframes fadeFix-out {
  0% {
    left: 50px;
  }

  100% {
    left: 208px;
  }
}

@-moz-keyframes fadeFix-out {
  0% {
    left: 50px;
  }

  100% {
    left: 208px;
  }
}
//右侧内容
//快
@keyframes fadeCon-in {
  0% {
    left: 222px;
    width: calc(100% - 238px);
  }

  100% {
    left: 64px;
    width: calc(100% - 80px);
  }
}

@-webkit-keyframes fadeCon-in {
  0% {
    left: 222px;
    width: calc(100% - 238px);
  }

  100% {
    left: 64px;
    width: calc(100% - 80px);
  }
}

@-ms-keyframes fadeCon-in {
  0% {
    left: 222px;
    width: calc(100% - 238px);
  }

  100% {
    left: 64px;
    width: calc(100% - 80px);
  }
}

@-o-keyframes fadeCon-in {
  0% {
    left: 222px;
    width: calc(100% - 238px);
  }

  100% {
    left: 64px;
    width: calc(100% - 80px);
  }
}

@-moz-keyframes fadeCon-in {
  0% {
    left: 222px;
    width: calc(100% - 238px);
  }

  100% {
    left: 64px;
    width: calc(100% - 80px);
  }
}

//快
@keyframes fadeCon-out {
  0% {
    left: 64px;
    width: calc(100% - 80px);
  }

  100% {
    left: 222px;
    width: calc(100% - 238px);
  }
}

@-webkit-keyframes fadeCon-out {
  0% {
    left: 64px;
    width: calc(100% - 80px);
  }

  100% {
    left: 222px;
    width: calc(100% - 238px);
  }
}

@-ms-keyframes fadeCon-out {
  0% {
    left: 64px;
    width: calc(100% - 80px);
  }

  100% {
    left: 222px;
    width: calc(100% - 238px);
  }
}

@-o-keyframes fadeCon-out {
  0% {
    left: 64px;
    width: calc(100% - 80px);
  }

  100% {
    left: 222px;
    width: calc(100% - 238px);
  }
}

@-moz-keyframes fadeCon-out {
  0% {    
    left: 64px;
    width: calc(100% - 80px);
  }

  100% {
    left: 222px;
    width: calc(100% - 238px);
  }
}
.mainMenus {
  position: fixed;
  top: 56px;
  float: left;
  height: calc(100vh - 56px);
  width: 208px;
  animation: fade-out 0.4s;
  padding: 16px 0;
  background: #fff;
  overflow: hidden;
  border-right: 1px solid #e9eaf1;

  &.iconWidth {
    width: 50px;
    animation: fade-in 0.4s;
  }
}

.fixImg {
  position: fixed;
  top: 50vh;
  width: 14px;
  height: 66px;
  cursor: pointer;
  &.fixIcon {
    animation: fadeFix-in 0.4s;
    left: 50px;
  }
  &.fixMemu {
    animation: fadeFix-out 0.4s;
    left: 208px;
  }
}

.navCon {
  display: inline-block;
  list-style: none;
  margin-top: 8px;
  margin-bottom: 8px;

  li {
    display: inline-block;
    height:24px;
    line-height:24px;
    color: #8C8C8C;
    font-size:12px;
  }

  span {
    color: #8C8C8C;
  }

  .linkSpan {
    color: #3C4353;
  }

  label {
    color: #9E9E9E;
    padding: 0 10px;
  }
}

.outer {
  display: inline-block;

  .oneLi {
    display: inline-block;
    margin-right: 20px;
  }
}

.outerCon {
  float: left;
  animation: fadeCon-out 0.4s;
  width: calc(100% - 238px);
  right: 16px;
  position: relative;
  padding-top: 56px;
  left: 222px;

  &.addWidth {
    animation: fadeCon-in 0.4s;
    width: calc(100% - 80px);
    left: 64px;
  }
}

.mainCon {
  float: left;
  width: 100%;
  height: 100%;

  &.addGuid {
    min-height: calc(100vh - 112px);
    //background: #ffffff;
    //box-shadow: 0px 4px 10px 0px rgba(0, 0, 0, 0.3);
  }
}

.proTitle {
  padding: 10px 0 10px 12px;

  img {
    width: 24px;
    vertical-align: middle;
  }

  span {
    padding-left: 5px;
    font-size: 15px;
    font-weight: bold;
    vertical-align: middle;
  }
}
</style>