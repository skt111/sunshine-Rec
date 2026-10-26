<template>
  <div class="rightCon">
    <div class="content">
      <div class="conTitle">角色管理</div>
      <div class="risk">
        <p>
          <span class="titleTheme">查询条件</span>
        </p>
        <div class="formLine">
          <div class="formInput">
            <span class="leftSpan">角色名称：</span>
            <input
              type="text"
              style="width:65%;"
              class="input"
              placeholder="请输入角色名称模糊查询"
              :maxlength="50"
              v-model="RmsIndexDto.queryKey"
            />
          </div>
          <div class="formInput">
            <span class="leftSpan">菜单按钮：</span>
            <MultipleSelect
              :values="menuNameList"
              :selectList="menusList"
              @onchange="menuChange"
              ref="menuCurr"
            ></MultipleSelect>
          </div>
        </div>
        <div class="formLine">
          <div class="formInput">
            <span class="leftSpan">所属机构：</span>
            <MutilTree
              ref="orgTree"
              @onSelect="selectOrg"
            ></MutilTree>
          </div>
          <div class="formInput">
            <span class="leftSpan">当前状态：</span>
            <select
              v-model="RmsIndexDto.isDel"
              class="input"
              style="width:65%;"
            >
              <option value="">全部</option>
              <option value="Y">启用</option>
              <option value="N">禁用</option>
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
            class="resetBtn"
            @click="reset"
            v-if="isReset"
          >重置</a>
        </div>
      </div>
      <div class="risk">
        <p>
          <span class="titleTheme">查询结果</span>
          <a
            class="resetBtn txtCenter addBtn"
            @click="addStation"
            v-if="isAdd"
            style="margin-top: 3px;"
          >新增</a>
        </p>
        <Table
          :data="positionData"
          :columns="positionCols"
          :rowKey="true"
          stripe
          border
          :loading="loading"
        ></Table>
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
      v-if="addDialog || showDialog"
      class="ivu-modal-mask"
      style="z-index:1000;"
    ></div>
    <Modal
      v-model="addDialog"
      :mask-closable="false"
      class-name="detailModal"
      scrollable
      draggable
      width="500"
    >
      <p slot="header">
        <span style="color:#fff;">{{ roleTitle }}</span>
      </p>
      <div>
        <div class="formLine3">
          <div class="formInput">
            <span class="leftSpan"><label>*</label>角色名称：</span>
            <input
              type="text"
              :class="
                valid ? 'input' : addIndexDto.roleName ? 'input' : 'input error'
              "
              :maxlength="45"
              v-model="addIndexDto.roleName"
            />
          </div>
        </div>
        <div class="formLine3">
          <div class="formInput">
            <span class="leftSpan"><label>*</label>所属系统：</span>
            <select
              :class="
                valid ? 'input' : addIndexDto.systemFlag ? 'input' : 'input error'
              "
              v-model="addIndexDto.systemFlag"
            >
              <option
                v-for="(sys,idx) in sysList"
                :key="'sys'+idx"
                :value="sys.id"
              >{{sys.value}}</option>
            </select>
          </div>
        </div>
        <div class="formLine3">
          <div class="formInput">
            <span class="leftSpan">角色描述：</span>
            <input
              type="text"
              class="input"
              :maxlength="100"
              v-model="addIndexDto.roleDesc"
            />
          </div>
        </div>
      </div>
      <div
        slot="footer"
        style="text-align:center;"
      >
        <Button
          type="primary"
          size="large"
          @click="addOk()"
        >确定</Button>
        <Button
          type="primary"
          size="large"
          @click="addCancel()"
        >取消</Button>
      </div>
    </Modal>

    <Modal
      v-model="showDialog"
      :mask-closable="false"
      class-name="detailModal"
      scrollable
      draggable
      width="500"
    >
      <p slot="header">
        <span style="color:#fff;">{{ treeTitle }}</span>
      </p>
      <div
        v-if="treeType != 'menu'"
        style="overflow:hidden;"
      >
        <div class="orgEvery">
          <p
            class="mb10"
            style="text-align:left;"
          >
            <Checkbox
              v-model="isAllProps"
              @on-change="
                val => {
                  changAll(val);
                }
              "
            >全系统</Checkbox>
          </p>
          <div class="stationTree">
            <Tree
              v-if="!isAllProps"
              :data="inTreeData"
              :load-data="loadDataIn"
              :render="renderContentIn"
              ref="inTree"
              show-checkbox
              multiple
            ></Tree>
          </div>
        </div>
        <!-- <div class="orgEvery">
          <p class="mb10">
            外勤
          </p>
          <div class="stationTree">
            <Tree
              :data="outTreeData"
              :render="renderContentOut"
              :load-data="loadDataOut"
              ref="outTree"
              show-checkbox
              multiple
            ></Tree>
          </div>
        </div>-->
      </div>
      <div
        v-else
        style="overflow:hidden;"
      >
        <div class="stationTree">
          <Tree
            :data="menuTreeData"
            ref="menutree"
            show-checkbox
            multiple
          ></Tree>
        </div>
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
import Loading from "@/components/Loading";
import MultipleSelect from "@/components/MultipleSelect";
import MutilTree from "@/views/authManage/MutilTree";
import util from "@/utils/util";
export default {
  name: "RoleManage",
  components: {
    MutilTree,
    MultipleSelect,
    Dialog,
    Loading,
  },
  data() {
    return {
      isView: true, //查看权限
      isReset: false, //重置
      isAdd: true, //新增权限
      isConfigpc: true, //配置菜单权限
      isConfigorg: true, //配置机构权限
      isUpdate: true, //修改权限
      isDelete: true, //删除权限
      content: "", //弹框展示内容
      dialogType: "sure", //什么类型的弹框
      loading: true,
      valid: true, //新增角色校验
      roleTitle: "新增角色",
      RmsIndexDto: {
        queryKey: "",
        menuNameList: [""], //菜单按钮
        isDel: "", //当前状态
        comCodes: [],
        pageNum: "",
        pageSize: 10,
      },
      //所属系统list
      sysList: [
        {
          id: "talentQuery",
          value: "一键搜",
        },
        {
          id: "oaCockpit",
          value: "人力数据分析",
        },
        {
          id: "ehrCockpit",
          value: "人力条线分析",
        },
        {
          id: "talent",
          value: "人才池展示",
        },
        {
          id: "ES360",
          value: "360度评估系统",
        },
        {
          id: "smartbi",
          value: "Smartbi",
        },
        {
          id: "board",
          value: "人力数据看板",
        },
        {
          id: "QA360",
          value: "QA360评估系统",
        },
        {
          id: "accfund",
          value: "社保公积金管理平台",
        },
        {
          id: "efficiencyBigShow",
          value: "效能大屏",
        },
        {
          id: "bigShow",
          value: "人力驾驶舱大屏",
        },
      ],
      menuNameList: [], //菜单按钮
      menusList: [], //菜单按钮列表
      addIndexDto: {},
      addDialog: false,
      showDialog: false,
      currentRoleId: "", //当前角色的ID
      menuRes: [], //原始菜单数据Pc端
      menu: [], //处理后的菜单数据Pc端

      total: 0,
      url: "/auMana/authorityHr/role/queryPage", //默认查询
      menuTreeData: [], //弹框树形数据--菜单数据

      treeTitle: "角色配置", //弹框标题
      treeType: "", //弹框类型

      isAllProps: false, //是否全系统
      firstOrg: [], //一级机构信息

      inTreeData: [], //内勤
      outTreeData: [], //外勤

      positionData: [], //角色
      positionCols: [
        {
          title: "序号",
          type: "index",
          width: 70,
          align: "center",
        },
        {
          title: "角色名称",
          key: "roleName",
          minWidth: 150,
          ellipsis: true,
          tooltip: true,
          align: "center",
        },
        {
          title: "所属系统",
          key: "systemFlag",
          minWidth: 100,
          ellipsis: true,
          tooltip: true,
          align: "center",
          render: (h, params) => {
            var sysNam = "";
            if (params.row.systemFlag) {
              var sele = this.sysList.filter((item) => {
                return item.id == params.row.systemFlag;
              });
              sysNam = sele[0].value;
            }
            return h("div", [
              h(
                "a",
                {
                  style: {
                    display: "block",
                    width: "100%",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                    lineHeight: "32px",
                    height: "32px",
                    paddingLeft: "10px",
                    paddingRight: "10px",
                    color:'#333'
                  },
                },
                sysNam
              ),
            ]);
          },
        },
        {
          title: "角色描述",
          key: "roleDesc",
          align: "center",
          minWidth: 110,
          ellipsis: true,
          tooltip: true,
        },
      ],
    };
  },
  created() {
    this.$store.commit("focusId", { focusId: "A0102" });
    //操作权限
    // let btns = localStorage.getItem("butData");
    // if (btns.indexOf("ABR001") > -1) {
    this.isView = true;
    // }
    // if (btns.indexOf("ABR002") > -1) {
    this.isReset = true;
    // }
    // if (btns.indexOf("ABR007") > -1) {
    this.isAdd = true;
    // }
    // if (btns.indexOf("ABR003") > -1) {
    this.isConfigpc = true;
    // }
    // if (btns.indexOf("ABR004") > -1) {
    this.isConfigorg = true;
    // }
    // if (btns.indexOf("ABR005") > -1) {
    this.isUpdate = true;
    // }
    // if (btns.indexOf("ABR006") > -1) {
    this.isDelete = true;
    // }
    let that = this,
      operateMenu = {
        title: "菜单按钮",
        key: "",
        width: 100,
        align: "center",
        ellipsis: true,
        tooltip: true,
        render: (h, params) => {
          return h("div", [
            h(
              "Button",
              {
                props: {
                  type: "primary",
                  size: "small",
                },
                on: {
                  click: () => {
                    this.menuBtnConfig(params);
                  },
                },
              },
              "配置菜单"
            ),
          ]);
        },
      },
      operateOrg = {
        title: "所属机构",
        key: "",
        width: 100,
        align: "center",
        ellipsis: true,
        tooltip: true,
        render: (h, params) => {
          return h("div", [
            h(
              "Button",
              {
                props: {
                  type: "primary",
                  size: "small",
                },
                on: {
                  click: () => {
                    this.orgConfig(params);
                  },
                },
              },
              "配置机构"
            ),
          ]);
        },
      },
      operateDel = {
        title: "操作",
        key: "",
        align: "center",
        width: 150,
        render: (h, params) => {
          let ops = [];
          if (that.isDelete) {
            ops.push(
              h(
                "Button",
                {
                  props: {
                    type: "primary",
                    size: "small",
                  },
                  on: {
                    click: () => {
                      this.delStation(params);
                    },
                  },
                },
                params.row.isDel == "N" ? "禁用" : "启用"
              )
            );
          }
          if (that.isUpdate) {
            ops.push(
              h(
                "Button",
                {
                  props: {
                    type: "primary",
                    size: "small",
                  },
                  style: {
                    "margin-left": "20px",
                  },
                  on: {
                    click: () => {
                      this.updateStation(params);
                    },
                  },
                },
                "修改"
              )
            );
          }
          return h("div", ops);
        },
      };

    if (that.isConfigpc) that.positionCols.push(operateMenu);
    if (that.isConfigorg) that.positionCols.push(operateOrg);
    if (this.isUpdate || this.isDelete) that.positionCols.push(operateDel);
  },
  mounted() {
    let that = this;
    document.onkeydown = function (e) {
      let key = window.event.keyCode;
      if (key === 13) {
        that.query();
      }
    };
    this.query();
    this.initAll(); //查询所有的菜单和按钮
    this.initTable();
  },
  methods: {
    //角色名称下拉选择
    menuChange(values) {
      //console.log(values);
      if (values.selectVal.length == 1 && values.selectVal[0] == "") {
        this.RmsIndexDto.menuNameList = [];
      } else {
        this.RmsIndexDto.menuNameList = values.selectVal;
      }
    },
    //机构信息
    selectOrg(data) {
      console.log(data);
      this.RmsIndexDto.comCodes = data;
    },
    //查询所有菜单和按钮
    initAll() {
      let that = this;
      //清空数据
      that.menu = [];
      this.$refs.loading.show();
      that.axios
        .post("/auMana/authorityHr/menu/queryAll", {})
        .then((res) => {
          that.$refs.loading.hide();
          let data = res.data;
          if (data.status == "success") {
            //所有菜单和按钮
            data.result.map((item) => {
              item.code = item.id;
              item.content = item.menuName;
              item.checked = false;
              item.operateList.map((ele) => {
                ele.title = ele.operateName;
                ele.checked = false;
              });
            });
            that.menuRes = data.result;
            that.menusList = util.arrDeepCopy(data.result);
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          // that.$refs.loading.hide();
          that.sure(error);
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
      this.RmsIndexDto.pageSize = pageSize;
      this.RmsIndexDto.pageNum = "1";
      this.initTable();
    },
    initTable() {
      let that = this;
      that.$refs.loading.show();
      this.axios
        .post(this.url, this.RmsIndexDto)
        .then((res) => {
          let data = res.data;
          that.$refs.loading.hide();
          that.loading = false;
          if (data.status == "success") {
            this.positionData = data.result.list;
            this.total = data.result.total; //总条数
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          that.loading = false;
          // that.$refs.loading.hide();
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
        pageNum: "1",
        comCodes: [],
        pageSize: 10,
      };
      this.$refs.pagenate.currentPage = 1;
      this.$refs.menuCurr.refresh();
    },
    //新增角色
    addStation() {
      //清空数据
      this.addIndexDto = {};
      this.addDialog = true;
      this.roleTitle = "新增角色";
    },
    addOk() {
      let that = this;
      if (this.addIndexDto.roleName&&this.addIndexDto.systemFlag) {
        that.valid = true;
      } else {
        that.valid = false;
      }
      if (!this.valid) {
        that.sure("请补充角色信息！");
        return false;
      }
      let url = "/auMana/authorityHr/role/add";
      if (this.addIndexDto.id) {
        url = "/auMana/authorityHr/role/update";
      }
      that.$refs.loading.show();
      that.axios
        .post(url, that.addIndexDto)
        .then((res) => {
          that.$refs.loading.hide();
          if (res.data.status == "success") {
            that.addDialog = false;
            this.$refs.dialog.show();
            that.dialogType = "sureTo";
            that.content = res.data.message;
            that.initTable();
          } else {
            that.sure(res.data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    addCancel() {
      this.addDialog = false;
    },
    //删除角色
    delStation(params) {
      let that = this;
      that.$Modal.confirm({
        title: "信息",
        content:
          "确定" + (params.row.isDel == "N" ? "禁用" : "启用") + "角色吗？",
        onOk: () => {
          that.$refs.loading.show();
          that.axios
            .post(
              "/auMana/authorityHr/role/updateIsDel/" +
                params.row.id +
                "/" +
                (params.row.isDel == "Y" ? "N" : "Y") +
                "",
              {}
            )
            .then((res) => {
              that.$refs.loading.hide();
              if (res.data.status == "success") {
                that.dialogType = "sureTo";
                that.content = res.data.message;
                that.$refs.dialog.show();
              } else {
                that.sure(res.data.message);
              }
            })
            .catch((error) => {
              that.$refs.loading.hide();
              that.sure(error);
            });
        },
        onCancel: () => {},
      });
    },
    //修改角色
    updateStation(params) {
      let that = this;
      this.roleTitle = "修改角色";
      that.$refs.loading.show();
      this.axios
        .post("/auMana/authorityHr/role/query/" + params.row.id, {})
        .then((res) => {
          that.$refs.loading.hide();
          if (res.data.status == "success") {
            that.addIndexDto = res.data.result;
            that.addDialog = true;
          } else {
            that.sure(res.data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    //配置菜单按钮
    menuBtnConfig(params) {
      let that = this;
      this.$refs.loading.show();
      that.showDialog = true;
      //清空数据
      that.menuTreeData = [];
      that.treeTitle = "菜单配置";
      that.treeType = "menu";
      that.menu = [];
      that.currentRoleId = params.row.id;
      that.axios
        .post("/auMana/authorityHr/role/roleSelectMenu", {
          id: params.row.id,
        })
        .then((res) => {
          that.$refs.loading.hide();
          let data = res.data;
          if (data.status == "success") {
            //处理菜单树
            let menuChecked = res.data.result, //已选中的菜单按钮
              menuData = JSON.parse(JSON.stringify(that.menuRes)); //所有的菜单按钮
            menuChecked.forEach((ele) => {
              menuData.forEach((item) => {
                item.checked = false;
                for (let i = 0; i < item.operateList.length; i++) {
                  item.operateList[i].title = item.operateList[i].operateName;
                  item.operateList[i].id =
                    item.operateList[i].menuId +
                    "" +
                    item.operateList[i].operateId;
                  if (
                    item.operateList[i].operateId == ele.powerOperateId &&
                    item.operateList[i].menuId == ele.powerMenuId
                  ) {
                    item.operateList[i].checked = true;
                  }
                }
              });
            });
            //菜单按钮上下级关系
            menuData.map(function (value) {
              if (!value.parentId) {
                //存在下级菜单处理
                let temp = menuData.filter(function (item) {
                  return item.parentId === value.id;
                });

                let nodes = [];
                temp.map(function (i) {
                  if (i.isLeaf == "N") {
                    i = that.menuNext(i, menuData);
                  } else {
                    i.children = [];
                    i.title = i.menuName;
                  }
                  nodes.push(i);
                });
                value.title = value.menuName;
                value.children = nodes;
                value.children = that.nextOperate(value);
                that.menu.push(value);
              }
            });
            that.menuTreeData = that.menu;
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    menuNext(i, menuData) {
      let that = this,
        tempNext = menuData.filter(function (item) {
          return item.parentId === i.id;
        });
      let nodesNext = [];
      tempNext.map(function (k) {
        if (k.isLeaf == "N") {
          k = that.menuNext(k, menuData);
        } else {
          k.children = [];
          k.title = k.menuName;
        }
        nodesNext.push(k);
      });
      i.title = i.menuName;
      i.children = nodesNext;
      return i;
    },
    nextOperate(node) {
      let that = this,
        operateBtn = [];
      if (node.children.length == 0) {
        node.isLeaf = "Y";
        operateBtn = node.operateList;
      } else {
        node.isLeaf = "N";
        node.children.map((ele) => {
          ele.children = that.nextOperate(ele);
        });
        operateBtn = node.children;
      }
      return operateBtn;
    },
    ok() {
      let that = this;
      that.$refs.loading.show();
      let dialogType = {
        menu() {
          let checkedList = that.$refs.menutree.getCheckedAndIndeterminateNodes();
          let checkedData = checkedList.filter((item) => {
            item.powerMenuId = item.menuId;
            item.powerOperateId = item.operateId;
            return item.operateId;
          });
          return {
            url: "/auMana/authorityHr/role/configMenu",
            postData: {
              id: that.currentRoleId,
              rolePowerMenus: checkedData,
            },
          };
        },
        org() {
          //内勤，外勤选中的数据
          that.getIn();
          // that.getOut();
        },
      };
      let postInfo = dialogType[that.treeType]();
      if (postInfo) {
        that.axios
          .post(postInfo.url, postInfo.postData)
          .then((res) => {
            let data = res.data;
            that.$refs.loading.hide();
            if (data.status == "success") {
              that.showDialog = false;
              that.$refs.dialog.show();
              that.dialogType = "sureTo";
              that.content = data.message;
            } else {
              that.sure(data.message);
            }
          })
          .catch((error) => {
            that.$refs.loading.hide();
            that.sure(error);
          });
      }
    },
    changeStructure(item, comNames) {
      let that = this;
      if (item.children && item.children.length > 0) {
        var nexts = item.children.filter((ele) => {
          return ele.checked||ele.indeterminate;
        });
        if (nexts.length != item.children.length) {
          nexts.forEach((ele) => {
            that.$set(ele, "title", item.title + ">" + ele.title);
            comNames = comNames.concat(that.changeStructure(ele, []));
          });
        } else {
          comNames.push(item.title);
        }
      } else {
        comNames.push(item.title);
      }
      return comNames;
    },
    //全系统
    changAll(val) {
      this.isAllProps = val;
      if (!this.isAllProps) {
        //非全系统
        this.inTreeData.map((item) => {
          item.checked = false;
        });
      }
    },
    //获取内勤选中的数据
    getIn() {
      let that = this,
        comCodes = [],
        comPathList = [];
      if (!that.isAllProps) {
        let checkedList = that.$refs.inTree.getCheckedAndIndeterminateNodes();
        let dataProjectList = checkedList.filter((ch) => {
          return ch.checked == true;
        });
        comCodes = util.getKeyList(dataProjectList, "id");
        console.log(checkedList);
        var selectData = checkedList.filter((item) => {
          var codes = item.parentId.split("#").filter((item) => item != "");
          return codes.length == 1;
        });
        var checkData = util.arrDeepCopy(selectData);
        checkData.forEach((item) => {
          comPathList = comPathList.concat(that.changeStructure(item, []));
        });
      } else {
        comCodes = util.getKeyList(that.firstOrg, "id");
        comPathList = ["全系统"];
      }
      //return false;
      let data = {
        id: that.currentRoleId,
        flag: "ehr",
        comCodes: comCodes,
        comPathList: comPathList,
      };
      this.commitOrg(data);
    },
    //获取外勤选中的数据
    getOut() {
      let that = this,
        comCodes = [];
      if (!that.isAllPropsNo) {
        let checkedList = that.$refs.outTree.getCheckedAndIndeterminateNodes();
        let dataProjectList = checkedList.filter((ch) => {
          return ch.checked == true;
        });
        comCodes = util.getKeyList(dataProjectList, "id");
      }
      let data = {
        id: that.currentRoleId,
        flag: "shr",
        comCodes: comCodes,
      };
      this.commitOrg(data);
    },
    //角色配置机构提交
    commitOrg(data) {
      let that = this;
      that.$refs.loading.show();
      that.axios
        .post("/auMana/authorityHr/role/configCom", data)
        .then((res) => {
          let data = res.data;
          that.$refs.loading.hide();
          if (data.status == "success") {
            that.showDialog = false;
            that.$refs.dialog.show();
            that.dialogType = "sureTo";
            that.content = data.message;
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },
    cancel() {
      this.showDialog = false;
    },
    //配置机构
    orgConfig(params) {
      this.showDialog = true;
      this.treeTitle = "配置机构";
      this.treeType = "org";
      this.currentRoleId = params.row.id;
      //处理内勤的赋值处理
      this.initInAll(params);
      //处理外勤的赋值处理
      //this.initOutAll(params);
    },
    //内勤
    initInAll(params) {
      let that = this;
      this.$refs.loading.show();
      that.inTreeData = [];
      that.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "ehr",
          parentId: "",
          roleId: params.row.id, //角色id
        })
        .then((res) => {
          that.$refs.loading.hide();
          let data = res.data;
          if (data.status == "success") {
            data.result.forEach((item, i) => {
              item.title = item.deptName;
              //判断是否有子节点
              if (item.isLeaf == "N") {
                item.loading = false;
                item.children = [];
              }
              item.indeterminate = item.checkStatus == "Y" ? true : false; //选中
              item.checked = item.checkStatus == "A" ? true : false; //全选
            });
            //全系统是否选中
            var checks = data.result.filter((item) => item.checked);
            that.firstOrg = util.arrDeepCopy(data.result); //一级机构信息
            that.isAllProps = checks.length == that.firstOrg.length;
            //机构树信息
            that.inTreeData = data.result;
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },

    //外勤
    initOutAll(params) {
      let that = this;
      this.$refs.loading.show();
      that.outTreeData = [];
      that.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "shr",
          parentId: "",
          roleId: params.row.id, //角色id
        })
        .then((res) => {
          that.$refs.loading.hide();
          let data = res.data;
          if (data.status == "success") {
            data.result.forEach((item, i) => {
              item.title = item.deptName;
              //判断是否有子节点
              if (item.isLeaf == "N") {
                item.loading = false;
                item.children = [];
              }
              item.indeterminate = item.checkStatus == "Y" ? true : false; //选中
              item.checked = item.checkStatus == "A" ? true : false; //全选
            });
            that.outTreeData = data.result;
          } else {
            that.sure(data.message);
          }
        })
        .catch((error) => {
          that.$refs.loading.hide();
          that.sure(error);
        });
    },

    //点击名称的时候展示、收缩节点
    selectChangeTreeIn(node) {
      if (!node.expand) {
        this.loadDataIn(node, (response) => {
          if (!response) return; // 没有子节点则返回
          let array = [];
          response.forEach((item) => {
            // 遍历子节点，然后在各子节点上递归调用请求下一层后代
            array.push(item);
          });
          node.children = array; // 挂载子节点
          //node.expand = true; // 展开子节点树
          this.$set(node, "expand", true);
        });
      } else {
        node.children = [];
        //node.expand = false; // 展开子节点树
        this.$set(node, "expand", false);
      }
    },
    //选中机构，请求对应数据
    selectChangeIn(data) {
      this.selectChangeTreeIn(data);
    },
    //添加icon
    renderContentIn(h, { root, node, data }) {
      return h(
        "span",
        {
          style: {
            display: "inline-block",
            cursor: "pointer",
          },
          //点击事件取到节点数据，触发事件
          on: {
            click: () => {
              if (data.id) this.selectChangeIn(data);
            },
          },
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "md-people",
                color: "#2d8cf0",
                size: "16",
              },
              style: {
                marginRight: "8px",
              },
            }),
            h("span", data.title),
          ]),
        ]
      );
    },
    //异步加载子节点
    loadDataIn(item, callback) {
      // console.log(item);
      let that = this;
      this.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "ehr",
          roleId: that.currentRoleId, //角色id
          parentId: item.id,
        })
        .then(function (res) {
          if (res.data.status == "success") {
            res.data.result.forEach((ele, i) => {
              ele.title = ele.deptName;
              //判断是否有子节点
              if (ele.isLeaf == "N") {
                ele.loading = false;
                ele.children = [];
              }
              ele.indeterminate = ele.checkStatus == "Y" ? true : false; //选中
              ele.checked = ele.checkStatus == "A" ? true : false; //全选
            });
            callback(res.data.result);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    //点击名称的时候展示、收缩节点
    selectChangeTreeOut(node) {
      if (!node.expand) {
        this.loadDataIn(node, (response) => {
          if (!response) return; // 没有子节点则返回
          let array = [];
          response.forEach((item) => {
            // 遍历子节点，然后在各子节点上递归调用请求下一层后代
            array.push(item);
          });
          node.children = array; // 挂载子节点
          //node.expand = true; // 展开子节点树
          this.$set(node, "expand", true);
        });
      } else {
        node.children = [];
        //node.expand = false; // 展开子节点树
        this.$set(node, "expand", false);
      }
    },
    //选中机构，请求对应数据
    selectChangeOut(data) {
      this.selectChangeTreeOut(data);
    },
    //添加icon
    renderContentOut(h, { root, node, data }) {
      return h(
        "span",
        {
          style: {
            display: "inline-block",
            cursor: "pointer",
          },
          //点击事件取到节点数据，触发事件
          on: {
            click: () => {
              if (data.id) this.selectChangeOut(data);
            },
          },
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "md-people",
                color: "#2d8cf0",
                size: "16",
              },
              style: {
                marginRight: "8px",
              },
            }),
            h("span", data.title),
          ]),
        ]
      );
    },
    //异步加载子节点---外勤
    loadDataOut(item, callback) {
      let that = this;
      this.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "shr",
          roleId: that.currentRoleId, //角色id
          parentId: params.row.id,
        })
        .then(function (res) {
          if (res.data.status == "success") {
            res.data.result.forEach((item, i) => {
              item.title = item.deptName;
              //判断是否有子节点
              if (item.isLeaf == "N") {
                item.loading = false;
                item.children = [];
              }
              item.indeterminate = item.checkStatus == "Y" ? true : false; //选中
              item.checked = item.checkStatus == "A" ? true : false; //全选
            });
            callback(res.data.result);
          }
        });
      // .catch(function(error) {
      //   console.log(error);
      // });
    },
    dialogOk() {
      this.reset();
      this.initTable();
    },
    sure(content) {
      //this.currentRoleId = "";
      this.dialogType = "sure";
      this.content = content;
      if (this.$refs.dialog) {
        this.$refs.dialog.show();
      }
    },
  },
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.content {
  margin-left: 16px;
}
.stationTree {
  height: 250px;
  overflow: auto;
}
.orgEvery {
  width: 100%;
  float: left;
  margin-right: 20px;
  padding: 5px 0px 5px 5px;
  border-radius: 5px;
}
.mb10 {
  text-align: center;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}
.formLine3 {
  margin: 10px 0px;
  width: 100%;
  .formInput {
    height: 32px;
    .leftSpan {
      width: 115px;
      display: inline-block;
      float: left;
      height: 32px;
      line-height: 32px;
      padding-right: 10px;
      text-align: right;
      label{
        color:#ff0000;
      }
    }
    .input {
      width: 65%;
      float: left;
      height: 32px;
      border: 1px solid #dcdee2;
      border-radius: 5px;
      outline: none;
      padding-left: 10px;
      &.error {
        border: 1px solid red;
      }
    }
    .content {
      width: 80%;
      float: right;
      background: salmon;
    }
  }
}
/deep/.ivu-table-header thead tr th {
  padding: 8px 0px;
}
</style>
