/*
 * @Descripttion: slot使用
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
 */
<template>
  <div :class="['todoDiv', applyType + 'Style']">
    <slot :data="index"></slot>
    <div class="lineDiv">
      <div class="inputDiv txtCenter" style="width: 100%">
        <a class="searchBtn" @click="query">查询</a>
      </div>
    </div>
    <Table
      border
      stripe
      :loading="loading"
      :columns="headerData"
      :data="indexData"
      class="tableMagnify"
      @on-row-click="selectedData"
      @on-select="selectItem"
      @on-select-cancel="cancleItem"
      @on-select-all="selectAllItem"
      @on-select-all-cancel="selectAllItemCancel"
    ></Table>
    <div style="margin-top: 10px; float: left; width: 100%" v-show="isPage">
      <div style="float: right">
        <Page
          ref="pagenate"
          :total="total"
          :current="index.pageNum"
          @on-change="changePage"
          @on-page-size-change="changPageSize"
          :page-size-opts="[10, 20, 30, 40]"
          :page-size="index.pageSize"
          :show-sizer="true"
          :show-total="true"
        ></Page>
      </div>
    </div>
    <div class="lineDiv">
      <div
        class="inputDiv txtCenter"
        v-if="selectionCols"
        style="width: 100%; margin-top: 10px"
      >
        <a class="but" @click="selectedDataMore">确定</a>
      </div>
    </div>
  </div>
