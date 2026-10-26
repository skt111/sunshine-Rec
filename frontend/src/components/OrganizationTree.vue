<template>
  <div ref="searchMain" :class="defaultClass" style="position: relative">
    <Tooltip
      v-if="showtip && selectText"
      max-width="200"
      :transfer="true"
      :content="selectText"
    >
      <Input
        v-model="selectText"
        :placeholder="placeholder"
        :suffix="isSuffix ? 'ios-arrow-down' : ''"
        :disabled="disabled"
        :readonly="readonly"
        @on-focus="treeFocus"
        style="width: 100%"
      >
        <template v-show="clearable && selectText" #suffix>
          <Icon type="md-close" @click="cleanUpData()" />
        </template>
      </Input>
    </Tooltip>
    <Input
      v-else
      v-model="selectText"
      :placeholder="placeholder"
      :suffix="isSuffix ? 'ios-arrow-down' : ''"
      :disabled="disabled"
      :readonly="readonly"
      @on-focus="treeFocus"
      style="width: 100%"
    >
      <template v-if="clearable && selectText" #suffix>
        <Icon type="md-close" @click="cleanUpData()" />
      </template>
    </Input>
    <div
      v-show="treeShow"
      class="treebox"
      :class="isInputTreeHide || isSuffix ? 'standard-tree' : ''"
      v-bind:style="treeStyle"
    >
    <div class="selectCon">
      <Input v-model="searchCon" />
      <Icon type="ios-search" @click="initTree"/>
    </div>
      <Tree
        :data="treeData"
        :render="renderContentIn"
        :load-data="loadData"
        :check-strictly="treecheck"
        :show-checkbox="checkbox"
        :multiplex="checkbox"
        @on-check-change="changeCheck"
        ref="inTree"
        class="treeDiv"
      ></Tree>
    </div>
  </div>
</template>

