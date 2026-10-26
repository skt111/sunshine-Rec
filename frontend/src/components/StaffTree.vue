<template>
  <div class="staffTree">
    <div
      class="inputBox"
      @click="disabled ? model = false : model = true"
    >
      <Input
        type="text"
        v-if="onlyOrg"
        v-model="orgNames"
        suffix="ios-people"
        @click.native="searchRoot"
        style="width:100%"
        disabled
      />
      <Input
        type="text"
        v-else
        v-model="userNameShow"
        suffix="ios-people"
        disabled
        @click.native="searchRoot"
      />
    </div>
    <Modal
      v-model="model"
      class-name="choiceModal hasFoot"
      width="800"
      :styles="{top: '35px'}"
    >
      <p
        slot="header"
        class="org_header"
      >
        <Icon
          type="ios-people"
          size="40"
          color="#1890ff"
        ></Icon>
        <span v-if="onlyOrg">机构选择</span>
        <span v-else>人员选择</span>
      </p>
      <div>
        <div class="org_path">
          <span>{{orgPath}}</span>
        </div>
        <div
          class="search_input"
          v-if='!onlyOrg'
        >
          <input
            type="text"
            v-model="searchStr"
            placeholder="请输入关键字"
            v-on:keypress='searchUsers'
          />
          <Icon
            type="ios-search"
            size="24"
            @click="searchStaff()"
          />
        </div>
      </div>
      <div style="height:316px;">
        <div
          class="department"
          :class="onlyOrg?'maxWidth':''"
        >
          <Tree
            :data="department"
            :render="renderContent"
            :load-data="loadData"
          ></Tree>
        </div>
        <div
          class="staff"
          v-if='!onlyOrg'
        >
          <div
            v-if="personNum=='much'"
            class="select_all"
          >
            <Checkbox
              :indeterminate="indeterminate"
              :value="checkAll"
              @click.prevent.native="handleCheckAll"
            >全选</Checkbox>
          </div>
          <div v-if="showPerson">
            <template v-for="(vv, kk) in staff">
              <p
                v-if="personNum=='much'"
                :key="'mmm'+kk"
              >
                <label><input
                    type="checkbox"
                    :value="vv.checked"
                    @change="selectPerson(vv,kk)"
                    :checked="vv.checked"
                  >
                  {{vv.lastName}}({{vv.oprid}})</label>
              </p>
              <p
                v-if="personNum=='single'"
                :key="'sss'+kk"
              >
                <label><input
                    type="radio"
                    name="onePerson"
                    @change="selectPerson(vv,kk)"
                    :checked="vv.checked"
                  >
                  {{vv.lastName}}({{vv.oprid}})</label>
              </p>
              <div
                class="person_from"
                :key="'pf'+kk"
              >{{vv.sigBuDescr}}_{{vv.sigDeptDescr}}</div>
            </template>
          </div>
          <div v-if='noneUserList'>
            <img src="../assets/img/common/no_data.png" />
          </div>
        </div>

        <div class="selectNode">
          <div class="scroll_div">
            <div
              v-for="(v, i) in selectArr"
              @mouseover="hoverStyle(i)"
              @mouseout="outStyle(i)"
              :key="'ses'+i"
            >
              <span :title="v.title||v.lastName||v.memberName">{{v.minName}}</span>
              <span
                v-show="hoverDelete[i]"
                class="delete_btn"
              >
                <img
                  src="../assets/img/common/deleteBtn.png"
                  @click="deleteItemPerson(v,i)"
                />
              </span>
            </div>
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
          @click="ok()"
        >确定</Button>
        <Button
          type="primary"
          size="large"
          @click="canel()"
        >取消</Button>
      </div>
    </Modal>
    <Loading ref="loadings"></Loading>
    <Dialog
      ref="dialog"
      :content="content"
    ></Dialog>
  </div>
