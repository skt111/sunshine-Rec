<!-- 列表查询 -->
<template>
  <div class="wrapper">
    <!-- 标题 -->
    <p class="titleP" v-if="isTitle">
      <span></span><label>{{ titleName }}</label>
    </p>
    <!-- 查询条件 -->
    <slot :data="searchData"></slot>
    <!-- 内部操作 -->
    <div :class="[operateClass]" v-if="isIn">
      <a class="resetBtn mr10" @click="reset" v-show="isReset">重置</a>
      <a class="searchBtn mr10" @click="query" v-show="isSearch">查询</a>
      <a
        v-show="isInsideBtn"
        v-for="(item, idx) in btnList"
        :key="'btn' + idx"
        :class="[item.class]"
        class="mr10"
        @click="btnOperate(item)"
        >{{ item.name }}</a
      >
    </div>
    <!-- 外部操作 -->
    <div :class="[operateClass]" v-if="isOut">
      <a
        v-for="(item, idx) in btnList"
        :key="'btn' + idx"
        :class="[item.class]"
        @click="btnOperate(item)"
        >{{ item.name }}</a
      >
    </div>
    <!-- 表格标题 -->
    <div class="tabletitleP" v-if="isTbaleTitle">
      <span></span><label>{{ tbaletitleName }}</label>
      <div :class="[tableBtnClass]" v-show="isTableBtn">
        <a
          v-for="(item, idx) in btnList"
          :key="'btn' + idx"
          :class="[item.class]"
          class="mr10"
          @click="btnOperate(item)"
          >{{ item.name }}</a
        >
      </div>
    </div>
    <div
      :class="['tableCon', topClass]"
      :style="{ 'min-height': iHeight + 'px' }"
    >
      <Table
        :data="listData"
        :columns="listCols"
        border
        no-data-text="暂无符合条件的查询结果"
        :loading="loading"
        @on-select="selectItem"
        @on-select-cancel="cancleItem"
        @on-select-all="selectAllItem"
        @on-select-all-cancel="selectAllItemCancel"
      >
      </Table>
      <div class="pageCon" :style="isMagnify && 'padding-right:30px'">
        <Page
          v-show="isPage"
          ref="pagenate"
          :total="total"
          :current="searchData.pageNum"
          @on-change="changePage"
          @on-page-size-change="changPageSize"
          :page-size="searchData.pageSize"
          :page-size-opts="[10, 20, 50]"
          :show-sizer="true"
          :show-total="true"
        >
        </Page>
        <div class="operateBtn" style="width: 30px" v-if="isMagnify">
          <Icon @click="openModal" size="24" class="magBtn" type="ios-search" />
        </div>
      </div>
      <!-- 说明文字 -->
      <div class="explainClass" v-show="isExplain">
        <p v-for="(item, index) in tableExplain" :key="index">{{ item }}</p>
      </div>
    </div>
  </div>