</template>
<script>
import util from "@/utils/util";
import columns from "./columns";
import { postDataList, getList } from "../../api/dataRequst";
export default {
  name: "MagnifySlot",
  components: {},

  data() {
    return {
      loading: false,
      total: 0, //分页总条数
      index: {
        pageSize: 10, //每页条数
        pageNum: 1, //当前页数
      },
      url: "", //默认列表查询接口的url
      headerData: [],
      indexData: [],
      selectData: [], //已选中的数据
    };
  },
  props: {
    //列表类型
    selectType: {
      type: String,
      default: () => {
        return "";
      },
    },
    //默认是否查询第一页信息
    isFirst: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //列表中是否增加复选框
    selectionCols: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //是否展示分页
    isPage: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //额外传参
    postMore: {
      type: Object,
      default: () => {
        return {};
      },
    },
    //整体风格的颜色:默认是蓝色，orange橙色
    applyType: {
      type: String,
      default: "",
    },
    //默认放大镜数据，无需调后台处理
    magnifyData: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //是否取默认值
    isAutoSet: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //列表动态字段
    moreCols: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //请求类型
    requestType: {
      type: String,
      default: "post",
    },
  },
  watch: {
    selectType() {
      this.initData();
    },
    postMore() {
      this.initData();
    },
  },
  created() {},
  mounted() {
    this.initData();
  },
  methods: {
    initData() {
      if (!this.selectType) {
        return false;
      }
      this.url = columns[this.selectType](this.selectType).url;
      this.headerData = columns[this.selectType](this.selectType).colums;
      //初始化查询的信息
      this.initInfo();
      //是否增加多选
      if (this.selectionCols) {
        this.initCols();
      }
      //是否动态字段
      if (this.moreCols.length > 0) {
        this.initMoreCols();
      }
      //默认是否查询
      if (this.isFirst) {
        this.query();
      }
    },
    //初始化查询信息
    initInfo() {
      this.index = {
        pageSize: 10, //每页条数
        pageNum: 1, //当前页数
      };
      if (columns[this.selectType](this.selectType).postData) {
        this.index = Object.assign(
          this.index,
          columns[this.selectType](this.selectType).postData
        );
      }
      this.indexData = [];
      this.total = 0;
      //多传参数
      if (!util.isEmptyObject(this.postMore)) {
        this.index = Object.assign(this.index, this.postMore);
      }
    },
    //是否多选
    initCols() {
      let storeCol = [
          {
            title: "选择",
            type: "selection",
            width: 80,
            align: "center",
          },
        ],
        orgData = [];
      this.headerData = orgData.concat(
        this.headerData.slice(0, 1),
        storeCol,
        this.headerData.slice(1)
      );
    },
    //是否动态列
    initMoreCols() {
      console.log(this.moreCols);
      let isHas = util.getInclude(this.headerData, this.moreCols);
      if (!isHas) {
        this.headerData = this.headerData.concat(this.moreCols);
      }
    },
    //获取选择的数据--点击单行
    selectedData(value) {
      this.$emit("onSend", {
        params: value,
        type: this.selectType,
      });
      this.index.pageNum = 1;
      this.index.pageSize = 10;
    },
    //获取多选的数据
    selectedDataMore() {
      this.$emit("onSend", {
        params: this.selectData,
        type: this.selectType,
      });
      this.index.pageNum = 1;
      this.index.pageSize = 10;
    },
    selectItem(selection, row) {
      var pushFlag = true;
      for (var i = 0; i < this.selectData.length; i++) {
        if (row.id == this.selectData[i].id) {
          pushFlag = false;
        }
      }
      if (pushFlag) {
        this.selectData.push(row);
      }
    },
    cancleItem(selection, row) {
      for (var i = 0; i < this.selectData.length; i++) {
        if (this.selectData[i].id == row.id) {
          this.selectData.splice(i, 1);
        }
      }
    },
    selectAllItem(selection) {
      this.selectData = [];
      for (var k = 0; k < selection.length; k++) {
        this.selectData.push(selection[k]);
      }
    },
    selectAllItemCancel() {
      this.selectData = [];
    },
    //查询
    query(event) {
      if(event) event.preventDefault();
      this.$refs.pagenate.currentPage = 1;
            this.loading = true;
      this.index.pageNum = 1;
      this.initTable();
    },
    //重置
    reset() {
      //包含slot插入了别的查询条件
      this.initInfo();
      this.$refs.pagenate.currentPage = 1;
      if (this.isFirst) {
        this.initTable();
      }
    },
    //点击页数
    changePage(value) {
      this.loading = true;
      this.index.pageNum = value;
      this.initTable();
    },
    //点击每页行数
    changPageSize(value) {
      this.loading = true;
      this.index.pageSize = value;
      this.index.pageNum = 1;
      this.initTable();
    },
    //表格生成
    async initTable() {
      this.total = 0;
      this.indexData = [];
      //多传参数
      if (!util.isEmptyObject(this.postMore)) {
        this.index = Object.assign({}, this.index, this.postMore);
      }
      let postData = this.index;
      //是默认值
      if (this.isAutoSet) {
        this.loading = false;
        this.indexData = this.magnifyData;
        return false;
      }
      try {
        let res = null;
        if (this.requestType == "get") {
          res = await getList(this.url, postData);
        } else {
          res = await postDataList(this.url, postData);
        }
        let data = res.data;
        this.loading = false;
        if (data.status == "success") {
          this.indexData = this.isPage ? data.result.list : data.result;
          this.total = (this.isPage && data.result && data.result.total) || 0; //总条数
          if (this.selectionCols) {
            for (var i = 0; i < this.indexData.length; i++) {
              this.indexData[i]._checked = false;
              for (var j = 0; j < this.selectData.length; j++) {
                if (
                  !!this.indexData[i].id &&
                  this.indexData[i].id == this.selectData[j].id
                ) {
                  this.indexData[i]._checked = true;
                }
              }
            }
          }
        } else {
          this.$Message.error(data.message);
        }
      } catch (e) {
        this.loading = false;
        this.$Message.error("请求失败！");
      }
    },
  },
};
</script>
<style lang="scss" scoped>
@mixin fw($val) {
  float: left;
  width: #{$val + "%"};
}
.lineDiv {
  @include fw(100);
  position: relative;
  .inputDiv {
    @include fw(50);
    .spanName {
      width: 115px;
      display: inline-block;
      height: 32px;
      line-height: 32px;
      padding-right: 10px;
      text-align: right;
    }
    .input {
      width: 50%;
      height: 32px;
      border: 1px solid #dcdee2;
      border-radius: 5px;
      outline: none;
      padding-left: 10px;
    }
    .but {
      width: 90px;
      height: 25px;
      line-height: 25px;
      text-align: center;
      background: #2d8cf0;
      color: #fff;
      //padding: 5px 30px;
      font-size: 14px;
      border-radius: 4px;
      border: 1px solid #2d8cf0;
    }
  }
}
.tableMagnify {
  @include fw(100);
  margin-top: 10px;
}
.txtCenter {
  text-align: center;
}
.orangeStyle {
  .lineDiv .inputDiv .but {
    background: #faa001;
    border: 1px solid #faa001;
  }
  /deep/.ivu-table-header thead tr th {
    background: #faa001;
  }
}
</style>