</template>
<script>
/*-----------
人员和机树staffTree的使用说明：
1.多个机构选择：
<StaffTree @selectedOrg="点击确定后的回调函数" :orgLists='已选的机构列表' :onlyOrg='onlyOrg'></StaffTree>
2.多人选择：
<StaffTree personNum='much' @onSelect="点击确定后的回调函数" :groupTypeCode='orgData.groupTypeCode' :groupNum='orgData.groupNum' :groupTypeName='orgData.groupTypeName' ></StaffTree>
3.单人选择：
<StaffTree personNum='single'  @onSelect="点击确定后的回调函数" :diffOrg='某个人员对象' :changeStaff='changeStaff' :index='index' :isGroupLeader='org.isGroupLeader' :groupTypeCode='org.groupTypeCode' :groupNum='org.groupNum' :groupTypeName='org.groupTypeName'></StaffTree>
------------*/
import Loading from "@/components/Loading";
import Dialog from "@/components/Dialog";
export default {
  name: "StaffTree",
  components: {
    Loading,
    Dialog,
  },
  props: {
    //控制弹窗是否可以选择  默认为false可弹  true为不可弹窗
    disabled: {
      default: false,
    },
    //是否仅选择机构
    onlyOrg: {
      default: false,
    },
    //多人还是单人
    personNum: {
      default: "much",
    },
    //组id
    groupTypeCode: {
      default: "",
    },
    //组名称
    groupTypeName: {
      default: "",
    },
    //组号
    groupNum: {
      default: "",
    },
    //是否是组长
    isGroupLeader: {
      default: "",
    },
    //编号
    index: {
      default: "",
    },
    // 某个人员对象
    diffOrg: {
      type: Object,
      default: function () {
        return {};
      },
    },
    //选择人员的时候触发orgList变化，orgList又会反作用于staff中的diffOrg变化
    changeStaff: {
      default: false,
    },
    //多个机构列表
    orgLists: {
      default: function () {
        return [];
      },
    },
    //多个人员的列表
    persons: {
      default: function () {
        return [];
      },
    },
    aboutMan: {
      default: "0",
    },
  },
  data() {
    return {
      model: false,
      department: [], //机构数据
      staff: [], //人员数据
      selectNode: "", //选中机构或人员数据
      indeterminate: true, //设置 indeterminate 状态，只负责样式控制
      checkAll: false, //全选状态的切换
      selectArr: [], //下方选中的集合
      showPerson: false, //是否显示人员，用v-if触发人员列表重新渲染
      hoverDelete: [], //决定删除哪个
      searchStr: "", //人员模糊查询输入的字符串
      userNameShow: "", //显示在input框里的文字（选人员）
      orgNames: "", //显示在input框里的文字（选机构）
      noneUserList: false, //没有人员的时候要显示图片
      orgPath: "", //当前机构路径
      staffItem: {}, //处理数据时用到的一个中间变量
      nodesMap: {}, //一个map，用来记录根据人员查出的机构树哪些节点是展开的
      content: "", //弹框展示的内容
    };
  },
  watch: {
    diffOrg: {
      handler() {
        this.staffItem = JSON.parse(JSON.stringify(this.diffOrg));
        this.staffItem.minName = this.staffItem.memberName;
        this.staffItem.lastName = this.staffItem.memberName;
        this.userNameShow = this.staffItem.lastName;
        if (this.staffItem.minName && this.changeStaff) {
          this.selectArr.push(this.staffItem);
        }
      },
      deep: true,
      immediate: true,
    },
    orgLists: {
      handler() {
        if (this.orgLists.length > 0) {
          this.selectArr = this.orgLists;
          if (this.orgLists.length == 1) {
            //机构的话显示的字段是title，人员显示的字段是lastName
            this.orgNames = this.orgLists[0].title;
          } else {
            this.orgNames = this.orgLists[0].title + "等";
          }
          this.orgLists.forEach(function (v, i) {
            v.minName =
              v.title && v.title.length > 6
                ? v.title.slice(0, 6) + "..."
                : v.title;
          });
        }
      },
      deep: true,
      immediate: true,
    },
    persons: {
      handler: function (val, oldVal) {
        if (this.persons.length > 0) {
          this.persons.forEach(function (v, i) {
            v.minName = v.lastName;
          });
          this.selectArr = this.persons;
          if (this.persons.length == 1) {
            //机构的话显示的字段是title，人员显示的字段是lastName
            this.userNameShow = this.persons[0].lastName;
          } else {
            this.userNameShow = this.persons[0].lastName + "等";
          }
        }
      },
      deep: true,
      immediate: true,
    },
  },
  mounted() {},
  methods: {
    clearSelectArr() {
      this.selectArr = [];
      this.orgNames = "";
      this.userNameShow = "";
    },
    //人员模糊查询-回车查询
    searchUsers(e) {
      var keyCode = null;
      if (e.which) keyCode = e.which;
      else if (e.keyCode) keyCode = e.keyCode;

      if (keyCode == 13) {
        this.searchStaff();
        return false;
      }
      return true;
    },
    //人员模糊查询-按钮查询
    searchStaff() {
      var that = this;
      this.checkAll = false;
      this.showPerson = false;
      if (this.searchStr == "") {
        this.sure("请输入关键字再进行搜索！");
        return false;
      }
      this.axios
        .get("/authority/deptManage/queryDeptUserByDeptId/" + this.searchStr)
        .then(function (res) {
          that.showPerson = true;
          res.data.result.forEach((item, i) => {
            item.checked = false;
            item.minName = item.lastName;
          });
          for (let i = 0; i < res.data.result.length; i++) {
            that.selectArr.forEach((value, index) => {
              if (res.data.result[i].oprid == value.oprid) {
                that.$set(res.data.result[i], "checked", true);
              }
            });
          }
          that.staff = res.data.result;
          if (that.staff.length == 0) {
            that.noneUserList = true;
          } else {
            that.noneUserList = false;
          }
        })
        .catch(function (error) {});
    },
    //全选框选中或取消的切换
    handleCheckAll() {
      if (this.indeterminate) {
        this.checkAll = false;
      } else {
        this.checkAll = !this.checkAll;
      }
      this.indeterminate = false;

      if (this.checkAll) {
        this.staff.forEach((value, index) => {
          value.checked = true;
        });
        for (var i = 0; i < this.staff.length; i++) {
          this.selectArr.push(this.staff[i]);
        }
      } else {
        this.selectArr = this.selectArr.slice(
          0,
          this.selectArr.length - this.staff.length
        );
        this.staff.forEach((value, index) => {
          value.checked = false;
        });
      }
    },
    //移入时删除图标显示
    hoverStyle(index) {
      this.$set(this.hoverDelete, index, true);
    },
    //移出时删除图标消失
    outStyle(index) {
      this.$set(this.hoverDelete, index, false);
    },
    //点击删除小图标
    deleteItemPerson(v, index) {
      this.selectArr.splice(index, 1);
      this.staff.forEach((value, index) => {
        //判断是否有子节点
        if (v.oprid == value.oprid) {
          this.$set(value, "checked", false);
        }
      });
    },
    //确定
    ok() {
      //触发回调，把选择数据返给父组件
      if (this.selectArr && this.selectArr.length == 0) {
        this.sure("请至少选择一项");
        return false;
      }
      if (this.onlyOrg) {
        this.$emit("selectedOrg", this.selectArr);
      } else {
        this.$emit("onSelect", {
          selectArr: this.selectArr,
          personNum: this.personNum,
          groupTypeCode: this.groupTypeCode,
          groupNum: this.groupNum,
          groupTypeName: this.groupTypeName,
          isGroupLeader: this.isGroupLeader,
          index: this.index,
        });
      }
      if (this.personNum == "single") {
        this.userNameShow = this.selectArr[0].lastName;
      } else {
        if (this.selectArr.length == 1) {
          //机构的话显示的字段是title，人员显示的字段是lastName
          this.userNameShow = this.selectArr[0].lastName;
        } else {
          this.userNameShow = this.selectArr[0].lastName + "等";
        }
      }
      this.model = false;
    },
    //取消
    canel() {
      this.model = false;
    },
    //根据机构查人员
    selectChange(data) {
      let that = this;
      if (data.path) {
        this.orgPath = data.path;
      }
      this.checkAll = false;
      //判断选择人员还是机构
      if (!this.onlyOrg) {
        this.showPerson = false;
        this.axios
          .post("/authority/deptManage/queryDeptUserByDeptId", {
            deptId: data.deptId,
            businessId: data.setId,
          })
          //.get("/authority/deptManage/queryDeptUserByDeptId/"+data.deptId+"/"+data.setId)
          .then(function (res) {
            that.showPerson = true;
            if (res.data.result.length == 0) {
              that.noneUserList = true;
            } else {
              that.noneUserList = false;
            }
            res.data.result.forEach((item, i) => {
              item.checked = false;
              item.minName = item.lastName;
            });
            for (let i = 0; i < res.data.result.length; i++) {
              that.selectArr.forEach((value, index) => {
                var id = value.memberId || value.emplid;
                if (res.data.result[i].emplid == id) {
                  that.$set(res.data.result[i], "checked", true);
                }
              });
            }
            that.staff = res.data.result;
          })
          .catch(function (error) {});
      } else {
        var pushFlag = true;
        for (var i = 0; i < this.selectArr.length; i++) {
          if (
            data.title ==
            (this.onlyOrg
              ? this.selectArr[i].title
              : this.selectArr[i].lastName)
          ) {
            pushFlag = false;
          }
        }
        if (pushFlag || this.selectArr.length == 0) {
          this.selectArr.push(data);
        }
        if (this.selectArr.length == 1) {
          this.orgNames = this.selectArr[0].title;
        } else {
          this.orgNames = this.selectArr[0].title + "等";
        }
      }
      that.selectChangeTree(data);
    },
    selectChangeTree(node) {
      var that = this;
      if (!node.expand) {
        if (node.children.length > 0) {
          that.$set(node, "expand", true);
        } else {
          this.loadData(node, (response) => {
            if (!response || response.length == 0) return; // 没有子节点则返回
            let array = [];
            response.forEach((item) => {
              // 遍历子节点，然后在各子节点上递归调用请求下一层后代
              array.push(item);
            });
            node.children = array; // 挂载子节点
            that.$set(node, "expand", true);
            //node.expand = true; // 展开子节点树
          });
        }
      } else {
        //node.children =[];
        that.$set(node, "expand", false);
        //node.expand = false; // 展开子节点树
      }
    },

    //选中人员事件
    selectPerson(item, i) {
      if (this.personNum == "single") {
        this.selectArr = [];
        if (!this.staff[i].checked) {
          for (var j = 0; j < this.staff.length; j++) {
            this.staff[j].checked = false;
          }
          this.staff[i].checked = !this.staff[i].checked;
        }
      } else {
        this.staff[i].checked = !this.staff[i].checked;
      }
      if (this.staff[i].checked) {
        this.selectArr.push(item);
      } else {
        this.selectArr.forEach((value, index) => {
          //判断是否有子节点
          if (item.oprid == value.oprid) {
            this.selectArr.splice(index, 1);
          }
        });
      }
    },
    //添加icon
    renderContent(h, { root, node, data }) {
      return h(
        "span",
        {
          style: {
            display: "inline-block",
          },
          //点击事件取到节点数据，触发事件
          on: {
            click: () => {
              this.selectChange(data);
            },
          },
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "md-people",
                color: "#f4781f",
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
    loadData(value, callback) {
      let that = this;
      this.axios
        .post("/authority/deptManage/querySubDeptTreeById", {
          deptId: value.deptId,
          businessId: value.setId,
        })
        //.get("/authority/deptManage/querySubDeptTreeById/"+value.deptId+"/"+value.setId)
        .then(function (res) {
          if (res.data.status == "success") {
            res.data.result.forEach((item, i) => {
              item.loading = false;
              item.children = [];
              item.minName =
                item.title && item.title.length > 6
                  ? item.title.slice(0, 6) + "..."
                  : item.title;
              item.path = value.path + ">" + item.title;
            });
            callback(res.data.result);
          }
        })
        .catch(function (error) {});
    },
    //修改回写时的参数处理
    setProps(data) {
      var that = this;
      data.forEach((item, i) => {
        item.loading = false;
        item.minName =
          item.title && item.title.length > 6
            ? item.title.slice(0, 6) + "..."
            : item.title;
        item.path = item.title;
        if (that.nodesMap[item.deptId] == "true") {
          item.expand = true;
        }
        if (item.children) {
          this.setProps(item.children);
        } else {
          item.children = [];
        }
      });
      return data;
    },
    //机构树的初始化查询
    searchRoot() {
      var that = this,
        data,
        indexMark;
      //多人且传入的persons为空，代表是新增；修改的时候不清空
      if (this.personNum == "much" && this.aboutMan == 0 && !this.onlyOrg) {
        this.selectArr = [];
        this.staff = [];
      }
      //证明有人，去查所在机构
      if (
        this.staffItem.minName != "" &&
        this.staffItem.minName != undefined &&
        this.personNum == "single"
      ) {
        data = {
          emplid: this.staffItem.memberId,
        };
      } else {
        data = {
          emplid: localStorage.getItem("loginId")
            ? JSON.parse(localStorage.getItem("loginId")).loginId
            : "",
        };
      }
      return false;
      this.axios
        .post("/authority/deptManage/queryDeptTreeByOprid", data)
        .then(function (res) {
          var str = "";
          for (let i = 0; i < res.data.result.deptPathList.length; i++) {
            if (i == 0) {
              str += res.data.result.deptPathList[0].title;
            } else {
              str += ">" + res.data.result.deptPathList[i].title;
            }
            that.orgPath = str;
          }
          that.nodesMap = res.data.result.deptMap;
          res.data.result.deptTreeModelList.forEach(function (v, i) {
            if (v.setId == res.data.result.setId) {
              indexMark = i;
              res.data.result.deptTreeModelList[indexMark] = that.setProps([
                res.data.result.deptTreeModelList[i],
              ])[0];
            } else {
              v.children = [];
              v.loading = false;
            }
          });

          that.department = res.data.result.deptTreeModelList;
          if (!that.onlyOrg) {
            that.selectChange({
              deptId: res.data.result.deptId,
              setId: res.data.result.setId,
            });
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    sure(content) {
      this.dialogType = "sure";
      this.content = content;
      this.$refs.dialog.show();
    },
  },
};
</script>
<style lang="scss" scoped>
@mixin staffTree {
  height: 250px;
  width: 50%;
  border: 1px solid #eaeef1 !important;
  overflow-y: auto;
  margin-bottom: 8px;
}

.department {
  @include staffTree;
  float: left;
  padding-left: 8px;
}
/deep/ .ivu-input[disabled],
fieldset[disabled] .ivu-input {
  cursor: pointer;
}

.staff {
  @include staffTree;
  float: right;
  li {
    padding: 5px;
  }
  li:nth-child(odd) {
    background: #fafafa;
  }
  li:hover {
    background: #dbecfd;
    padding: 8px;
    .staffName {
      color: #f28133;
    }
  }
  .staffDep {
    color: #aaaaaa;
  }
  .select_all {
    border-bottom: 1px solid #e9e9e9;
    padding-bottom: 3px;
    margin-left: 5px;
  }
  input {
    margin: 0 5px;
  }
  p {
    color: #000;
  }
  img {
    margin: 25px auto;
    display: block;
  }
}
.person_from {
  margin-left: 23px;
  color: #9f9f9f;
  margin-bottom: 1px;
}
.org_header {
  height: 30px;
  line-height: 30px;
  font-size: 16px;
  span {
    position: absolute;
    line-height: 40px;
    margin-left: 10px;
    color: #333;
  }
}
.staffTree {
  .inputBox {
    .ivu-input-wrapper {
      width: 50%;
    }
  }
}
.org_path {
  width: 75%;
  float: left;
  height: 30px;
  margin-bottom: 5px;
}
.search_input {
  width: 25%;
  float: left;
  margin-bottom: 5px;
  height: 30px;
  text-align: right;
  position: relative;
  input {
    width: 97%;
    margin-left: 5px;
    height: 32px;
    border-radius: 3px;
    border: 1px solid #ddd;
    padding-left: 5px;
  }
  .ivu-icon {
    position: absolute;
    top: 4px;
    right: 0px;
  }
}
.selectNode {
  clear: both;
  width: 100%;
  height: 50px;
  line-height: 50px;
  padding-left: 10px;
  color: #515a6e;
  border: 1px solid #eaeef1 !important;
  .scroll_div {
    width: auto;
    overflow-x: scroll;
    height: 138%;
    white-space: nowrap;
    div {
      border-radius: 26%;
      width: 120px;
      display: inline-block;
    }
  }
  .delete_btn {
    position: relative;
    left: 0px;
    top: 0px;
  }
}
.maxWidth {
  width: 100% !important;
}
/deep/ .ivu-icon-md-people {
  color: #6499f7 !important;
}
/deep/ .ivu-tree-children {
  span {
    cursor: pointer;
  }
}
/deep/ .ivu-modal-footer {
  padding-top: 40px;
  border-top: 0px;
}
/deep/ .ivu-modal-body {
  height: 380px;
}
</style>