<template>
  <div class="staffTree">
    <div
      class="inputBox"
      @click="disabled ? model = false : model = true"
    >
      <Input
        type="text"
        v-model="departmentName"
        suffix="ios-people"
        readonly
      />
      <Input
        type="text"
        v-model="staffName"
        v-show="showStaff"
        suffix="ios-people"
        readonly
      />
    </div>
    <Modal
      v-model="model"
      class-name="staffModal"
      width="800"
    >
      <p
        slot="header"
        style="height:30px;line-height:30px;font-size:16px;"
      >
        <Icon
          type="ios-people"
          size="40"
          color="#f4781f"
        ></Icon>
        <span class="treeModal">机构人员树</span>
      </p>
      <div style="height:300px;">
        <div class="department">
          <Tree
            :data="department"
            :render="renderContent"
            :load-data="loadData"
          ></Tree>
        </div>
        <div class="staff">
          <ul v-if="staff">
            <li
              v-for="(item,index) in staff"
              @click="selectPerson(item,staff)"
              :key="'staff'+index"
            >
              <span class="staffName">{{item.LAST_NAME}}</span>
              <br>
              <span class="staffDep">{{item.DESCR3}}</span>
            </li>
          </ul>
          <ul v-else>
            <li>暂无人员数据</li>
          </ul>
        </div>
        <div class="selectNode">{{selectNode.LAST_NAME}}</div>
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
  </div>
</template>
<script>
/*-----------
人员和机树staffTree的使用说明：
<StaffTree :disabled="disabled" :showStaff="showStaff" @onSelect="selectMessage" BUSINESS_UNIT="机构主体" DESCRSHORT="机构" userName="人员"></StaffTree>
disabled控制弹窗是否可以选择  默认为false可弹  true为不可弹窗 
showStaff控制是否显示人员选择  默认为false不显示  true显示  
onSelect为回调事件，返回人员和机构信息
onSelect回调信息：{
    BUSINESS_UNIT: "机构标识"
    DESCRSHORT: "机构名称"
    userCode: "人员标识"
    userName: "人员名称"
}
orgCode机构主体传参
orgName机构名称赋值
userName人员名称赋值
------------*/
export default {
  name: "StructureTree",
  props: {
    disabled: {
      default: false
    },
    showStaff: {
      default: false
    },
    //机构主体
    BUSINESS_UNIT: {
      default: "",
      type: String
    },
    //机构名称
    DESCRSHORT: {
      type: String
    },
    //人员名称
    userName: {
      type: String
    }
  },
  data() {
    return {
      model: false,
      departmentName: this.DESCRSHORT, //机构名称
      staffName: this.userName, //人员名称
      department: [], //机构数据
      staff: [], //人员数据
      selectNode: "" //选中机构或人员数据
    };
  },
  watch: {
    BUSINESS_UNIT(newCode, oldCode) {
      this.initTree();
    },
    DESCRSHORT(newName, oldName) {
      this.departmentName = newName;
    },
    userName(newName, oldName) {
      this.staffName = newName;
    },
    showStaff(newVal, oldVal) {
      //清空人员列表
      this.staff = [];
      this.staffName = "";
      this.departmentName = "";
      this.selectNode = "";
      //触发回调，把选择数据返给父组件
      this.$emit("onSelect", this.selectNode);
    }
  },
  methods: {
    clearCon() {
      this.departmentName = "";
    },
    //确定
    ok() {
      if (this.selectNode) {
        //触发回调，把选择数据返给父组件
        this.$emit("onSelect", this.selectNode);
      }
      this.model = false;
    },
    //取消
    canel() {
      this.model = false;
    },
    initTree() {
      let that = this;
      //请求一级机构数据
      this.axios
        .post("/inductAuto/eprotocol/common/architecture_query")
        .then(function(res) {
          if (res.data.status == "success") {
            res.data.result.forEach((item, i) => {
              item.title = item.DESCRSHORT;
              //判断是否有子节点
              if (!item.OPEN) {
                item.loading = false;
                item.children = [];
              }
            });
            that.department = res.data.result;
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    },
    //点击名称的时候展示、收缩节点
    selectChangeTree(node) {
      if (!node.expand) {
        this.loadData(node, response => {
          if (!response) return; // 没有子节点则返回
          let array = [];
          response.forEach(item => {
            // 遍历子节点，然后在各子节点上递归调用请求下一层后代
            array.push(item);
          });
          node.children = array; // 挂载子节点
          //node.expand = true; // 展开子节点树
          this.$set(node,'expand',true); 
        });
      } else {
        node.children =[];
        //node.expand = false; // 展开子节点树
        this.$set(node,'expand',false); 
      }
    },
    //选中机构，请求对应数据
    selectChange(data) {
      let that = this;
      //判断选择人员还是机构
      if (this.showStaff) {
        this.axios
          .post("/inductAuto/eprotocol/common/query_empl_by_dept", {
					"deptId": data.TREE_NODE+'',
					"startNum":data.TREE_NODE_NUM+'',
					"endNum":data.TREE_NODE_NUM_END+'',
					"bu":data.SETID,
					"searchContent":""
					})
          .then(function(res) {
            that.staff = res.data.result;
          })
          .catch(function(error) {
            console.log(error);
          });
      } else {
        //赋值选中机构数据
        this.selectNode = data;
        this.selectChangeTree(data);
      }
    },
    //选中人员事件
    selectPerson(item, org) {
      //赋值选中人员字段
      this.selectNode = item;
    },
    //添加icon
    renderContent(h, { root, node, data }) {
      return h(
        "span",
        {
          style: {
            display: "inline-block",
            cursor:"pointer"
          },
          //点击事件取到节点数据，触发事件
          on: {
            click: () => {
              if (data.SETID) this.selectChange(data);
            }
          }
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "md-people",
                color: "#f4781f",
                size: "16"
              },
              style: {
                marginRight: "8px"
              }
            }),
            h("span", data.title)
          ])
        ]
      );
    },
    //异步加载子节点
    loadData(item, callback) {
      let that = this;
      this.axios
        .post("/inductAuto/eprotocol/common/query_children_dept",{
					"bu":item.SETID,
					"num":item.TREE_NODE_NUM+''
					})
        .then(function(res) {
          if (res.data.status == "success") {
            res.data.result.forEach((item, i) => {
              item.title = item.DESCRSHORT;
              //判断是否有子节点
              if (!item.OPEN) {
                item.loading = false;
                item.children = [];
              }
            });
            callback(res.data.result);
          }
        })
        .catch(function(error) {
          console.log(error);
        });
    }
  },
  mounted() {
    this.initTree();
  }
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
}
.staffTree {
	  position: relative;
    top: -33px;
    left: 20px;
    z-index: 2;
    opacity: 0;
  .inputBox {
  	width:114px;
    .ivu-input-wrapper {
      width: 50%;
    }
  }
}

.selectNode {
  clear: both;
  width: 100%;
  height: 50px;
  color: #f28133;
  line-height: 50px;
  padding-left: 10px;
  font: {
    weight: bold;
    size: 16px;
  }
  border: 1px solid #eaeef1 !important;
}
.treeModal{
	position: relative;
	top: -5px;
}
</style>