<template>
    <div ref="sel"  v-clickoutside="handleClose">
      <div class="filter-text" @click="showDisplay">
        <input
          class="filter-title"
          type="text"
          readonly=""
          placeholder="pleace select"
          v-model="showVal"
        >
        <i :class="show?'icon icon-filter-arrow filter-show':'icon icon-filter-arrow'"></i>
      </div>
      <ul
        class="filter-list"
        v-show="show"
      >
        <li v-for="(item) in dataList" @click="selectOption(item)" :key="item.code" :class="item.checked?'filter-selected':''"><a :title="item.code">{{item.content}}</a></li>        
      </ul>
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
  name: "SingleSelect",
  props: {
    values: {
      type: String,
      default: ""
    },
    selectList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
        reVals:this.values,
      showVal: "",
      show: false,
      position: "bottom"
    };
  },
  computed: {
    dataList() {
      return this.selectList;
    }
  },
  created() {},
  mounted() {
    let that = this;
    //select下拉位置
    this.computePos();
    this.initSelections();
  },
  methods: {
    //初始化下拉数据
    initSelections() {
      let that = this;
      if (this.reVals && this.dataList.length) {
        this.dataList.map(function(item) {
            if (that.reVals == item.code) {
              that.$set(item, "checked", true);
            }
        });
        //展示值,实际值
        this.setVal();
      } else {
        this.reVals="";
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
    showDisplay() {
      this.show = !this.show;
    },
    //点击空白处隐藏下拉多选
    handleClose(e) {
      this.show = false;
    },
    selectOption(item) {
      let that = this;
      this.show=false;
      this.dataList.map(function(option) {
        if (option.content == item.content) {
          that.$set(option, "checked", true);
           that.reVals=item.code;
        }else{
          that.$set(option, "checked", false);
        }
      });
       
    },
    setVal() {
      //展示默认值
      let that=this,values = [];
      this.dataList.forEach(function(item) {
        if (item.checked) {
          values.push(item.content);
        }
      });
      this.showVal = values.join();
      this.show=false;
      //触发父组件onchang事件传递多选值
      this.$emit("onchange", {
        selectVal: that.reVals
      });
    },
    refresh() {
      let that = this;
      this.dataList.forEach(function(item) {
        if (item.code == "") {
          that.$set(item, "checked", true);
        } else {
          that.$set(item, "checked", false);
        }
      });
      this.setVal();
    }
  },
  watch: {
  	values(newVal,oldVal) {
  		let that = this;
  		this.reVals = newVal;
	      if (this.reVals && this.dataList.length) {
	        this.dataList.map(function(item) {
	            if (that.reVals == item.code) {
	              that.$set(item, "checked", true);
	            }
	        });
	      } else {
	        this.reVals="";
	      }
    },
    //监听下拉多选值，更改展示值,并触发父组件onchang事件传递多选值
    reVals(newVal,oldVal) {
      this.setVal();
    },
    dataList: {
      handler() {
        if (
          this.dataList &&
          this.dataList.length > 0 
        ) {
          return;
        }
        this.initSelections();
      },
      deep: true
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
</style>