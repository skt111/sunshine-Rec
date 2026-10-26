<template>
  <div class="select" ref="sel" v-clickoutside="handleClose">
    <div class="input2" @click="toggle">
      <input
        type="text"
        placeholder="请选择"
        readonly
        :title="showVal"
        :value="showVal"
      />
      <Icon type="ios-arrow-down" class="img" />
    </div>
    <div class="fuzzyCon" v-if="isFuzzy">
      <input type="text" v-model="queryLike" />
      <Icon
        type="ios-search"
        size="24"
        class="searchIcon"
        @click="fuzzySearch"
      />
    </div>
    <ul
      class="content"
      :class="{
        bottom: position == 'bottom',
        top: position == 'top',
        top50: isFuzzy,
      }"
      v-show="show && dataList && dataList.length"
      ref="content"
    >
      <li
        v-for="(item, idx) in dataList"
        :key="'muti' + idx"
        @click.stop="selectOption(item)"
        :class="item.checked ? 'active' : ''"
      >
        {{ item.content }}
        <Icon type="md-checkmark" class="imgLi" v-if="item.checked" />
      </li>
    </ul>
    <div class="noCon" v-show="selectList && selectList.length == 0">
      <img src="../assets/img/common/noData.png" />
    </div>
  </div>
</template>
<script>
/*-----------
下拉多选select的数据结构：
   父组件传递参数有：
      "values"：默认选择值；
      "selectList":下拉选择的list数据集，[{
      content：展示值,
      code：实际值,
      checked:true or false是否选中
      },{},...,{}]。
  子组件触发父组件事件：onchange:事件返回选中的数据selectVal；
  子组件：
      showVal:页面展示值；
      position:下拉展示的位置，默认为bottom；
      通过控制show对下拉选择做展开和收起操作。
------------*/

