// 富文本编译器
<template>
  <div class="editorDiv">
    <Editor
      :initial-value="EditorContents"
      @onChange="onChangeHandler"
      :id="'tinyMce'+idx"
      :init="editorInit"
      v-model="EditorContents"
    ></Editor>
  </div>
</template>

<script>
import tinymce from "tinymce/tinymce";
import "tinymce/themes/modern/theme";
import Editor from "@tinymce/tinymce-vue";
import "tinymce/plugins/image";
import "tinymce/plugins/link";
import "tinymce/plugins/code";
import "tinymce/plugins/table";
import "tinymce/plugins/lists";
import "tinymce/plugins/contextmenu";
import "tinymce/plugins/wordcount";
import "tinymce/plugins/colorpicker";
import "tinymce/plugins/textcolor";
export default {
  name: "TinyEditor",
  components: {
    Editor
  },
  props: {
    EditorContent: {
      default: "",
      type: String
    },
    idx: {
      default: 0,
      type: Number
    }
  },
  data() {
    let that = this;
    return {
      EditorContents: "",
      initialVal: "",
      editorInit: {
        language_url: "/static/tinymce/zh_CN.js",
        language: "zh_CN",
        selector: "textarea",
        skin_url: "/static/tinymce/skins/lightgray",
        height: 300,
        // width:600,
        plugins:
          "link lists image code table colorpicker textcolor wordcount contextmenu",
        toolbar:
          "bold italic underline strikethrough | fontsizeselect | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist | outdent indent blockquote | undo redo | link unlink image code | removeformat",
        // 初始化数据传递
        init_instance_callback: editor => {
          // 在插入位置插入内容, 光标会被移至右侧
          //console.log(editor);
          editor.setContent("");
          editor.insertContent(that.EditorContent);
        }
      }
    };
  },
  created() {
    tinymce.init({});
  },
  //最先开始执行
  mounted() {},
  methods: {
    onChangeHandler(res, index, tag) {
      let html = res.level.content;
      //this.EditorContents=html;
      //console.log(html)
      this.$emit("updateContent", html);
    }
  },
  watch: {
    $route() {
      this.$forceUpdate();
    },
    EditorContent(newVal, oldVal) {
      //debugger;
      //if (oldVal==''||newVal=='') {
        this.EditorContents = newVal;
      //}
    }
    /*EditorContents(newContent, old) {
      console.log('bbb')
      this.$emit("updateContent", newContent);
    } */
  }
};
</script>


<style scoped lang="scss">
.editorDiv {
  padding: 20px 0px;
  float: left;
  width: 100%;
}
</style>