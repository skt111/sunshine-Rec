<template>
  <div class="staffTree">
    <div
      class="inputBox"
      @click="disabled ? model = false : model = true"
    >
      <Input
        type="text"
        v-model="comName"
        suffix="ios-arrow-down"
        placeholder="点击弹框选择"
        readonly
      />
    </div>
    <div
      v-if="model"
      class="ivu-modal-mask"
      style="z-index:1000;"
    ></div>
    <Modal
      v-model="model"
      :mask-closable="false"
      class-name="detailModal"
      scrollable
      draggable
      width="500"
    >
      <p slot="header">
        <span style="color:#fff;"> 数据范围 </span>
      </p>
      <div style="overflow:hidden;">
        <div class="orgEvery">
          <p class="mb10">
            <Checkbox
              v-model="isAll"
              @on-change="
                val => {
                  changAll(val);
                }
              "
            >全系统</Checkbox>
          </p>
          <div class="stationTree">
            <Tree
              v-if="!isAll"
              :data="inTreeData"
              :load-data="loadDataIn"
              :render="renderContentIn"
              ref="inTree"
              show-checkbox
              multiple
            ></Tree>
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
          @click="cancel()"
        >取消</Button>
      </div>
    </Modal>
  </div>
</template>
<script>
import util from "@/utils/util";
export default {
  name: "MutilTree",
  props: {
    disabled: {
      default: false
    }
  },
  data() {
    return {
      model: false,
      inTreeData: [],
      firstOrg:[],//一级机构
      isAll: false, //是否全系统
      comName: "" //机构名称
    };
  },
  watch: {},
  methods: {
    //确定
    ok() {
      let that=this,comCodes = [];
      if (!this.isAll) {
        var checkedList = this.$refs.inTree.getCheckedAndIndeterminateNodes();
        let dataProjectList = checkedList.filter(ch => {
          return ch.checked == true;
        });
        console.log(dataProjectList)
        comCodes = util.getKeyList(dataProjectList, "id");
        this.comName = util.getKeyValue(dataProjectList, "title");
      } else {
        this.comName = "全系统";
        comCodes=util.getKeyList(that.firstOrg, "id");
      }
      console.log(this.comName);
      //return false;
      //触发回调，把选择数据返给父组件
      this.$emit("onSelect", comCodes);
      this.model = false;
    },
    //取消
    cancel() {
      this.model = false;
      this.comName = "";
      this.initTree();
    },
    changAll(val) {
      this.isAll = val;
    },
    initTree() {
      let that = this;
      that.inTreeData = [];
      that.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "ehr",
          parentId: ""
        })
        .then(res => {
          let data = res.data;
          if (data.status == "success") {
            data.result.forEach((item, i) => {
              item.title = item.deptName;
              //判断是否有子节点
              if (item.isLeaf == "N") {
                item.loading = false;
                item.children = [];
              }
            });
            //全系统是否选中
            var checks=data.result.filter(item=>item.checked);
            that.firstOrg=util.arrDeepCopy(data.result);//一级机构信息
            that.isAll=checks.length==that.firstOrg.length;
            that.inTreeData = data.result;
          } else {
            console.log(data.message);
          }
        })
        .catch(error => {
          console.log(error);
        });
    },
    //点击名称的时候展示、收缩节点
    selectChangeTreeIn(node) {
      if (!node.expand) {
        this.loadDataIn(node, response => {
          if (!response) return; // 没有子节点则返回
          let array = [];
          response.forEach(item => {
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
            cursor: "pointer"
          },
          //点击事件取到节点数据，触发事件
          on: {
            click: () => {
              if (data.id) this.selectChangeIn(data);
            }
          }
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "md-people",
                color: "#2d8cf0",
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
    loadDataIn(item, callback) {
      // console.log(item);
      let that = this;
      this.axios
        .post("/auMana/authorityHr/role/queryCom", {
          flag: "ehr",
          parentId: item.id
        })
        .then(function(res) {
          if (res.data.status == "success") {
            res.data.result.forEach((ele, i) => {
              ele.title = ele.deptName;
              //判断是否有子节点
              if (ele.isLeaf == "N") {
                ele.loading = false;
                ele.children = [];
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

.stationTree {
  height: 250px;
  overflow: auto;
}

.staffTree {
  .inputBox {
    cursor: pointer;
    .ivu-input-wrapper {
      width: 65%;
    cursor: pointer;
    }
  }
}
</style>