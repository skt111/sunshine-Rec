<template>
  <div class="position">
    <input
      ref="fileInput"
      type="file"
      v-on:change="addFile()"
      name="myfile"
      :id="id"
      class="file"
      v-if="isUpdate"
    />
    <div
      class="fileSpan"
      v-if="isUpdate"
      @click="updata"
    >
      <i-button icon="ios-cloud-upload-outline">{{buttonSpan}}</i-button>
    </div>
    <ul
      class="file-list"
      v-if="!isHome"
    >
      <li
        v-for="(item,index) in list"
        class="file-li"
        :key="'file'+index"
      >
        <span>{{index+1}}、</span>
        <a
          target="_blank"
          class="file-Name"
          @click="viewFile(item)"
          :title="item.fileName"
        >{{item.fileName}}</a>
        <input
          type="file"
          class="files"
          ref="filesImg"
          @change="changeFile(index)"
          v-if="isUpdate"
        />
        <a
          @click="deleteFile(index)"
          v-if="isUpdate"
        >删除</a>
      </li>
    </ul>
    <ul
      class="file-list"
      v-if="isHome"
    >
      <li
        v-for="(item,index) in list"
        class="file-li"
        :key="'files'+index"
      >
        <span
          target="_blank"
          class="file-Name"
          :title="item.fileName"
          :style="isUpdate?'':'border-left:1px solid #e5e5e5;'"
        >{{item.fileName}}</span>
        <a
          @click="deleteFile(index)"
          v-if="isUpdate"
        >删除</a>
      </li>
    </ul>
    <Loading ref="loadings" style='z-index:2;'></Loading>
  </div>
</template>
<script>
/*--------
上传文件：
<UploadFile :list="files" :upUrl="" :isHome="true" :isUpdate="false" @getFile="files"></UploadFile>
list已上传的文件列表[{},{},...,{}] ；
isUpdate是否可以上传，true上传编辑等，false展示；
isHome
upUrl

getFile()返回已上传文件列表信息
--------*/
import util from "@/utils/util";
import Loading from "@/components/Loading";

