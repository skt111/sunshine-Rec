<template>
  <div class="rightCon">
    <div class="content">
      <div class="risk">
        <p>
          <span class="titleTheme">查询条件</span>
        </p>
        <div class="formLine">
          <div class="formInput">
            <span class="leftSpan">用户信息：</span>
            <input
              type="text"
              class="input"
              v-model="RmsIndexDto.userQueryKey"
              placeholder="请输入用户姓名模糊查询"
              style="width:65%;"
            />
          </div>
          <div class="formInput">
            <span class="leftSpan">更新日期：</span>
    <DatePicker @on-change="dataValueStart" :options="optionsStart" class="inputDate" v-model="RmsIndexDto.updateStartTime"></DatePicker>
      <span class="lineSpan">至</span>
      <DatePicker @on-change="dataValueEnd" :options="optionsEnd" class="inputDate" v-model="RmsIndexDto.updateEndTime"></DatePicker>
      
          </div>
        </div>
        <div class="formLine txtCenter">
          <a
            class="resetBtn mr10"
            @click="query"
            v-if="isView"
          >查询</a>
          <a
            class="resetBtn mr10"
            @click="reset"
            v-if="isView"
          >重置</a>
          <!-- <a class="resetBtn" @click="reBack" v-if="isView">返回</a> -->
        </div>
      </div>
      <div class="risk">
        <p>
          <span class="titleTheme">调整日志</span>
          <a
            class="resetBtn txtCenter addBtn"
            @click="exportFun"
            v-if="isView"
            style="margin-top: 3px;"
          >导出</a>
        </p>
        <Table
          :data="userData"
          :columns="userCols"
          stripe
          border
          :loading="loading"
          :row-class-name="rowClassName"
        >
        </Table>
        <div style="margin: 10px;overflow: hidden">
          <div style="float: right;">
            <Page
              ref="pagenate"
              :total="total"
              :current="1"
              @on-change="changePage"
              @on-page-size-change="changPageSize"
              :page-size-opts="[10, 20, 30, 40]"
              :show-sizer="true"
              :show-total="true"
            ></Page>
          </div>
        </div>
      </div>
    </div>
    <Loading ref="loading"></Loading>
    <Dialog
      ref="dialog"
      :content="content"
      :type="dialogType"
      @onOkTo="dialogOk"
    ></Dialog>
  </div>