export default {
  name: "MultipleSelect",
  props: {
    values: {
      type: Array,
      default: () => [],
    },
    selectList: {
      type: Array,
      default: () => [],
    },
    //是否可下拉
    disabled: {
      type: Boolean,
      default: false,
    },
    //是否下拉选择全部  
    isAll: {
      type: Boolean,
      default: true,
    },
    //是否模糊搜索
    isFuzzy: {
      type: Boolean,
      default: false,
    },
    //查询的额外参数
    postData: {
      type: Object,
      default: () => {
        return {};
      },
    },
  },
  data() {
    return {
      queryLike: "", //搜索框模糊搜索
      showVal: "",
      show: false,
      position: "bottom",
    };
  },
  computed: {
    dataList() {
      return this.selectList;
    },
  },
  created() {},
  mounted() {
    //select下拉位置
    this.computePos();
    this.initSelections();
  },
  methods: {
    //初始化下拉数据
    initSelections() {
      let that = this;
      if (this.values.length && this.dataList.length) {
        if (this.isAll)
          this.dataList.unshift({
            code: "",
            content: "全部",
            checked: false,
          });
        this.dataList.map(function (item) {
          for (let i = 0; i < that.values.length; i++) {
            if (that.values[i] == item.code) {
              that.$set(item, "checked", true);
            }
          }
        });
        //展示值,实际值
        this.setVal();
      } else {
        if (this.isAll) this.values.push("");
      }
    },
    getElementTop(element) {
      var actualTop = element.offsetTop;
      var current = element.offsetParent;
      while (current !== null) {
        actualTop += current.offsetTop;
        current = current.offsetParent;
      }
      return actualTop;
    },
    // 计算选择框是往上弹出还是往下弹出
    computePos() {
      let elHeight = this.$refs.sel.offsetHeight;
      let absPos = this.getElementTop(this.$refs.sel);
      let contentHeight = this.dataList && this.dataList.length * 40;

      let docScrollHei =
        document.body.scrollTop || document.documentElement.scrollTop || 0;

      let docHeight =
        document.documentElement.clientHeight ||
        document.body.clientHeight ||
        0;
      this.position = "bottom";
      /*if ((elHeight + absPos + contentHeight - docScrollHei) > docHeight) {
                this.position = 'top';
            } else {
                this.position = 'bottom';
            }*/
    },
    //收起/展开
    toggle() {
      if (this.disabled) {
        return false;
      }
      this.show = !this.show;
    },
    //点击空白处隐藏下拉多选
    handleClose(e) {
      this.show = false;
    },
    selectOption(item) {
      //选择下拉的操作事件，是全部，传空，否则
      //存在移除，不存在则添加
      let that = this;
      this.dataList.map(function (option) {
        if (option.content == item.content) {
          that.$set(option, "checked", !item.checked);
          if (item.code == "") {
            //点击全部
            that.values.splice(0, that.values.length);
            that.dataList.map(function (item) {
              if (item.code !== "") {
                that.$set(item, "checked", false);
              }
            });
            if (item.checked) {
              that.values.push(item.code);
            }
          } else {
            //点击其他的
            let index = that.values && that.values.indexOf("");
            if (index > -1) {
              //判断之前是否点击全部，点击全部，移除的同时，设置checked为false
              that.values.splice(index, 1);
              that.dataList.map(function (item) {
                if (item.code == "") {
                  that.$set(item, "checked", false);
                }
              });
            }
            if (item.checked) {
              that.values.push(item.code);
            } else {
              let index = that.values && that.values.indexOf(item.code);
              if (index > -1) {
                that.values.splice(index, 1);
              }
            }
          }
        }
      });
    },
    //模糊搜索
    fuzzySearch() {
      this.$emit("fuzzyFun", this.queryLike);
    },
    setVal() {
      //展示默认值
      let that = this,
        values = [];
      this.dataList.forEach(function (item) {
        if (item.checked || that.values.indexOf(item.code) > -1) {
          values.push(item.content);
        }
      });
      this.showVal = values.join();
      //触发父组件onchang事件传递多选值
      this.$emit("onchange", {
        selectVal: this.values,
      });
    },
    refresh() {
      let that = this;
      this.dataList.forEach(function (item) {
        if (item.code == "") {
          that.$set(item, "checked", true);
        } else {
          that.$set(item, "checked", false);
        }
      });
      //点击全部
      this.values.splice(0, that.values.length);
      if (this.isAll) this.values.push("");
      this.setVal();
    },
  },
  watch: {
    //监听下拉多选值，更改展示值,并触发父组件onchang事件传递多选值
    values() {
      this.setVal();
    },
    dataList: {
      handler() {
        if (
          this.dataList &&
          this.dataList.length > 0 &&
          this.dataList[0].content === "全部"
        ) {
          return;
        }
        this.initSelections();
      },
      deep: true,
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
@mixin padding($pd) {
  padding: $pd;
}

$vendors: webkit, moz, ms, o;
@mixin prix($type, $val) {
  #{$type}: #{$val};
  @each $v in $vendors {
    #{"-" + $v + "-" + $type}: #{$val};
  }
}

.select {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}

.input2 {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
  input {
    width: 100%;
    height: 100%;
    cursor: pointer;
    border: none;
    background: transparent;
    border-radius: 5px;
    position: relative;
    top: -4px;
    padding-left: 8px;
  }
  .img {
    right: 5px;
    top: 50%;
    width: 12px;
    height: 12px;
    position: absolute;
    transform: translateY(-50%);
  }
}

.content {
  width: 100%;
  height: 320px;
  overflow-y: auto;
  margin-top: 0px;
  max-height: px(300);
  overflow-y: scroll;
  border-radius: 10px;
  @include padding(4px 0);
  position: absolute;
  left: 0;
  background-color: white;
  box-shadow: 0 0 20px 2px #ccc;
  @include prix(transform, translateY(5px));
  z-index: 2;
  ul {
    height: 240px;
    position: relative;
    top: 0px;
    overflow-y: auto;
    &.top50 {
      top: 50px;
    }
  }
  /*滚动条样式*/
  ul::-webkit-scrollbar {
    width: 4px;
    /*height: 4px;*/
  }
  ul::-webkit-scrollbar-thumb {
    border-radius: 10px;
    -webkit-box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
    background: rgba(0, 0, 0, 0.2);
  }
  ul::-webkit-scrollbar-track {
    -webkit-box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
    border-radius: 0;
    background: rgba(0, 0, 0, 0.1);
  }
  li {
    height: 40px;
    line-height: 40px;
    width: 100%;
    position: relative;
    @include padding(0 0 0 10px);
    .imgLi {
      font-size: 20px;
      top: 10px;
      right: 10px;
      position: absolute;
    }
  }
  .active {
    color: #409eff;
  }
  :hover {
    color: #409eff;
    background-color: rgba(33, 33, 33, 0.2);
  }
}

.content::-webkit-scrollbar {
  display: none;
}

.bottom {
  top: 100%;
}

.top {
  bottom: 125%;
}
.fuzzyCon {
  padding: 10px;
  position: fixed;
  width: 100%;
  input {
    width: 100%;
    height: 100%;
    cursor: pointer;
    border-radius: 5px;
    border: 1px solid #dcdee2;
    height: 32px;
    line-height: 32px;
    padding-left: 10px;
  }
  .searchIcon {
    position: absolute;
    right: 10px;
    top: 15px;
  }
}
.noCon {
  width: 100%;
  text-align: center;
  top: 100px;
  position: relative;
  img {
    width: 100px;
  }
}
</style>