<script>
import { postDataList } from "@/api/dataRequst";
export default {
  components: {},
  name: "OrganizationTree",
  /**
   * 组织树组件
   * 入参: value               必传:   无默认 节点id    返回字符串,单选树返回树的id,如:10000001
   *                                       多选树返回 ';' 分号分隔的id,如: 10000001;10000002;20080000;20040001
   *      text                选传:   无默认 id+文本/文本  返回字符串,单选树返回树的id或文本,如: 10000001-集团/集团
   *                                                   多选树返回 ',' 分号分隔的id与文本,如: 10000001-信报, 10000002-产险
   *      checkbox:           选传    无默认 多选树 默认为 false  传true可多选
   *      lazyLoad:           选传    默认 true 是否懒加载，传false不启用懒加载
   *      treeData            选传    无默认 树的数据
   *      cache               选传    默认 false  是否缓存，用于表格中嵌套组织树，否则一般情况不需要缓存数据，且现不考虑懒加载
   *      checkAuth:          选传    默认 true 权限校验，传false不启用权限校验
   *      disabled:           选传    默认 false 是否禁用
   *      treeStyle:          选传    无默认 扩展样式与v-bind:style用法一致 如：treeStyle="{color:'red'}"
   *      callback:           选传    无默认 回调函数，选择节点时触发，返回选择的节点对象信息
   *      isInputTreeHide:    选传    默认 true，是否需要初始化时直接显示组织树，从而达到不需要单击文本框显示
   *      expandOnClickNode   选传    默认 false 是否在点击节点的时候展开或者收缩节点，如果为 true，则只有点箭头图标的时候才会展开或者收缩节点。
   *      parameter           选传    无默认 object对象参数，用于调用组织树所需的附加参数 parameter.assemblyID-->组织名称是否显示id
   *      cascadedCode        选传    无默认 用于级联
   *      disabledAll         选传    默认 true 树是否需要右键按钮
   *      isSuffix            选传    默认 false 是否需要下拉图标
   *      clearable           选传    默认 true 单选时，有清空选项 默认有清空
   *      treeUrl             选传    默认 “/tscDevelopment/track/lazyEsTabTree” 接口路径(业务单位查询部门),默认查询其他
   *      treeQuerType        选传    无默认  input发生变化时调用
   *      placeholder         选传    默认 “请选择树结构” input空时默认值
   * 调用示例：
   *    有级联时 <OrganizationTree :cascadedCode.sync="queryForm.businessUnit" 这里是通过业务单位级联，其他级联也传这里
   *                             :value.sync="queryForm.estabId" 这里是机构id
   *                               :text.sync="queryForm.estabName" 这里是机构文本 />
   *    无级联时 <OrganizationTree :value.sync="queryForm.estabId" 这里是机构id
   *                             :text.sync="queryForm.estabName" 这里是机构文本 />
   */
  props: {
    value: { type: String },
    text: String,
    checkbox: { type: Boolean, default: false },
    showtip: { type: Boolean, default: false },
    lazyLoad: { type: Boolean, default: true },
    treecheck: { type: Boolean, default: false },
    cascadedCode: String,
    disabled: Boolean,
    readonly: { type: Boolean, default: true },
    callback: Function,
    treeStyle: Object, //待验证，
    isInputTreeHide: { type: Boolean, default: true },
    isSuffix: { type: Boolean, default: false },
    expandOnClickNode: { type: Boolean, default: false },
    placeholder: {
      type: String,
      default: function () {
        return "请选择组织范围";
      },
    },
    url: {
      type: String,
      default: function () {
        return "/riskhuman/hrcs/home/queryComDepts";
      },
    },
    searchCallback: Function,
    clearable: {
      type: Boolean,
      default: true,
    },
    taskAppOrgList: {
      type: Array,
      default: () => {
        return [];
      },
    },
  },
  data() {
    return {
      treeList: [], //树的数据
      defaultCheckedKeys: [],
      defaultProps: {
        label: "text",
        children: "children",
        isLeaf: "leaf",
        id: "id",
        disabled: function (obj, treeObj) {
          return obj.isDisabled != "X";
        },
      },
      loading: false,
      treeShow: false,
      selectText: "",
      defaultClass: "tree-overflow",
      isEncode: process.env.VUE_APP_ENCODE,
      treeData: [], //树的数据
      searchCon:"",//下拉搜索内容
    };
  },
  watch: {
    text(val, oldVal) {
      this.selectText = val;
    },
    selectText(newVal, oldVal) {
      this.$emit("update:text", newVal);
    },
    taskAppOrgList: {
      handler(newVal) {
        if (newVal && newVal.length) {
          this.selectText = newVal
            .map((item) => {
              return item.deptName;
            })
            .join("，");
        }
      },
      immediate: true,
      deep: true,
    },
  },
  created() {},
  mounted() {
    //做不到完美的组织数默认值（考虑多选、懒加载等因素），所以，初始化的时候，输入框默认默认显示text的值以达到默认显示的效果；
    if (this.text != null && this.text != "") {
      this.selectText = this.text;
    }
    if (this.value != null && this.value != "") {
      let valueArr = this.value.split(";");
      let defaultArr = [];
      for (let i = 0; i < valueArr.length; i++) {
        defaultArr.push(valueArr[i].toString());
      }
      this.$nextTick(() => {});
    }
  },
  methods: {
    emptyOptions(data) {
      data.map((item) => {
        if (item.checked != undefined || item.indeterminate != undefined) {
          item.checked = false;
          item.indeterminate = false;
        }
        if (item.children && item.children.length > 0) {
          this.emptyOptions(item.children);
        }
      });
    },
    changeCheck(data, item) {
      if (data.length > 5 && this.treecheck) {
        this.$Modal.error({
          title: "提示",
          content: "最多支持点选五个组织",
        });
        this.$set(item, "checked", false);
        let idx = data.findIndex((sub) => {
          return (sub.id = item.id);
        });
        data.splice(idx, 1);
      }
      this.selectText = data
        .map((item) => {
          return item.deptName;
        })
        .join("，");
      this.$emit("update:text", this.selectText);
      this.$emit("handleFinish", data);
    },
    /**
     * 清理数据
     */
    cleanUpData() {
      this.selectText = "";
      this.$emit("update:value", "");
      this.$emit("update:bunit", "");
      this.$emit("clearTree");
    },
    //节点渲染
    renderContent(h, { root, node, data }) {
      return h(
        "span",
        {
          style: {
            display: "inline-block",
            float: "left",
            width: "100%",
          },
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "ios-folder-outline",
              },
              style: {
                marginRight: "6px",
              },
            }),
            h("span", data.title),
          ]),
        ]
      );
    },
    // 表单获取焦点 显示树形
    treeFocus() {
      this.initTree();
      if (this.isInputTreeHide) {
        this.treeShow = true;
      }
      document.addEventListener("click", this.hidePanel, false);
    },
    // 隐藏树形方法过
    treeHide() {
      if (this.isInputTreeHide) {
        this.treeShow = false;
      }
      document.removeEventListener("click", this.hidePanel, false);
    },
    hidePanel(e) {
      if (this.$refs.searchMain == null) return;
      if (!this.$refs.searchMain.contains(e.target)) {
        this.treeHide();
      }
    },
    //查询树跟节点expandNode
    async initTree() {
      try {
        let res = await postDataList(this.url, {});
        if (res.data.status == "success") {
          if (res.data.result) {
            this.treeData = res.data.result;
            this.treeData.forEach((item) => {
              if (item.isLeaf == "N") {
                this.$set(item, "loading", false);
                this.$set(item, "children", []);
              }
              let flag = this.taskAppOrgList.some((sub) => {
                return (
                  sub.businessUnit == item.businessUnit &&
                  sub.deptId == item.deptId
                );
              });
              if (flag) {
                this.$set(item, "checked", true);
              }
            });
          }
        } else {
          this.$Message.error(res.data.message);
        }
      } catch (err) {
        this.$Modal.error({
          title: "信息",
          content: err,
        });
      }
    },
    //异步加载子节点
    async loadData(item, callback) {
      try {
        let that = this;
        let res = await postDataList(this.url, {
          businessUnit: item.businessUnit,
          deptId: item.deptId,
          level: item.level,
        });
        if (res.data.status == "success") {
          res.data.result.forEach((ele, i) => {
            //判断是否有子节点
            if (ele.isLeaf == "N") {
              ele.loading = false;
              ele.children = [];
            }
            let flaga = this.taskAppOrgList.some((sub) => {
              return (
                sub.businessUnit == ele.businessUnit && sub.deptId == ele.deptId
              );
            });
            if (flaga) {
              this.$set(ele, "checked", true);
            }
          });
          let childAll = [...item.children, ...res.data.result];
          callback(childAll);
        } else {
          item.loading = false;
          that.$Message.error({
            title: "信息",
            content: data.message,
          });
        }
      } catch (err) {
        this.$Modal.error({
          title: "信息",
          content: err,
        });
      }
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
              if (this.checkbox) {
                return false;
              }
              this.selectChangeIn(data);
            },
          },
        },
        [
          h("span", [
            h("Icon", {
              props: {
                type: "ios-folder",
                color: "#1A66E5",
                size: "16",
              },
              style: {
                marginRight: "8px",
              },
            }),
            h(
              "span",
              { class: "deptCon", attrs: { title: data.deptName } },
              data.deptName
            ),
          ]),
        ]
      );
    },
    selectChangeIn(data) {
      console.log(data);
      this.$emit("update:value", data.deptId);
      this.$emit("update:text", data.deptName);
      this.$emit("update:bunit", data.businessUnit);
      this.selectText = data.deptName;
    },
  },
  computed: {},
};
</script>
<style lang="scss" scoped>
/deep/ .standard-tree {
  .ivu-tree-title {
    line-height: 25px;
    margin-top: 4px;
  }
  .ivu-tree ul li {
    margin: 0;
    line-height: 21px;
  }
  .ivu-tree-arrow {
    margin-top: 7px;
  }
}
.tree-overflow {
  display:inline-block;
  padding:0px !important;
  vertical-align: middle;
  border:none !important;
}
.tree-overflow .el-tree {
  background-color: #fff;
  position: absolute;
  z-index: 999;
  min-width: 100%;
}
.tree-overflow-isNot .el-tree {
  /*height: 220px;*/
  background-color: #fff;
  position: absolute;
  /*left: 0px;*/
  z-index: 999;
  width: 100%;
}
.tree-parent {
  position: relative;
}
.treebox {
  min-width: 200px;
  height: 220px;
  overflow-y: auto;
  border: 1px solid #ececec;
  background-color: #fff;
  position: absolute;
  left: 0px;
  z-index: 99;
  border-radius: 4px;
  color: #2c3e50;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.2);
  /deep/.ivu-tree-arrow .ivu-icon {
    color: #2c3e50 !important;
  }
}
.tree-info .ivu-tree {
  position: absolute;
  z-index: 9999;
  background: #fff;
  line-height: 17px;
}
</style>
