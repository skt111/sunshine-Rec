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
              v-model="RmsIndexDto.queryKey"
              placeholder="请输入用户id、姓名或姓名拼音"
              style="width:65%;"
            />
          </div>
          <div class="formInput">
            <span class="leftSpan">角色名称：</span>
            <MultipleSelect
              :values="queryRoleNames"
              :selectList="rolesList"
              @onchange="roleChange"
              ref="roleCurr"
            ></MultipleSelect>
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
            <span class="leftSpan">当前状态：</span>
            <select class="input" v-model="RmsIndexDto.status" style="width:65%;">
              <option value="">全部</option>
              <option value="I">禁用</option>
              <option value="A">启用</option>
            </select>
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
          <span class="titleTheme">查询结果</span>
          <a
            class="resetBtn txtCenter addBtn"
            @click="exportFun"
            v-if="false"
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
    <div
      v-if="showDialog"
      class="ivu-modal-mask"
      style="z-index:1000;"
    ></div>
    <Modal
      v-model="showDialog"
      :mask-closable="false"
      class-name="detailModal"
      scrollable
      draggable
      width="500"
    >
      <p slot="header">
        <span style="color:#fff;">角色配置</span>
      </p>
      <div class="baseTable">
        <table>
          <tr><td width="16%">姓名：</td><td width="34%">{{currentSelect.name}}</td><td width="16%">用户ID：</td><td width="34%">{{currentSelect.stuId}}</td></tr>
          <tr><td>工号：</td><td>{{currentSelect.stuOprid}}</td><td>在职状态：</td><td>{{currentSelect.status=='A'?"启用":"禁用"}}</td></tr>
          <tr><td>所在单位：</td><td colspan="3">{{currentSelect.estabName}}</td></tr>
          <tr><td>所在部门：</td><td colspan="3">{{currentSelect.deptName}}</td></tr>
          <tr><td>所在岗位：</td><td colspan="3">{{currentSelect.pstnDescr}}</td></tr>
        </table>
      </div>
      <div
        class="treeCon"
        v-if="showDialog"
      >
        <Tree
          :data="positionList"
          ref="tree"
          @on-check-change="positionChecked"
          show-checkbox
          multiple
        ></Tree>
      </div>
      <div
        slot="footer"
        style="text-align:center;"
      >
        <Button
          type="primary"
          size="large"
          @click="ok()"
        >确定</Button>
        <Button
          type="primary"
          size="large"
          @click="cancel()"
        >取消</Button>
      </div>
    </Modal>
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
import MultipleSelect from "@/components/MultipleSelect";
import Loading from "@/components/Loading";
import util from "@/utils/util";
export default {
  name: "UserManage",
  components: {
    MultipleSelect,
    Dialog,
    Loading
  },
  data() {
    return {
      isView: true, //查看权限
      statusete: true, //删除权限
      isConfigrole: true, //角色配置
      content: "", //弹框展示内容
      dialogType: "sure", //什么类型的弹框
      loading: true,
      RmsIndexDto: {
        queryKey: "",
        orgQueryKey:"",//组织信息
        queryRoleNames: [""], //角色名称
        status:"",//当前状态
        pageNum: "",
        pageSize: 10
      },
      queryRoleNames: [], //角色名称
      rolesList: [], //角色列表
      showDialog: false,
      positionList: [],
      currentSelect: {},
      total: 0,
      url: "/auMana/authorityHr/student/queryPage",
      userData: [],
      userCols: [
        {
          title: "序号",
          type: "index",
          width: 70,
          align: "center"
        },

        {
          title: "用户账号",
          key: "stuId",
          minWidth: 110,
          align: "center"
        },
        {
          title: "姓名",
          key: "name",
          width: 90,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "部门名称",
          key: "deptName",
          minWidth: 150,
          ellipsis: true,
          tooltip: true,
          align: "center"
        },
        {
          title: "角色名称",
          key: "roleNames",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true
        }
      ]
    };
  },
  created() {
    this.$store.commit("focusId", { focusId: "A0101" });
    let that = this,
      operatePosition = {
        title: "角色",
        key: "",
        align: "center",
        width: 100,
        render: (h, params) => {
          return h("div", [
            h(
              "Button",
              {
                props: {
                  type: "primary",
                  size: "small"
                },
                on: {
                  click: () => {
                    //配置角色
                    this.stationConfig(params.row);
                  }
                }
              },
              "配置角色"
            )
          ]);
        }
      },
      operateUser = {
        title: "操作",
        key: "",
        align: "center",
        width: 170,
        render: (h, params) => {
          let subOperate = [],
            del = h(
              "Button",
              {
                props: {
                  type: "primary",
                  size: "small"
                },
                on: {
                  click: () => {
                    this.delUser(params.row);
                  }
                }
              },
              params.row.status == "A" ? "禁用" : "启用"
            );
          subOperate.push(del);
          return h("div", subOperate);
        }
      };
    if (that.isConfigrole) that.userCols.push(operatePosition);
    if (that.statusete) that.userCols.push(operateUser);
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
    this.position();
  },
  methods: {
    //角色名称下拉选择
    roleChange(values) {
      console.log(values.selectVal);
      if(values.selectVal.length==1&&values.selectVal[0]==''){
      this.RmsIndexDto.queryRoleNames = [];
      }else{
      this.RmsIndexDto.queryRoleNames = values.selectVal;
      }
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
    //角色信息查询
    position() {
      let that = this;
      this.axios
        .post("/auMana/authorityHr/role/queryAll", {})
        .then(res => {
          let data = res.data;
          if (data.status == "success") {
            that.loading = false;
            //处理角色树
            data.result.forEach((item, index) => {
              item.code=item.id;
              item.content=item.roleName;
              item.title =
                item.roleName.length > 18
                  ? item.roleName.substring(0, 30) + "..."
                  : item.roleName;
            });
            that.positionList = data.result;
            that.rolesList = util.arrDeepCopy(data.result);
          } else {
            that.sure(data.message);
          }
        })
        .catch(error => {
          that.sure(error);
        });
    },
    //查询
    query() {
      this.loading = true;
      this.$refs.pagenate.currentPage = 1;
      this.RmsIndexDto.pageNum = 1;
      this.initTable();
    },
    //重置
    reset() {
      this.RmsIndexDto = {
        queryKey: "",
        orgQueryKey:"",
        status:"",
        pageNum: "1",
        pageSize: 10
      };
      this.$refs.pagenate.currentPage = 1;
      this.$refs.roleCurr.refresh();
    },
    //启用，禁用
    delUser(params) {
      let that = this;
      that.$Modal.confirm({
        title: "信息",
        content: "确定" + (params.status == "A" ? "禁用" : "启用") + "用户吗？",
        onOk: () => {
          that.axios
            .post("/auMana/authorityHr/student/updateStatus", {
              stuId: params.stuId,
              status: params.status == "A" ? "I" : "A"
            })
            .then(res => {
              if (res.data.status == "success") {
                that.dialogType = "sureTo";
                that.content = res.data.message;
                that.$refs.dialog.show();
              } else {
                that.sure(res.data.message);
              }
            })
            .catch(error => {
              that.sure(error);
            });
        },
        onCancel: () => {}
      });
    },
    //配置角色
    stationConfig(params) {
      let that = this;
      that.showDialog = true;
      //初始化赋值状态
      that.positionList.map(function(items) {
        that.$set(items, "checked", false);
      });
      this.axios
        .post("/auMana/authorityHr/student/userSelectRole", {
          stuId: params.stuId
        })
        .then(res => {
          let data = res.data;
          if (data.status == "success") {
            //赋值选中项
            that.currentSelect = Object.assign({}, params);
            that.currentSelect.roleIdList =
              data.result == null ? [] : data.result;
            that.currentSelect.roleIdList.forEach(function(item) {
              that.positionList.map(function(items) {
                if (item.roleId == items.id) {
                  //id有改动stuId
                  that.$set(items, "checked", true);
                }
              });
            });
          } else {
            that.sure(data.message);
          }
        })
        .catch(error => {
          that.sure(error);
        });
    },
    //选中角色数据
    positionChecked(values) {
      let that = this,
        roleId = [];
      values.map(item => {
        roleId.push(item.id);
      });
      that.currentSelect.roleIdList = roleId;
    },
    //提交角色数据
    ok() {
      let that = this,
        roleId = [],
        noUpdate = false;
      that.$refs.loading.show();
      if (this.currentSelect.roleIdList.length > 0) {
        if (typeof that.currentSelect.roleIdList[0] == "object") {
          noUpdate = true;
          that.currentSelect.roleIdList.map(item => {
            roleId.push(item.roleId);
          });
        }
      }
      if (noUpdate) {
        that.currentSelect.roleIdList = roleId;
      }
      that.axios
        .post("/auMana/authorityHr/student/configRole", {
          stuId: that.currentSelect.stuId,
          roleIds: that.currentSelect.roleIdList
        })
        .then(res => {
          let data = res.data;
          that.$refs.loading.hide();
          if (data.status == "success") {
            that.showDialog = false;
            this.$refs.dialog.show();
            that.dialogType = "sureTo";
            that.content = data.message;
          } else {
            that.sure(data.message);
          }
        })
        .catch(error => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    cancel() {
      this.showDialog = false;
    },
    dialogOk() {
      this.reset();
      this.initTable();
    },
    //返回
    reBack() {
      this.reset();
      // this.initTable();
    },
    //导出
    exportFun(){
      let that = this;
      this.axios
        .post(
          "/adEds/payAddTmp/downloadWithEmpl",
          that.RmsIndexDto,
          {
            responseType: "blob"
          }
        )
        .then(response => {
          if (response.status == 200) {
            util.downloadStream(response);
          } else {
            that.sure(response.statusText);
          }
        })
        .catch(error => {
          that.sure('导出失败！');
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