export default {
  name: "UploadFile",
  components: {
			Loading,
		},
  props: {
    list: {
      type: Array,
      default: function() {
        return [];
      }
    },
    isUpdate: {
      type: Boolean,
      default: true
    },
    upUrl: {
      type: String,
      default: "/indMan/file/upload/batch?fileType=picLogo"
    },
    postData: {
      type: String,
      default: ""
    },
    isHome: {
      type: Boolean,
      default: false
    },
    id:{
    	type: String,
    	default: 'file'
    },
    upKey:{
    	type:String,
    	default:''
    },
    item:{
    	type:Object,
    	default:()=>{return {};}
    },
    proTypeName:{
    	type:String,
    	default:'-'
    },
    buttonSpan:{
    	type:String,
    	default:'上传材料'
    },
  },
  data() {
    return {};
  },
  mounted: function() {
  },
  methods: {
    updata() {
      this.$refs.fileInput.value = "";
      this.$refs.fileInput.click();
    },
    addFile: function() {
      var that = this;
      this.$refs.loadings.show();
      if(!this.proTypeName){
      	that.$Message.error({
						title: "信息",
						content: '请先选择报送项目类型'
					});
					return false;
      }
      // 获取文件对象
      var fileObj = document.getElementById(this.id).files[0];
      if (!fileObj) {
      	this.$refs.loadings.hide();
        return;
      }
      
      //FormData对象
      var form = new FormData();
      // 可以增加表单数据
      //form.append("fileType", "picLogo");
      if(that.upKey){
					form.append('pinValue',that.item.pinValue);
		      form.append('id',that.item.id);
		      form.append('mainId',that.item.mainId);
		      form.append('pageSize',10);
		      form.append('pageNum',1);
				}
      
      // 接收上传文件的后台地址
      var FileController = this.upUrl;
      form.append('file', fileObj); // 文件对象
      if(FileController =='/adEds/adEd/upload' || fileObj.name.indexOf('.xlsx')>-1 || fileObj.name.indexOf('.xls')>-1){
      //可以限制大小
      var size = Math.round(fileObj.size / 1024 / 1024);
      if (size > 5) {
        alert("文件大小不得超过5M");
        return;
      }

      // XMLHttpRequest 对象
      var xhr = new XMLHttpRequest();

      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = xhr.response;
          //将图片信息加到data里的list中
          //if (that.isHome) {
            //首页只展示一个
            //that.list.splice(0, that.list.length);
          //}
          
          /*that.list.push({
            fileName: JSON.parse(response).result.sigAttaName,
            attachLink: JSON.parse(response).result.sigAttaPath
          });*/
         if(FileController =='/adEds/adEd/upload'){
         	that.list = [{'fileName':JSON.parse(response).result.calAttName}];
         }
          that.filesSend(xhr.response);
        }
        if (xhr.readyState == 4 && xhr.status != 200) {
          alert(xhr.statusText);
        }
      };
      xhr.send(form);
      }else{
      	that.$Message.error({
					title: "信息",
					content: '请下载模板，填写后上传！'
				});
   		}
    },
    changeFile: function(index) {
      var that = this;
      // 获取文件对象
      var fileObj = this.$refs.filesImg[index].files[0];
      if (!fileObj) {
        return;
      }
      var FileController = this.upUrl;
      var form = new FormData();
      form.append(that.id, fileObj); // 文件对象
      var xhr = new XMLHttpRequest();
      xhr.open("post", FileController, true);
      xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
          var response = xhr.response;
          //将图片信息加到data里的list中
          that.list.splice(index, 1, {
            fileName: JSON.parse(response).result.sigAttaName,
            attachLink: JSON.parse(response).result.sigAttaPath,
            attachType: JSON.parse(response).result.attachType
          });
          that.filesSend();
        }
        if (xhr.status != 200) {
          alert(xhr.statusText);
        }
      };
      xhr.send(form);
    },
    viewFile(item) {
      //util.excelDownload("/indMan/excel/index/down", item.attachLink);
      //下载数据
      let that = this;
      this.axios
        .post(
          "/socSecu/attConfig/download",
          {
            //params: {
              sigAttaPath: item.attachLink,
              sigAttaName: item.fileName
            //}
          },
          {
            responseType: "blob"
          }
        )
        .then(response => {
          if (response.status == 200) {
            util.downloadStream(response);
          } else {
            that.$Message.error({
              title: "信息",
              content: response.statusText
            });
          }
        })
        .catch(error => {
          that.$Message.error({
            title: "信息",
            content: response.statusText
          });
        });
    },
    deleteFile: function(index) {
      let that = this;
      this.$refs.fileInput.value = "";
      this.list.splice(index, 1);
      this.filesSend();
    },
    realDelFile: function() {
      let type = this.upUrl.split("=");
      this.axios
        .post("/indMan/file/delete/riskdeclarebook?indexSubject=" + type[1])
        .then(res => {
          console.log("删除成功");
        })
        .catch(error => {
          console.log(error);
        });
    },
    filesSend: function(response) {
    	if(response){
    		this.$emit("getFile", this.list, response);
    	}else{
    		this.$emit("getFile", this.list);
    	}
    }
  }
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.position {
  float: left;
  .file {
    height: 32px;
    position: relative;
    cursor: pointer;
    line-height: 26px;
    width: 100px;
    z-index: 2;
    opacity: 0;
    left: 115px;
  }
  .fileSpan {
    display: inline-block;
    position: absolute;
    left: 125px;
  }
  .file-list {
    list-style: none;
    display: block;
    .file-li {
      height: 32px;
      line-height: 32px;
      position: relative;
      span {
        width: 20px;
        float: left;
      }
      a {
        cursor: pointer;
        float: left;
      }
      .file-Name {
        width: 200px;
        margin-right: 20px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .files {
        position: absolute;
        left: 20px;
        cursor: pointer;
        height: 32px;
        line-height: 32px;
        width: 200px;
        z-index: 2;
        opacity: 0;
      }
    }
  }
}
</style>