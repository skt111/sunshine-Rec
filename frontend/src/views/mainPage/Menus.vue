<template>
  <ul class="menuCon allMenu">
    <li v-for="(item, idx) in menuList" :key="'menu' + idx">
      <div :class="['first', item.isCheck && 'checkOne']">
        <div class="imgCon">
          <img class="imgIcon" :src="require('@/assets/img/menu/'+item.imgIcon+'.svg')" />
        </div>
        <a
          @click="showCon(item, $event)"
          :class="[isShow ? 'showCon' : 'hideCon']"
          :title="item.name"
          >{{ item.name }}</a
        >
        <img
          class="arrow"
          v-show="item.children.length > 0 && isShow"
          :src="
            item.isOpen
              ? require('../../assets/img/menu/menuArrowDownOpen.png')
              : require('../../assets/img/menu/menuArrowDownClose.png')
          "
        />
      </div>
      <ul class="menuCon" v-show="item.children && item.isOpen && isShow">
        <li
          v-for="(ele, ii) in item.children"
          :class="ele.isCheck && 'checkTwo'"
          :key="'ele' + ii"
        >
          <a
            @click="showCon(ele, $event)"
            :class="['second', isShow ? 'showCon' : 'hideCon']"
            :title="ele.name"
            >{{ ele.name }}</a
          >
        </li>
      </ul>
    </li>
  </ul>
</template>
<script>
/*-----------
菜单menus的数据结构：
    "id": 菜单的id,
    "pid": 父菜单的id,
    "name": 菜单显示名称,
    "linkTo":跳转的页面,
    "isOpen":是否展开下一级，true展开，false隐藏,
    "isCheck":是否选中，true选中，false不选中，
    "children":子菜单，对象数组[{},{},...,{}],
------------*/
export default {
  name: "Menus",
  props: {
    //控制菜单展示隐藏
    isShow: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //菜单信息
    menuData: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      //选中菜单信息
      focusId: [],
    };
  },
  computed: {
    //菜单列表
    menuList() {
      return this.menuData;
    },
    //当前聚焦的id菜单
    currFocus() {
      return this.$store.state.focusId;
    },
  },
  created() {},
  watch: {
    currFocus: {
      handler(newVal, oldVal) {
        let focus = [];
        if (newVal.length == 5) {
          focus.push(newVal);
        }
        for (let i = 0; i <= (newVal.length - 5) / 2; i++) {
          focus.push(newVal.slice(0, newVal.length - 2 * i));
        }
        this.focusId = focus;
        //处理菜单
        this.dealMenu();
      },
      deep: true,
      immediate: true,
    },
  },
  mounted() {},
  methods: {
    //处理菜单
    dealMenu() {
      //先清空
      this.menuList.map((item) => {
        item.isCheck = false;
        item.isOpen = false;
        if (item.children) {
          item.children.map((ele) => {
            ele.isCheck = false;
            ele.isOpen = false;
          });
        }
      });
      this.menuList.map((item) => {
        if (this.focusId.includes(item.id)) {
          item.isCheck = true;
          item.isOpen = true;
        }
        if (item.children) {
          item.children.map((ele) => {
            if (this.focusId.includes(ele.id)) {
              ele.isCheck = true;
              ele.isOpen = true;
            }
          });
        }
      });
      console.log(this.menuList);
    },
    //菜单跳转
    showCon(to, event) {
      //选中的菜单存在focusId中
      this.$store.commit("focusId", { focusId: to.id });
      if (!to.linkTo) {
        event.preventDefault();
        event.stopPropagation();
        return false;
      }
      this.$emit("toPage", to);
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
//出现
@keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
@-webkit-keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
@-ms-keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
@-o-keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
@-moz-keyframes fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}
//消失
@keyframes fade-out {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
@-webkit-keyframes fade-out {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
@-ms-keyframes fade-out {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
@-o-keyframes fade-out {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
@-moz-keyframes fade-out {
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

.allMenu {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  /* 设置滚动条的样式 */
  &::-webkit-scrollbar {
    width: 5px;
    height: 5px;
  }

  /* 滚动槽 */
  &::-webkit-scrollbar-track {
    -webkit-box-shadow: inset 0 0 3px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
  }

  /* 滚动条滑块 */
  &::-webkit-scrollbar-thumb {
    border-radius: 5px;
    background: rgba(157, 165, 183, 0.7);
    -webkit-box-shadow: inset 0 0 3px RGB(243, 243, 244);
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.8);
  }
}
.showCon {
  animation: fade-in 0.8s;
}
.hideCon {
  display: none;
  animation: fade-out 0.8s;
}
.menuCon {
  width: 100%;
  position: relative;
  li {
    list-style: none;
    width: 100%;
    min-height: 40px;
    line-height: 40px;
  }
  .imgCon {
    width: 14px;
    height: 14px;
    overflow: hidden;
    vertical-align: middle;
    margin-right: 0.026042rem;
    cursor: pointer;
    float: left;
    position: relative;
    top: 12px;
  }
  .imgIcon {
    width: 100%;
    vertical-align: middle;
    margin-right: 5px;
    cursor: pointer;
    float: left;
    position: relative;
    top: 0px;
  }
  .first {
    height: 40px;
    line-height: 40px;
    padding: 0 16px;
  }
  .second {
    height: 40px;
    line-height: 40px;
    padding: 0 16px 0 35px;
  }
  .arrow {
    width: 14px;
    margin-left: 5px;
    vertical-align: middle;
  }
  .first a,
  .second {
    color: #838a9d;
    overflow: hidden;
    height: 40px;
    width: calc(100% - 45px);
    float: left;
    line-height: 40px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .second {
    width: 100%;
  }
  .checkOne {
    background: #2f54eb;
    box-shadow: 0px 4px 10px 0px rgba(0, 47, 167, 0.14);
    color: #fff;
    .imgIcon {
      transform: translateX(30px);
      filter: drop-shadow(#fff -30px 0px 0px);
      top: 0;
    }
    a {
      color: #fff;
    }
  }
  .checkTwo a{
    background: #f0f5ff;
    border-right: 3px solid #2f54eb;
    color: #2f54eb;
    font-weight: bold;
  }  
  .second:hover {
    color: #2f54eb;
    font-weight: bold;
  }
}
</style>

