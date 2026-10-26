<template>
    <div class="loading">
        <Col class="demo-spin-col" span="8"  v-show="displayShow">
            <Spin fix>
                <Icon type="ios-loading" class="demo-spin-icon-load"></Icon>
                <div>正在加载中，请稍后...</div>
            </Spin>
        </Col>
    </div>
</template>
<script>
/*
<Loading ref="loading"></Loading>
*/
export default {
    name: 'Loading',
    data() {
        return {
            displayShow: false
        }
    },
    mounted() {

    },
    methods: {
        show() {
            this.displayShow = true;
        },
        hide() {
            this.displayShow = false;
        }
    }
}
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.loading{
    left: 0;
    top: 0;
    position: fixed;
}
.demo-spin-col {
    width: 100%;
    height: 100%;
    position: fixed;
    z-index: 999999;
    font-size: 18px;
    .demo-spin-icon-load {
        animation: ani-demo-spin 1s linear infinite !important;
    }
    .ivu-icon-ios-loading{
        font-size:36px;
    }
}
</style>