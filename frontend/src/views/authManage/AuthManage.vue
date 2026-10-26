<template>
  <div class="home">
    <div class="menu" v-if="!closeLeft">
      <div class="menu-left">
        <Menu
          :menus="menus"
          :focusId="focusId"
        ></Menu>
      </div>
    </div>
    <div class="arrawBtn" @click="toggleMenu" :style="closeLeft?'left:0%':'left:25%;'">
      <Icon
        :type="closeLeft?'ios-arrow-forward':'ios-arrow-back'"
        color="#1890ff"
        size="20"
      />
    </div>
    <router-view v-if="isRouterAlive" :style="closeLeft?'width:98%':'width:73%;'" ></router-view>
  </div>
</template>
<script>
import Menu from "@/components/LeftMenu.vue";
import "../../assets/shareHr.scss";
import util from "@/utils/util";
export default {
  name: "ShareHR",
  components: { Menu, util },
  data() {
    return {
      isRouterAlive: true,
      closeLeft: false, //是否隐藏左侧菜单
      menus: [
        {
          id: "A01",
          pid: null,
          name: "权限管理中心",
          open: false,
          icon: "../../assets/img/menu/title.png",
          nodes: [
            {
              id: "A0101",
              pid: "A01",
              name: "用户管理",
              open: false,
              url: "/authInfo/user",
              iconLS: "../../assets/img/menu/shape-1.png",
              iconLH: "../../assets/img/menu/shape-1-2.png"
            },
            {
              id: "A0102",
              pid: "A01",
              name: "角色管理",
              open: false,
              url: "/authInfo/role",
              iconLS: "../../assets/img/menu/shape-2.png",
              iconLH: "../../assets/img/menu/shape-2-2.png"
            },
            {
              id: "A0103",
              pid: "A01",
              name: "权限报表",
              open: false,
              url: "/authInfo/reports",
              iconLS: "../../assets/img/menu/shape-2.png",
              iconLH: "../../assets/img/menu/shape-2-2.png"
            },
            {
              id: "A0104",
              pid: "A01",
              name: "权限调整日志",
              open: false,
              url: "/authInfo/logs",
              iconLS: "../../assets/img/menu/shape-2.png",
              iconLH: "../../assets/img/menu/shape-2-2.png"
            },
            {
              id: "A0105",
              pid: "A01",
              name: "板块模板申请",
              open: false,
              url: "/configEhrAuth/blockConfig",
              iconLS: "../../assets/img/menu/shape-2.png",
              iconLH: "../../assets/img/menu/shape-2-2.png"
            }
          ]
        }
      ]
    };
  },

  provide() {
    return {
      reload: this.reload
    };
  },
  computed: {
    focusId() {
      return this.$store.state.focusId;
    }
  },
  created() {
    //存数据
  },
  destroy() {},
  mounted() {},
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function() {
        this.isRouterAlive = true;
      });
    },
    //展示/隐藏左侧菜单
    toggleMenu(){
      this.closeLeft=!this.closeLeft;
    }
  } /* ,
  beforeRouteEnter(to, from, next) {
    //更改背景色
    window.document.body.style.backgroundColor = "#fff";
    next();
  },
  beforeRouteLeave(to, from, next) {
    window.document.body.style.backgroundColor = "#eeeeee";
    next();
  } */
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.home {
  width: 100%;
  float: left;
}
.menu {
  width: 25%;
  float: left;
}
.menu-left {
  padding-bottom: 3000px;
  margin-bottom: -3000px;
  background: #fff;
  overflow: hidden;
  -webkit-box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}
.arrawBtn {
  float: left;
  background: #e6f7ff; 
  border-radius: 5px;
  position: absolute;
  left: 25%;
  top: 25%;
  height: 50px;
  line-height: 50px;
  cursor:pointer;
}
</style>