</template>
<script>
import { postDataList, getList } from "../../api/dataRequst";
export default {
  name: "ListSearch",
  components: {},
  data() {
    return {
      loading: false,
      searchData: {
        pageNum: 1,
        pageSize: 20,
      },
      total: 0,
      listData: [], //数据
      searchVal: "", //检索内容
    };
  },
  props: {
    //表格高度
    iHeight: {
      type: Number,
      default: () => {
        return 550;
      },
    },
    //标题
    titleName: {
      type: String,
      default: () => {
        return "";
      },
    },
    //操作样式
    operateClass: {
      type: String,
      default: () => {
        return "";
      },
    },
    //接口
    url: {
      type: String,
      default: () => {
        return "";
      },
    },
    //是否分页
    isPage: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //查询项
    searchOtherData: {
      type: Object,
      default: () => {
        return {};
      },
    },
    //列表头
    listCols: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //按钮信息
    btnList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    //是否展示搜索放大镜
    isMagnify: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //放大镜搜索key值
    searchKey: {
      type: String,
      default: () => {
        return "searchKey";
      },
    },
    //默认的按钮操作是有搜索项，内部
    isIn: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //查询
    isSearch: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //重置
    isReset: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //内部按钮额外按钮
    isInsideBtn: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //外部操作
    isOut: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //多选表格
    isTable: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //是否默认查询，默认为false
    isFirst: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //是否展示标题，默认为true
    isTitle: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //表格标题
    isTbaleTitle: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //表格标题名字
    tbaletitleName: {
      type: String,
      default: () => {
        return "";
      },
    },
    //表格内按钮样式
    tableBtnClass: {
      type: String,
      default: () => {
        return "";
      },
    },
    //表格按钮
    isTableBtn: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //表格的外层样式
    topClass: {
      type: String,
      default: () => {
        return "";
      },
    },
    //是否展示表格下方文字说明
    isExplain: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //表格下面说明文字
    tableExplain: {
      type: Array,
      default: () => {
        return [];
      },
    },
    // 切换页签时在父组件是否需要触发其他方法
    isPageChangeOther: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
  },
  //使用
  inject: ["showLoading", "hideLoading"],
  created() {},
  mounted() {
    let that = this;
    document.onkeydown = function (e) {
      let key = window.event.keyCode;
      if (key === 13) {
        that.query();
      }
    };
    if (this.isFirst) this.query();
  },
  methods: {
    //打开放大镜
    openModal() {
      this.searchVal = "";
      this.$Modal.confirm({
        render: (h) => {
          return h("Input", {
            props: {
              value: this.searchVal,
              autofocus: true,
              placeholder: "请输入检索内容...",
            },
            style: {
              paddingTop: "20px",
            },
            on: {
              input: (val) => {
                this.searchVal = val;
              },
            },
          });
        },
        onOk: () => {
          this.query();
        },
        onCancel: () => {},
      });
    },
    changePage(index) {
      //index显示的页码
      this.searchData.pageNum = index;
      if (this.isPageChangeOther) {
        this.$emit("isPageChangeOther");
      }
      this.initTable();
    },
    changPageSize(pageSize) {
      this.searchData.pageSize = pageSize;
      this.searchData.pageNum = 1;
      if (this.isPageChangeOther) {
        this.$emit("isPageChangeOther");
      }
      this.initTable();
    },
    async initTable(listType) {
      let that = this,
        postData = Object.assign({}, this.searchOtherData);
      //第一次调用列表，需处理页码
      if (this.isPage) {
        postData = Object.assign({}, this.searchOtherData, this.searchData);
        if (listType == "firstList") {
          postData = Object.assign({}, this.searchData, this.searchOtherData);
          this.$refs.pagenate.currentPage = this.searchOtherData.pageNum;
          //this.$refs.pagenate.pageSize = this.searchOtherData.pageSize;
        }
      }
      //是否放大镜里搜索
      if (this.isMagnify) postData[this.searchKey] = this.searchVal;
      this.loading = true;
      console.log(postData);
      //return false;
      this.showLoading();
      try {
        let res = await postDataList(that.url, postData);
        let data = res.data;
        this.hideLoading();
        this.loading = false;
        if (data.status == "success") {
          this.listData = this.isPage ? data.result.list : data.result;
          if (this.isPage) this.total = data.result.total; //总条数
        } else {
          this.$Message.error({
            title: "信息",
            duration: 5,
            closable: true,
            content: data.message,
          });
        }
      } catch (error) {
        this.loading = false;
        this.hideLoading();
        this.$Message.error({
          title: "信息",
          duration: 5,
          closable: true,
          content: "请求失败",
        });
      }
    },
    //查询
    query(listType) {
      this.loading = false;
      if (this.isPage) this.$refs.pagenate.currentPage = 1;
      this.searchData.pageNum = 1;
      this.initTable(listType);
    },
    //重置
    reset() {
      this.searchData = {
        pageNum: 1,
        pageSize: 20,
      };
      if (this.isPage) this.$refs.pagenate.currentPage = 1;
      this.initTable();
    },
    //操作
    btnOperate(item) {
      this.$emit("btnOperate", item);
    },
    //多选的处理
    selectItem(selection, row) {
      let that = this;
      that.$emit("selectItem", selection);
    },
    cancleItem(selection, row) {
      let that = this;
      that.$emit("selectItem", selection);
    },
    selectAllItem(selection, row) {
      let that = this;
      that.$emit("selectItem", selection);
    },
    selectAllItemCancel(selection, row) {
      let that = this;
      that.$emit("selectItem", selection);
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.wrapper {
  position: relative;
}
</style>