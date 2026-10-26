<template>
  <div class="tabs">
    <div class="tabs-nav">
      <!--标题页的标题 v-for遍历, :class 动态绑定class-->
      <div
        :class="tabClass(item)"
        :key="'tab'+index"
        v-for="(item,index) in tabList"
        @click="tabChange(index)"
      >
        <div class="titLabel" v-html="item.label"></div>
        <!-- <span></span> -->
      </div>
    </div>
    <div class="tabs-content">
      <!--这里的slot就是嵌套的pane组件的内容-->
      <slot></slot>
    </div>
  </div>
</template>
<script>
/*----
父模板引入Tab和Pane，使用方法如下：（调用Tab.vue模板，Tab.vue调用Pane.vue模板）
<Tab v-model='value'>
  <Pane label="tab1" name='1'>
    pane1
  </Pane>
  <Pane label="tab2" name='2'>
    pane2
  </Pane>
  <Pane label="tab3" name='3'>
    pane3
  </Pane>
</Tab>
说明：
<Pane></Pane>：标签中的内容为tab页签展示的内容
lable：展示页签名字
name：页签的值
value：当前定位的页签，其中name和value是一致的
-----*/
export default {
  name: "Tab",
  props: {
    value: {
      type: String
    }
  },
  data() {
    return {
      tabList: [],
      currentValue: this.value
    };
  },
  methods: {
    //获取子组件
    getTabs() {
      return this.$children.filter(function(item) {
        return item.$options.name === "Pane";
      });
    },
    //更新tabs
    updateTabs() {
      this.tabList = [];
      var that = this;
      this.getTabs().forEach(function(pane, index) {
        that.tabList.push({
          label: pane.label,
          name: pane.name || index
        });
        //如果没有设置name，默认设置为索引值
        if (!pane.name) {
          pane.name = index;
        }
        //设置第一个pane为当前显示的tab
        if (index === 0) {
          if (!that.currentValue) {
            that.currentValue = pane.name || index;
          }
        }
      });
      this.updateStatus();
    },
    updateStatus() {
      var tabs = this.getTabs();
      var that = this;
      //显示当前选中的tab对应的pane组件，隐藏没有选中的
      tabs.forEach(function(tab) {
        return (tab.show = tab.name === that.currentValue);
      });
    },
    tabClass(item) {
      return [
        "tabs-tab",
        {
          //为当前选中的tab加样式
          "tabs-tab-active": item.name === this.currentValue
        }
      ];
    },
    //点击tab标题触发
    tabChange(index) {
      var nav = this.tabList[index];
      var name = nav.name;
      //改变当前选中的tab，触发watch
      this.currentValue = name;
      //实现子组件与父组件通信
      this.$emit("input", name);
      this.$emit("tabClick", name);
    }
  },
  watch: {
    value(val) {
      this.currentValue = val;
    },
    currentValue() {
      //tab发生变化时，更新pane的显示状态
      this.updateStatus();
    }
  }
};
</script>
<style lang="scss" scoped>
.tabs-nav {
  margin:10px;
  padding-bottom:10px;
  border-bottom:1px solid #e5e5e5;
  .tabs-tab {
    display: inline-block;
    padding: 0px 20px;
    height: 24px;
    line-height: 22px;
    margin-right: 20px;
    background:#fffaf8;
    color:#ff774f;
    text-align: center;
    cursor: pointer;
    border: 1px solid #ff774f;
    font-size:12px;
    border-radius:3px;
  }
  .tabs-tab-active {
    background: #fd7c59;
    color: #fff;
    position: relative;
    span {
      border: 10px solid transparent;
      border-top-color: #ff774f;
      position: absolute;
      left: 45%;
      bottom: -20px;
    }
  }
}

.tabs-content {
  float: left;
  width: 100%;
}

</style>