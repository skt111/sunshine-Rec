<template>
  <div class="rightCon">
    <div class="content">
      <div class="risk">
        <p>
          <span class="titleTheme">查询条件</span>
        </p>
        <div class="formLine">
          <div class="formInput">
            <span class="leftSpan">用户姓名：</span>
            <input
              type="text"
              class="input"
              v-model="RmsIndexDto.queryKey"
              placeholder="请输入用户姓名模糊查询"
              style="width:65%;"
            />
          </div>
          <div class="formInput">
            <span class="leftSpan">开通时间：</span>
            <DatePicker
              @on-change="dataValueStart"
              :options="optionsStart"
              class="inputDate"
              v-model="RmsIndexDto.openTimeStart"
            ></DatePicker>
            <span class="lineSpan">至</span>
            <DatePicker
              @on-change="dataValueEnd"
              :options="optionsEnd"
              class="inputDate"
              v-model="RmsIndexDto.openTimeEnd"
            ></DatePicker>

          </div>
        </div>
        <div class="formLine">
          <div class="formInput">
            <span class="leftSpan">组织信息：</span>
            <input
              type="text"
              class="input"
              v-model="RmsIndexDto.orgQueryKey"
              placeholder="请输入员工所在公司、机构或部门关键词"
              style="width:65%;"
            />
          </div>
          <div class="formInput">
            <span class="leftSpan">数据范围：</span>
            <MutilTree
              ref="orgTree"
              @onSelect="selectOrg"
            ></MutilTree>
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
          <span class="titleTheme">权限报表</span>
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
import MutilTree from "@/views/authManage/MutilTree";
import util from "@/utils/util";
export default {
  name: "Reports",
  components: {
    MutilTree,
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
        queryKey: "",
        orgQueryKey: "",
        dataRange: [],
        openTimeStart: util.getNowLastMonth().lastdate,
        openTimeEnd: util.getNowLastMonth().currdate,
        pageNum: "1",
        pageSize: 10
      },
      total: 0,
      url: "/auMana/authReport/queryAuthReportData",
      userData: [],
      userCols: [
        {
          title: "序号",
          type: "index",
          width: 70,
          align: "center"
        },

        {
          title: "姓名",
          key: "name",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true
        },
        {
          title: "EHR用户名",
          key: "stuId",
          minWidth: 110,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "所属公司",
          key: "companyName",
          minWidth: 110,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "所属机构",
          key: "estabName",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "数据范围",
          key: "dateRangeStr",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "开通时间",
          key: "createTime",
          align: "center",
          minWidth: 90,
          ellipsis: true,
          tooltip: true
        },
        {
          title: "账号状态",
          key: "status",
          align: "center",
          minWidth: 90,
          ellipsis: true,
          tooltip: true
        }
      ]
    };
  },
  created() {
    this.$store.commit("focusId", { focusId: "A0103" });
  },
  mounted() {
    let that = this;
    document.onkeydown = function(e) {
      let key = window.event.keyCode;
      if (key === 13) {
        that.query();
      }
    };
    this.initHead();
    this.query();
  },
  methods: {
    //开始结束时间校验
    dataValueStart(e) {
      let that = this;
      this.optionsEnd = {
        disabledDate(date) {
          if (that.RmsIndexDto.openTimeEnd) {
            if (that.RmsIndexDto.openTimeStart) {
              return (
                date.getTime() <=
                new Date(that.RmsIndexDto.openTimeStart).getTime()
              );
            } else {
              return (
                date.getTime() >=
                new Date(that.RmsIndexDto.openTimeEnd).getTime()
              );
            }
          } else {
            return date.getTime() <= new Date(e).getTime();
          }
        }
      };
      this.RmsIndexDto.openTimeStart = e;
    },
    dataValueEnd(e) {
      let that = this;
      this.optionsStart = {
        disabledDate(date) {
          if (that.RmsIndexDto.openTimeStart) {
            if (that.RmsIndexDto.openTimeEnd) {
              return (
                date.getTime() >=
                new Date(that.RmsIndexDto.openTimeEnd).getTime()
              );
            } else {
              return (
                date.getTime() <=
                new Date(that.RmsIndexDto.openTimeStart).getTime()
              );
            }
          } else {
            return date.getTime() >= new Date(e).getTime();
          }
        }
      };
      console.log(e)
      this.RmsIndexDto.openTimeEnd = e;
    },
    //机构信息
    selectOrg(data) {
      this.RmsIndexDto.dataRange = data;
    },
    rowClassName(row, index) {
      if (row.status == "A") {
        return "disabled-table";
      }
      return "";
    },
    //表头处理
    changeStructure(item) {
      var that = this,
        nodes = {};
      nodes.title = item.menuName;
      nodes.key = item.menuCode;
      nodes.minWidth = 100;nodes.align = "center";
      nodes.tooltip = true;
      nodes.ellipsis= true;
      if (item.childMenu && item.childMenu.length > 0) {
        var ones = [];
        item.childMenu.forEach(ele => {
          ones.push(that.changeStructure(ele));
        });
        nodes.children = ones;
      } else {
        nodes.render = (h, param) => {
          var icon = "",
            codes = util.getKeyList(param.row.powerMenuExtList, "menuCode");
          if (codes.indexOf(item.menuCode) > -1) {
            icon = h("Icon", {
              props: {
                type: "md-checkmark",
                size: 24
              }
            });
          }
          return h("div", [icon]);
        };
      }
      return nodes;
    },
    //初始化表头
    initHead() {
      var detailCols = [];
      var that = this;
      //查出表头数据存在headCols
      this.axios
        .post("/auMana/authReport/queryAuthTableHead")
        .then(res => {
          let data = res.data;
          if (data.status == "success") {
            data.result.forEach(item => {
              detailCols.push(that.changeStructure(item));
            });
            console.log(detailCols);
            this.userCols = this.userCols.concat(detailCols);
          } else {
            this.$Message.error({
              title: "信息",
              content: data.message
            });
          }
        })
        .catch(error => {
          this.$Message.error({
            title: "信息",
            content: error
          });
        });
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
      this.$refs.pagenate.currentPage = 1;
      this.RmsIndexDto.pageNum = 1;
      if(this.RmsIndexDto.openTimeStart==''&&this.RmsIndexDto.openTimeEnd!=''){
        this.$Modal.error({
          title:"提示",
          content:"请选择开通开始时间"
        });
        return false;
      }
      this.RmsIndexDto.openTimeStart=this.RmsIndexDto.openTimeStart?util.changeDay(this.RmsIndexDto.openTimeStart):'';
      this.RmsIndexDto.openTimeEnd=this.RmsIndexDto.openTimeEnd?util.changeDay(this.RmsIndexDto.openTimeEnd)+" 23:59:59":'';
      this.loading = true;
      this.initTable();
    },
    //重置
    reset() {
      this.RmsIndexDto = {
        queryKey: "",
        orgQueryKey: "",
        dataRange: [],
        openTimeStart: util.getNowLastMonth().lastdate,
        openTimeEnd: util.getNowLastMonth().currdate,
        pageNum: "1",
        pageSize: 10
      };
      this.$refs.pagenate.currentPage = 1;
      this.$refs.orgTree.cancel();
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
        .post("/auMana/authReport/downloadAuthReport", that.RmsIndexDto, {
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
