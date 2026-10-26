<template>
  <div class="tableCon" :style="'min-height:' + iHeight + 'px'">
    <Table disabled-hover :class="[tableClass || 'zzxxTable']" key="zzxxTable" :data="dataList" :columns="headData"
      border>
    </Table>
    <div style="text-align: right; margin-bottom: 10px; position: relative" v-show="canAdd">
      <a v-if="dataList.length == 0 && isEdit" class="but primary" @click="addLine"
        style="position: absolute; right: 10px; top: -48px">添加一行</a>
    </div>
    <div v-if="btnList.length > 0">
      <a v-for="(item, idx) in btnList" :key="'btn' + idx" class="but primary ml10" @click="btnOperate(item)"
        style="margin-top: 3px">{{ item.name }}</a>
    </div>
    <div :class="['pageCon', isMagnify && 'magCon']">
      <Page v-if="isPage" transfer :total="total" :current="pageData.pageNum" ref="pagenate" @on-change="changePage"
        @on-page-size-change="changPageSize" :page-size="pageData.pageSize" :page-size-opts="[10, 20, 50]"
        :show-sizer="true" :show-total="true"></Page>
      <div class="operateBtn" v-if="isDefault">
        <a class="but" @click="exportInfo">下载</a>
        <Icon @click="openModal" size="24" class="magBtn" v-if="isMagnify" type="ios-search" />
      </div>
      <a class="addBtn" @click="addInfo" v-show="isAdd">{{ addName }}</a>
    </div>
    <Loading ref="loading"></Loading>
  </div>
</template>

<script>
  import Loading from "@/components/Loading";
  import util from "@/utils/util";
  import { postFileList, getFileList } from "../../api/dataRequst";
  export default {
    name: "EditTable",
    components: {
      Loading,
    },
    data() {
      return {
        //分页信息
        pageData: {
          pageNum: 1,
          pageSize: 20,
        },
        //数据信息
        configList: [],
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
      //表格定位
      tableClass: {
        type: String,
        default: () => {
          return "";
        },
      },
      //表头信息
      headData: {
        type: Array,
        default: () => {
          return [];
        },
      },
      //是否分页
      isPage: {
        type: Boolean,
        default: () => {
          return false;
        },
      },
      //列表信息
      dataList: {
        type: Array,
        default: () => {
          return [];
        },
      },
      total: {
        type: Number,
        default: () => {
          return 0;
        },
      },
      //是否显示添加一行
      canAdd: {
        type: Boolean,
        default: true,
      },
      //是否显示操作
      isHandle: {
        type: Boolean,
        default: true,
      },
      //是否可编辑
      isEdit: {
        type: Boolean,
        default: true,
      },
      //下载的url
      downUrl: {
        type: String,
        default: "",
      },
      //下载的url请求方式
      downType: {
        type: String,
        default: "post",
      },
      //下载的数据
      downPostData: {
        type: Object,
        default: () => {
          return {};
        },
      },
      //添加按钮
      isAdd: {
        type: Boolean,
        default: () => {
          return true;
        },
      },
      //添加的名字
      addName: {
        type: String,
        default: () => {
          return "添加";
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
      //按钮信息
      btnList: {
        type: Array,
        default: () => {
          return [];
        },
      },
      //默认的按钮操作是否显示，下载
      isDefault: {
        type: Boolean,
        default: () => {
          return true;
        },
      },
    },
    watch: {
      dataList: {
        handler(newVal, oldVal) {
          this.configList = newVal;
        },
        deep: true,
      },
    },
    computed: {},
    filters: {
      formatDate(time) {
        return typeof time == "string" ? time : util.changeDate(time);
      },
    },
    created() { },
    mounted() { },
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
            setTimeout(() => {
              this.changPageSize(20);
            }, 500);
          },
          onCancel: () => { },
        });
      },
      //精确查询
      changePage(index,type) {
        // The simulated data is changed directly here, and the actual usage scenario should fetch the data from the server
        //index显示的页码
        let old = {...this.pageData};
        this.pageData.pageNum = index;
        if(type=='no'){
          if (this.$refs.pagenate) this.$refs.pagenate.currentPage = index;
          return false;
        }
        this.initData(Object.assign({
          typeOp: 'page'
        }, old));
      },
      changPageSize(pageSize,type,pageNum) {
        let old = {...this.pageData};
        this.pageData.pageSize = pageSize;
        if(type=='no'){
          this.pageData.pageNum = pageNum;
          if (this.$refs.pagenate) this.$refs.pagenate.currentPage = pageNum;
          return false;
        }
        this.pageData.pageNum = 1;
        if (this.$refs.pagenate) this.$refs.pagenate.currentPage = 1;
        this.initData(Object.assign({
          typeOp: 'pageSize'
        }, old));
      },
      //页码信息查询
      initData(oldData) {
        let postData = Object.assign({}, this.pageData);
        if (this.isMagnify) {
          postData[this.searchKey] = this.searchVal;
        }
        this.$emit("pageSearch", postData, oldData);
      },
      //添加一行
      addLine() {
        this.$emit("addLine");
      },
      addInfo() {
        this.$emit("addInfo");
      },
      //操作
      btnOperate(item) {
        this.$emit("btnOperate", item);
      },
      //下载
      async exportInfo() {
        this.$refs.loading.show();
        let reqList = {
          post() {
            return postFileList;
          },
          get() {
            return getFileList;
          },
        };
        try {
          let res = await reqList[this.downType]()(
            process.env.VUE_APP_CONFIG + this.downUrl,
            this.downPostData
          );
          this.$refs.loading.hide();
          if (res.status == 200) {
            util.downloadStream(res);
          } else {
            this.$Message.error({
              title: "信息",
              duration: 5,
              closable: true,
              content: res.statusText,
            });
          }
        } catch (e) {
          this.$refs.loading.hide();
          //弹框提示：请求失败
          this.$Message.error({
            title: "信息",
            duration: 5,
            closable: true,
            content: "请求失败",
          });
        }
      },
    },
    beforeRouteEnter(to, from, next) {
      window.document.body.style.backgroundColor = "#fff";
      next();
    },
    beforeRouteLeave(to, from, next) {
      window.document.body.style.backgroundColor = "#eeeeee";
      next();
    },
  };
</script>

<style lang="scss" scoped>
  .magCon {
    padding-right: 90px;

    .operateBtn {
      width: 90px;
    }

    .magBtn {
      position: absolute;
      right: 70px;
    }
  }

  .addBtn {
    position: absolute;
    left: 0px;
    top: 0px;
    background: var(--theme_color);
    border: 1px solid Var(--theme_color);
    color: #fff;
    height: 28px;
    line-height: 26px;
    padding: 0 16px;
    border-radius: 4px;
  }
</style>