</template>
<script>
import Dialog from "@/components/Dialog";
import "../../assets/role.scss";
import Loading from "@/components/Loading";
import util from "@/utils/util";
export default {
  name: "Logs",
  components: {
    Dialog,
    Loading
  },
  data() {
    return {
      isView: true, //查看权限
      content: "", //弹框展示内容
      dialogType: "sure", //什么类型的弹框
      loading: true,
      optionsStart: {},
      optionsEnd: {},
      RmsIndexDto: {
        userQueryKey: "",
        updateStartTime:util.getNowLastMonth().lastdate,
        updateEndTime:util.getNowLastMonth().currdate,
        pageNum: "1",
        pageSize: 10
      },
      total: 0,
      url: "/auMana/authChangeLog/queryAuthChangeLogPage",
      userData: [],
      userCols: [
        {
          title: "序号",
          type: "index",
          width: 70,
          align: "center"
        },

        {
          title: "用户账户",
          key: "oprId",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true
        },
        {
          title: "姓名",
          key: "userName",
          minWidth: 90,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "调整前角色",
          key: "beforeRole",
          minWidth: 110,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "调整后角色",
          key: "afterRole",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "调整前状态",
          key: "beforeStatus",
          align: "center",
          minWidth: 90,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "调整后状态",
          key: "afterStatus",
          align: "center",
          minWidth: 90,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "更新日期",
          key: "updateTime",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "更新人",
          key: "updateUser",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        }
      ]
    };
  },
  created() {
    this.$store.commit("focusId", { focusId: "A0104" });
  },
  mounted() {
    let that = this;
    document.onkeydown = function(e) {
      let key = window.event.keyCode;
      if (key === 13) {
        that.query();
      }
    };
    this.query();
  },
  methods: {
      //开始结束时间校验
    dataValueStart(e) {
      let that = this;
      this.optionsEnd = {
        disabledDate(date) {
          if (that.RmsIndexDto.updateEndTime) {
            if (that.RmsIndexDto.updateStartTime) {
              return (
                date.getTime() <= new Date(that.RmsIndexDto.updateStartTime).getTime()
              );
            } else {
              return date.getTime() >= new Date(that.RmsIndexDto.updateEndTime).getTime();
            }
          } else {
            return date.getTime() <= new Date(e).getTime();
          }
        }
      };
      this.RmsIndexDto.updateStartTime = e;
    },
    dataValueEnd(e) {
      let that = this;
      this.optionsStart = {
        disabledDate(date) {
          if (that.RmsIndexDto.updateStartTime) {
            if (that.RmsIndexDto.updateEndTime) {
              return date.getTime() >= new Date(that.RmsIndexDto.updateEndTime).getTime();
            } else {
              return date.getTime() <= new Date(that.RmsIndexDto.updateStartTime).getTime();
            }
          } else {
            return date.getTime() >= new Date(e).getTime();
          }
        }
      };
      this.RmsIndexDto.updateEndTime = e;
    },
    rowClassName(row, index) {
      if (row.status == "A") {
        return "disabled-table";
      }
      return "";
    },
    changePage(index) {
      this.loading = true;
      //index显示的页码
      this.RmsIndexDto.pageNum = index;
      this.initTable();
    },
    changPageSize(pageSize) {
      this.loading = true;
      this.RtmsIndexDto.pageSize = pageSize;
      this.RmsIndexDto.pageNum = "1";
      this.initTable();
    },
    initTable() {
      let that = this;
      that.$refs.loading.show();
      that.axios
        .post(that.url, that.RmsIndexDto)
        .then(res => {
          let data = res.data;
          that.$refs.loading.hide();
          that.loading = false;
          if (data.status == "success") {
            that.userData = data.result.list;
            that.total = data.result.total; //总条数
          } else {
            that.sure(data.message);
          }
        })
        .catch(error => {
          that.loading = false;
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    //查询
    query() {
      if(this.RmsIndexDto.updateStartTime==''&&this.RmsIndexDto.updateEndTime!=''){
        this.$Modal.error({
          title:"提示",
          content:"请选择更新开始时间"
        });
        return false;
      }
      this.loading = true;
      this.$refs.pagenate.currentPage = 1;
      this.RmsIndexDto.pageNum = 1;
      this.RmsIndexDto.updateStartTime=this.RmsIndexDto.updateStartTime?util.changeDay(this.RmsIndexDto.updateStartTime):'';
      this.RmsIndexDto.updateEndTime=this.RmsIndexDto.updateEndTime?util.changeDay(this.RmsIndexDto.updateEndTime)+" 23:59:59":'';
      this.initTable();
    },
    //重置
    reset() {
      this.RmsIndexDto = {
        userQueryKey: "",
        updateStartTime:util.getNowLastMonth().lastdate,
        updateEndTime:util.getNowLastMonth().currdate,
        pageNum: "1",
        pageSize: 10
      };
      this.$refs.pagenate.currentPage = 1;
    },
    
    dialogOk() {
      this.reset();
      this.initTable();
    },
    //导出
    exportFun() {
      let that = this;
      this.RmsIndexDto.updateStartTime=this.RmsIndexDto.updateStartTime?util.changeDay(this.RmsIndexDto.updateStartTime):'';
      this.RmsIndexDto.updateEndTime=this.RmsIndexDto.updateEndTime?util.changeDay(this.RmsIndexDto.updateEndTime)+" 23:59:59":'';
      this.axios
        .post("/auMana/authChangeLog/downloadAuthChangeLog", that.RmsIndexDto, {
          responseType: "blob"
        })
        .then(response => {
          if (response.status == 200) {
            util.downloadStream(response);
          } else {
            that.sure(response.statusText);
          }
        })
        .catch(error => {
          that.sure("导出失败！");
        });
    },
    sure(content) {
      console.log(content);
      this.content = content;
      this.$refs.dialog.show();
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.content {
  margin-left: 16px;
}
/deep/.ivu-table-header thead tr th {
  padding: 8px 0px;
}
/deep/ .disabled-table {
  background-color: #bfc0c1 !important;
}
</style>
