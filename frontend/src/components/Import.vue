<template>
  <div class="position">
    <input
      ref="fileInput"
      type="file"
      @change="addFile()"
      name="myfile"
      id="file"
      class="file"
      accept=".xls,.xlsx"
    />
    <div
      id="fileSpan"
      @click="updata"
    >
      <a class="but">导入</a>
    </div>
  </div>
</template>
<script>
/*--------
导入：
<Import :importUrl="importUrl" :postData="postData"></Import>
importUrl:导入的url,
postData:参数
isImport：是否可见
--------*/
export default {
  name: "Import",
  props: {
    postData: {
      type: Object,
      default: function() {
        return { date: "GHQ" };
      }
    },
    importUrl: {
      type: String,
      default: "/indMan/excel/indexInput"
    }
  },
  data() {
    return {};
  },
  mounted: function() {},
  methods: {
    updata() {
      this.$refs.fileInput.value = "";
      this.$refs.fileInput.click();
    },
    addFile: function() {
      var that = this;
      // 获取文件对象
      var fileObj = document.getElementById("file").files[0];
      if (!fileObj) {
        return;
      }
      if (!this.postData.date) {
        that.$Modal.error({
          title: "信息",
          content: "请选择日历组"
        });
        return false;
      }
      // 接收上传文件的后台地址
      var FileController = this.importUrl + "/" + this.postData.date;
      console.log(FileController)
      //FormData对象
      var form = new FormData();
      // 可以增加表单数据
      //form.append("fileType", "picLogo");
      form.append("file", fileObj); // 文件对象
      //可以限制大小
      var size = Math.round(fileObj.size / 1024 / 1024);
      if (size > 31) {
        that.$Modal.error({
          title: "信息",
          content: "大小不得超过30M"
        });
        return;
      }
      // XMLHttpRequest 对象
      var xhr = new XMLHttpRequest();

      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = JSON.parse(xhr.response);
          if (response.status == "success") {
            /* that.$Modal.success({
              title: "信息",
              content: "账单已成功上传，可以进行数据比对。",
              onOk() {
                that.$emit("loadSuccess",response.result);
              }
            }); */
            that.$Message.success({
              title: "信息",
              content: "账单已成功上传，可以进行数据比对。",
              duration:3,
              onClose() {
                that.$emit("loadSuccess",response.result);
              }
            });
          } else {
            that.$Modal.warning({
              title: "信息",
              content: response.message
            });
          }
        }
        if (xhr.readyState == 4 && xhr.status != 200) {
          that.$Modal.error({
            title: "信息",
            content: xhr.statusText
          });
        }
      };
      xhr.send(form);
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.position {
  position: relative;
  display: inline-block;
  .file {
    height: 32px;
    position: relative;
    cursor: pointer;
    line-height: 26px;
    width: 100px;
    //z-index: 2;
    opacity: 0;
  }
  #fileSpan {
    display: inline-block;
    position: absolute;
    left: 0;
  }
}
</style>