  <template>
  <div
    class="dialog"
    ref="dialog"
  >
    <div
      v-if="dialog"
      class="ivu-modal-mask"
      style="z-index:1000;"
    ></div>
    <Modal
      v-model="dialog"
      :mask-closable="false"
      :class-name='classString'
      scrollable
      :closable="false"
      width="300"
    >
      <p slot="header">
        <span style="color:#fff;">信息</span>
      </p>
      <div style="overflow:hidden;font-size:16px;text-align:center;" v-html="content">
      </div>
      <div slot="footer">
        <div v-if="type=='confirm'">
          <a
            @click="cancel"
            class="cancleBtn"
          >取消</a>
          <a
            @click="ok"
            class="sureBtn"
          >确定</a>
        </div>
        <div v-else>
          <a
            @click="ok"
            class="sureBtn"
          >确定</a>
        </div>
      </div>
    </Modal>
  </div>
</template>
<script>
/*
<Dialog ref="dialog" :content="content" :type="confirm" @onOk="dialogOk" @onClose="dialogClose" @onOkTo="sureTo"></Dialog>

type:confirm  取消和确认
type:sure     只有确认关闭
type:sureTo   只有确认关闭后回调事件
*/
export default {
  name: "Dialog",
  props: {
    content: {
      type: String,
      default: ""
    },
    type: {
      type: String,
      default: "sure"
    },
    dialogClass: {
    	type: String,
    	default:""
    }
  },
  data() {
    return {
      dialog: false,
      element: "",
      classString: "detailModal vertical-center-modal"
    };
  },
  created(){
  	if(this.dialogClass){
  		this.classString= this.classString+' '+this.dialogClass;
  	}
  	
  },
  mounted() {},
  methods: {
    show(ele) {
      this.element = ele;
      this.dialog = true;
    },
    hide() {
      this.dialog = false;
    },
    ok() {
      let that = this;
      this.hide();
      if (this.type == "confirm") {
        that.$emit("onOk", {
          btnType: that.element
        });
      } else if (this.type == "sureTo") {
        that.$emit("onOkTo");
      } else if (this.type == "sure") {
        that.$emit("onOk", {
          btnType: that.element
        });
      }
    },
    cancel() {
      this.hide();
      this.$emit("onClose", {
        btnType: this.element
      });
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.demo-spin-col {
  width: 100%;
  height: 100%;
  position: fixed;
  z-index: 999999;
  font-size: 18px;
  .demo-spin-icon-load {
    animation: ani-demo-spin 1s linear infinite !important;
  }
  .ivu-icon-ios-loading {
    font-size: 36px;
  }
}
.sureBtn{
      width: 90px;
    height: 25px;
    text-align: center;
    background: #2d8cf0;
    color: #fff;
    padding: 5px 30px;
    line-height: 30px;
    font-size: 14px;
    border-radius: 4px;
    border: 1px solid #2d8cf0;
}
.cancleBtn{
	margin-right:20px;
}
/deep/.ivu-modal-header{
  background:#2d8cf0;
}
/deep/ .addMoney .ivu-modal-header{
  background:#f93;
}
/deep/ .addMoney .sureBtn{
  background:#f93;
  border-color:#f93;
}
/deep/ .addMoney .cancleBtn{
  color:#f93;
}


</style>