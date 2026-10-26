<template>
  <div class="rightCon">
    <span class="leftSpan" :class="[moreLine ? 'moreLine' : '']" v-if="isShow"
      ><span class="reqFont" v-if="isRequired">*</span>{{ labelName }}</span
    >
    <Input
      type="text"
      maxlength="20"
      v-model="magnifyData"
      :title="magnifyData"
      :placeholder="'请输入' + labelName"
      @on-change="getSearchList"
      :disabled="disabled"
    />
    <Icon
      class="magnifyIcon"
      @click="magnifyOpen"
      type="ios-search"
      v-if="!disabled"
    />
    <Icon
      class="magnifyBtnClose"
      v-show="magnifyData"
      @click="magnifyOk({})"
      type="md-close-circle"
      v-if="!disabled"
    />
    <ul
      class="ulCon"
      :style="ulStyle"
      v-show="this.magnifyList.length > 0 && isMore && !disabled"
    >
      <li
        @click="magnifyOk(item)"
        v-for="(item, idx) in magnifyList"
        :title="item[queryKey]"
        :key="'magnify' + Math.random() + idx"
      >
        {{ item[queryKey] }}
      </li>
    </ul>
    <Loading ref="loading"></Loading>
    <Modal
      v-model="magnifyShow"
      :mask-closable="true"
      class-name="cityModal"
      :footer-hide="true"
      scrollable
      width="900"
    >
      <p slot="header">
        <span>{{ labelName }}</span>
      </p>
      <div style="overflow: hidden; padding: 5px">
        <Magnify
          ref="magnify"
          :isPage="isPageM"
          :isFirst="false"
          :requestType="requestType"
          :selectType="selectType"
          @onSend="
            (value) => {
              magnifyOk(value.params);
            }
          "
        >
          <div
            slot-scope="magnifyData"
            style="overflow: hidden; width: 100%; margin-bottom: 10px"
          >
            <div class="lineDiv">
              <div class="inputDiv">
                <span class="spanName">{{ labelName }}：</span>
                <input
                  @keyup.enter="query"
                  type="text"
                  class="input"
                  maxlength="50"
                  v-model="magnifyData.data[selectKey]"
                />
              </div>
            </div>
          </div>
        </Magnify>
      </div>
    </Modal>
  </div>
</template>
  
  <script>
import Loading from "@/components/Loading";
import Magnify from "@/components/magnify/MagnifySlot";
import { postDataList, getList } from "../../api/dataRequst";

export default {
  name: "FuzzyMagnify",
  components: {
    Loading,
    Magnify,
  },
  data() {
    return {
      magnifyShow: false, //放大镜弹框
      isMore: false, //下拉选择
      timer: null, //定时器
      magnifyData: "", //输入框展示的数据
      magnifyList: [
        // {
        //   code: "1",
        //   codeShortName: "测试",
        // },
      ], //放大镜下拉选择
      ulStyle: {},
    };
  },
  props: {
    //是否分页放大镜
    isPageM: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //是否显示左侧span
    isShow: {
      type: Boolean,
      default: () => {
        return true;
      },
    },
    //放大镜请求
    requestType: {
      type: String,
      default: () => {
        return "post";
      },
    },
    //放大镜类型
    selectType: {
      type: String,
      default: () => {
        return "";
      },
    },
    //放大镜搜索框的key
    selectKey: {
      type: String,
      default: () => {
        return "";
      },
    },
    //查询接口
    queryUrl: {
      type: String,
      default: () => {
        return "";
      },
    },
    //是否可编辑
    disabled: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //是否必填
    isRequired: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    //输入框内容
    queryData: {
      type: String,
      default: () => {
        return "";
      },
    },
    //字段属性
    queryKey: {
      type: String,
      default: () => {
        return "codeShortName";
      },
    },
    //标签说明
    labelName: {
      type: String,
      default: () => {
        return "";
      },
    },
    //标题是否换行
    moreLine: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
  },
  watch: {
    queryData() {
      this.magnifyData = this.queryData;
    },
    scrollTop(newVal, oldVal) {
      this.ulStyle = { transform: `translateY(-${newVal}px)` };
    },
  },
  computed: {
    scrollTop() {
      return this.$store.state.scrollTop;
    },
  },
  created() {},
  mounted() {},
  methods: {
    query() {
      this.$refs.magnify.query();
    },
    getSearchList(e) {
      if (this.timer) {
        clearTimeout(this.timer);
      }
      this.timer = setTimeout(() => {
        //调后台查list数据
        this.searchList(e.target.value);
      }, 1000);
    },
    async searchList(value) {
      this.isMore = true;
      this.magnifyList = [];
      //return false;
      this.$refs.loading.show();
      try {
        let res = await postDataList(
          process.env.VUE_APP_CONFIG + this.queryUrl,
          {
            [this.queryKey]: value,
            pageSize: 10,
            pageNum: 1,
          }
        );
        this.$refs.loading.hide();
        if (res.data.status == "success") {
          this.magnifyList = res.data.result.list;
        } else {
          this.$Message.error({
            title: "信息",
            duration: 5,
            closable: true,
            content: res.data.result.message,
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
    //放大镜查询
    magnifyOpen() {
      this.magnifyShow = true;
    },
    //选择放大镜确定
    magnifyOk(value) {
      this.isMore = false;
      this.magnifyShow = false;
      this.$refs.magnify.reset();
      this.magnifyData = value[this.queryKey];
      this.$emit("setVal", value);
    },
  },
};
</script>
  
  <style lang="scss" scoped>
</style>