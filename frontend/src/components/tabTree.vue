<template>
  <div class="staffTree">
    <div
      class="inputBox"
      @click="disabled ? model = false : model = true"
    >
      <Input
        type="text"
        v-model="userNameShow"
        suffix="ios-people"
        disabled
        @click.native="search"
      />
    </div>
    <Modal
      v-model="model"
      class-name="choiceModal hasFoot"
      width="1200"
      :styles="{top: '35px'}"
    >
      <p
        slot="header"
        :class='{selected : selectNo==0,tabP:true}'
        @click="toggleTab(0)"
      >
        <Icon type="ios-clock" size="25" :color='selectNo==0 ? "#f4781f":"#929292"' style='margin-right: 3px;'></Icon>
        <span class="treeModal" style='line-height: 50px;top:-1px;'>最近</span>
      </p>
      <p
        slot="header"
        :class='{selected : selectNo==1,tabP:true}'
        @click="toggleTab(1)"
      >
        <Icon
          type="ios-people"
          size="25"
          :color='selectNo==1 ? "#f4781f":"#929292"'
          style='margin-right: 3px;'
        ></Icon>
        <span class="treeModal" style='line-height: 50px;top:-1px;'>机构人员树</span>
      </p>
      <div v-if="selectNo==1">
        <div class="org_path">
          <span>{{orgPath}}</span>
        </div>
        <div
          class="search_input"
        >
          <input
            type="text"
            v-model="searchStr"
            placeholder="请输入关键字"
            v-on:keypress='queryStaff'
          />
          <Icon
            type="ios-search"
            size="24"
            @click="searchStaff('page')"
          />
        </div>
      </div>
      <div v-else>
        <div class="org_path">
          <span>{{orgPath}}</span>
        </div>
        <div
          class="search_input"
        >
          <input
            type="text"
            v-model="searchStrRecent"
            placeholder="请输入关键字"
            v-on:keypress='queryRecent'
          />
          <Icon
            type="ios-search"
            size="24"
            @click="searchRecentStaff()"
          />
        </div>
      </div>
      <div style="height:316px;position: relative;" v-if="selectNo==1">
      	<div style="position: absolute;width: 270px;left: 314px;top: 36px;">
      		<div
          class="search_input"
          style='width: 270px;'
	        >
	          <input
	            type="text"
	            v-model="searchOrgStr"
	            placeholder="请输入关键字"
	            v-on:keypress='queryOrg'
	          />
	          <Icon
	            type="ios-search"
	            size="24"
	            @click="searchOrgSim()"
	          />
	        </div>
	        <div v-if="orgListFlag" class="liList" style='max-height: 200px;overflow-y: scroll;'>
	        	<p v-for='item in orgList' @click="searchThisTree(item)" :title="item.DESCRSHORT">{{item.DESCR}}</p>
	        </div>
      	</div>
        <div
          class="department"
          :class="onlyOrg?'maxWidth':''"
        >
          <Tree
          	ref='tree'
            :data="department"
            :render="renderContent"
            :load-data="loadData"
          ></Tree>
        </div>
        <div
          class="staff"
        >
          <div
            class="select_all"
          >
            <Checkbox
              :indeterminate="indeterminate"
              :value="checkAll"
              @click.prevent.native="handleCheckAll"
            >全选</Checkbox>
          </div>
          <div id="listContent">
            <template v-for="(vv, kk) in staff">
              <p
                :key="'mmm'+kk"
              >
                <label><input
                    type="checkbox"
                    :value="vv.checked"
                    @change="selectPerson(vv,kk)"
                    :checked="vv.checked"
                  >
                  {{vv.LAST_NAME}}</label>
              </p>
              <div
                class="person_from"
                :key="'pf'+kk"
              >{{vv.DESCR5}}_{{vv.SIG_DESCR101}}</div>
            </template>
          </div>
          <div v-if='noneUserList'>
            <img src="../assets/img/common/no_data.png" />
          </div>
        </div>
        <div style="float: right;" v-if="staff.length>0 && pageShowFlag">
          <Page
            ref="pagenate"
            :total="total"
            :current="1"
            :page-size='limit'
            @on-change="changePage"
            :show-sizer="false"
            :show-total="true"
          ></Page>
        </div>
      </div>
      <div style="height:300px;" v-else>
	        <div style="width:100%;border: 1px solid #eaeef1;" class="department">
	        	<div style="width:50%;float: left;padding: 3px;" v-for="(item,index) in recentlist">
	        		<p
                :key="'mmm'+index"
              >
                <label><input
                    type="checkbox"
                    :value="item.checked"
                    @change="selectPerson(item,index,'recent')"
                    :checked="item.checked"
                  >
                  {{item.LAST_NAME}}</label>
              </p>
              <div
                class="person_from"
                :key="'pf'+index"
              >{{item.DESCR5}}_{{item.SIG_DESCR101}}</div>
	        	</div>
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
              <span :title="v.title||v.LAST_NAME||v.memberName">{{v.LAST_NAME}}</span>
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
<script>/*-----------
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
		Dialog
	},
	props: {
		//控制弹窗是否可以选择  默认为false可弹  true为不可弹窗
		disabled: {
			default: false
		},
		//是否仅选择机构
		onlyOrg: {
			default: false
		},
		//多人还是单人
		personNum: {
			default: "much"
		},
		//组id
		groupTypeCode: {
			default: ""
		},
		//组名称
		groupTypeName: {
			default: ""
		},
		//组号
		groupNum: {
			default: ""
		},
		//是否是组长
		isGroupLeader: {
			default: ""
		},
		//编号
		index: {
			default: ""
		},
		// 某个人员对象
		diffOrg: {
			type: Object,
			default: function() {
				return {};
			}
		},
		//选择人员的时候触发orgList变化，orgList又会反作用于staff中的diffOrg变化
		changeStaff: {
			default: false
		},
		//多个机构列表
		orgLists: {
			default: function() {
				return [];
			}
		},
		//多个人员的列表
		persons: {
			default: function() {
				return [];
			}
		},
		aboutMan: {
			default: "0"
		}
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
			selectNo:0,
			recentlist:[],
			searchStrRecent:'',
			searchOrgStr:'',
			orgList:[],
			orgListFlag:true,
			page: 1,
      limit: 20,
      onFooter: 0,
      onPullDown: true, //下拉加载
      listlength: 0,
      total:0,
      pageShowFlag:false,
      DEPTID:'',
      BUSINESS_UNIT:''
		};
	},
	watch: {
		diffOrg: {
			handler() {
				this.staffItem = JSON.parse(JSON.stringify(this.diffOrg));
				this.staffItem.minName = this.staffItem.memberName;
				this.staffItem.lastName = this.staffItem.memberName;
				this.userNameShow = this.staffItem.lastName;
				if(this.staffItem.minName && this.changeStaff) {
					this.selectArr.push(this.staffItem);
				}
			},
			deep: true,
			immediate: true
		},
		orgLists: {
			handler() {
				if(this.orgLists.length > 0) {
					this.selectArr = this.orgLists;
					if(this.orgLists.length == 1) {
						//机构的话显示的字段是title，人员显示的字段是lastName
						this.orgNames = this.orgLists[0].title;
					} else {
						this.orgNames = this.orgLists[0].title + "等";
					}
					this.orgLists.forEach(function(v, i) {
						v.minName =
							v.title && v.title.length > 6 ?
							v.title.slice(0, 6) + "..." :
							v.title;
					});
				}
			},
			deep: true,
			immediate: true
		},
		persons: {
			handler: function(val, oldVal) {
				if(this.persons.length > 0) {
					this.persons.forEach(function(v, i) {
						v.minName = v.lastName;
					});
					this.selectArr = this.persons;
					if(this.persons.length == 1) {
						//机构的话显示的字段是title，人员显示的字段是lastName
						this.userNameShow = this.persons[0].lastName;
					} else {
						this.userNameShow = this.persons[0].lastName + "等";
					}
				}
			},
			deep: true,
			immediate: true
		}
	},
	mounted() {
		this.searchStr = '';
		this.searchOrgStr = '';
		this.pageShowFlag = false;
	},
	methods: {
		changePage(index) {
      this.page = index;
      this.searchStaff('page');
    },
    //修改回写时的参数处理
    setProps(data){
    	var that = this;
    	data.forEach((item, i) => {
    		if(item.TREE_NODE == that.DEPTID && item.SETID == that.BUSINESS_UNIT){
							that.selectChange(item);
						}
    		if(!item.OPEN) {
					item.loading = false;
				}
	      item.title = item.DESCRSHORT;
	      if(item.children){
	      	item.expand = true;
	      	this.setProps(item.children);
	      }else{
	      	item.children = [];
	      }
	    });
	    return data;
    },
		searchThisTree(item){
			var that = this;
			this.searchOrgStr = item.DESCRSHORT;
			this.orgListFlag = false;
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/querydeptTreeByDeptId",{deptId:item.DEPTID,bu:item.BUSINESS_UNIT})
				.then(res => {
					that.$refs.loadings.hide();
					res.data.result.forEach(function(v,i){
						v.title = v.DESCRSHORT;
						//判断是否有子节点
						if(!v.OPEN) {
							v.loading = false;
						}
						if(v.TREE_NODE == item.DEPTID && v.SETID == item.BUSINESS_UNIT){
							that.selectChange(v);
						}
						that.DEPTID = item.DEPTID;
						that.BUSINESS_UNIT = item.BUSINESS_UNIT;
	      		if(v.children){
	      			res.data.result[i] = that.setProps([res.data.result[i]])[0];
	      		}else{
	      			v.children = [];
	      			v.loading = false;
	      		}
	      	});
					that.department = res.data.result;
				})
				.catch(function(error) {
				});
		},
		queryOrg(e){
      var keyCode = null;
      if (e.which) keyCode = e.which;
      else if (e.keyCode) keyCode = e.keyCode;

      if (keyCode == 13) {
        this.searchOrgSim();
        return false;
      }
      return true;
		},
		//机构模糊查询
		searchOrgSim(){
			var that = this;
			if(this.searchOrgStr == "") {
				this.sure("请输入关键字再进行搜索！");
				return false;
			}
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/querydeptByName",{deptName:this.searchOrgStr})
				.then(res => {
					that.$refs.loadings.hide();
					that.orgListFlag = true;
					that.orgList = res.data.result;
				})
				.catch(function(error) {});
		},
		search(){
			if(this.selectNo==0){
				this.initRecentlist();
			}else{
				this.searchRoot();
			}
		},
		initRecentlist(){
			var that = this;
			that.clearSelectArr();
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/queryrecently",{
					'queryString':'',
				})
				.then(res => {
					that.$refs.loadings.hide();
					for(let i = 0; i < res.data.result.length; i++) {
						that.selectArr.forEach((value, index) => {
							if(res.data.result[i].EMPLID == value.EMPLID) {
								that.$set(res.data.result[i], "checked", true);
							}
						});
					}
					that.recentlist = res.data.result;
				})
				.catch(function(error) {});
		},
		toggleTab(num){
	  		this.selectNo = num;
	  		if(this.selectNo==0){
					this.initRecentlist();
				}else{
					this.searchRoot();
				}
	  	},
		clearSelectArr() {
			this.selectArr = [];
			this.orgNames = "";
			this.userNameShow = "";
		},
		queryRecent(e){
      var keyCode = null;
      if (e.which) keyCode = e.which;
      else if (e.keyCode) keyCode = e.keyCode;

      if (keyCode == 13) {
        this.searchRecentStaff();
        return false;
      }
      return true;
		},
		searchRecentStaff(){
			var that = this;
			this.$refs.loadings.show();
			this.axios
				//.get("/inductAuto/eprotocol/common/queryEmployeeByName?queryString="+ this.searchStrRecent)
				.post("/inductAuto/eprotocol/common/queryrecently",{
					'queryString':that.searchStrRecent ? that.searchStrRecent :'',
				})
				.then(res => {
					that.$refs.loadings.hide();
					for(let i = 0; i < res.data.result.length; i++) {
						that.selectArr.forEach((value, index) => {
							if(res.data.result[i].EMPLID == value.EMPLID) {
								that.$set(res.data.result[i], "checked", true);
							}
						});
					}
					that.recentlist = res.data.result;
				})
				.catch(function(error) {});
		},
		queryStaff(e){
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
		searchStaff(page) {
			var that = this;
			if(page == 'page'){
				that.pageShowFlag = true;
			}
			this.checkAll = false;
			this.showPerson = false;
			if(this.searchStr == "") {
				this.sure("请输入关键字再进行搜索！");
				return false;
			}
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/queryEmployeeByName2",{
					'name':that.searchStr,
					'pageNo':that.page,
					'pageSize':that.limit
				})
				.then(function(res) {
					that.$refs.loadings.hide();
					res.data.result.persionList.forEach((item, i) => {
						item.checked = false;
						item.minName = item.lastName;
					});
					for(let i = 0; i < res.data.result.persionList.length; i++) {
						that.selectArr.forEach((value, index) => {
							if(res.data.result.persionList[i].EMPLID == value.EMPLID) {
								that.$set(res.data.result.persionList[i], "checked", true);
							}
						});
					}
					that.staff = res.data.result.persionList;
					that.total = res.data.result.total;
					if(that.staff.length == 0) {
						that.noneUserList = true;
					} else {
						that.noneUserList = false;
					}
				})
				.catch(function(error) {});
		},
		//全选框选中或取消的切换
		handleCheckAll() {
			var that = this;
			if(this.indeterminate) {
				this.checkAll = false;
			} else {
				this.checkAll = !this.checkAll;
			}
			this.indeterminate = false;

			if(this.checkAll) {
				this.staff.forEach((value, index) => {
					value.checked = true;
				});
				for(var i = 0; i < this.staff.length; i++) {
					var flag = false;
					that.selectArr.forEach((value, index) => {
						if(that.staff[i].EMPLID == value.EMPLID) {
							flag = true;
						}
					});
					if(!flag){
						this.selectArr.push(this.staff[i]);
					}
				}
				/*for(var i = 0; i < this.staff.length; i++) {
					
				}*/
			} else {
				for(var i = 0; i < this.staff.length; i++) {
					that.selectArr.forEach((value, index) => {
						if(that.staff[i].EMPLID == value.EMPLID) {
							that.selectArr.splice(index,1);
						}
					});
				}
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
				if(v.EMPLID == value.EMPLID) {
					this.$set(value, "checked", false);
				}
			});
			this.recentlist.forEach((value, index) => {
				//判断是否有子节点
				if(v.EMPLID == value.EMPLID) {
					this.$set(value, "checked", false);
				}
			});
		},
		//确定
		ok() {
			var that = this;
			if(this.selectArr.length > 0){
				this.$emit("onSelect", {
					selectArr: this.selectArr
				});
			}
			this.model = false;
		},
		//取消
		canel() {
			this.model = false;
		},
		//根据机构查人员
		selectChange(data) {
			/*let that = this;
		        this.axios
		          .post("/inductAuto/eprotocol/common/query_empl_by_dept", {
							"deptId": data.TREE_NODE+'',
							"startNum":data.TREE_NODE_NUM+'',
							"endNum":data.TREE_NODE_NUM_END+'',
							"bu":data.SETID,
							"searchContent":""
							})
		          .then(function(res) {
		          	res.data.result.forEach(function(item,index){
		          		item.checked = false;
						item.minName = item.lastName;
		          	});
		            that.staff = res.data.result;
		          })
		          .catch(function(error) {
		            console.log(error);
		          });*/
		    let that = this;
		    that.pageShowFlag = false;
		    that.checkAll = false;
		    that.searchStr = '';
		    this.searchOrgStr = '';
		      //判断选择人员还是机构
		    this.$refs.loadings.show();
		    this.axios
	          .post("/inductAuto/eprotocol/common/query_empl_by_dept", {
						"deptId": data.TREE_NODE+'',
						"startNum":data.TREE_NODE_NUM+'',
						"endNum":data.TREE_NODE_NUM_END+'',
						"bu":data.SETID,
						"searchContent":""
						})
	          .then(function(res) {
	          	that.$refs.loadings.hide();
	          	res.data.result.forEach(function(item,index){
	          		item.checked = false;
					item.minName = item.lastName;
					that.selectArr.forEach((value, index) => {
						if(item.EMPLID == value.EMPLID) {
							that.$set(item, "checked", true);
						}
					});
	          	});
	          	
	            that.staff = res.data.result;
	          })
	          .catch(function(error) {
	            console.log(error);
	          });
		},
		selectChangeTree(node) {
			var that = this;
			if(!node.expand) {
				if(node.children.length > 0) {
					that.$set(node, "expand", true);
				} else {
					this.loadData(node, response => {
						if(!response || response.length == 0) return; // 没有子节点则返回
						let array = [];
						response.forEach(item => {
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
		selectPerson(item, i, type) {
			if(type=='recent'){
				this.recentlist[i].checked = !this.recentlist[i].checked;
				if(this.recentlist[i].checked){
					this.selectArr.push(item);
				}else{
					this.selectArr.forEach((value, index) => {
						//判断是否有子节点
						if(item.EMPLID == value.EMPLID) {
							this.selectArr.splice(index, 1);
						}
					});
				}
			}else{
				this.staff[i].checked = !this.staff[i].checked;
				if(this.staff[i].checked) {
					this.selectArr.push(item);
				} else {
					this.selectArr.forEach((value, index) => {
						//判断是否有子节点
						if(item.EMPLID == value.EMPLID) {
							this.selectArr.splice(index, 1);
						}
					});
				}
			}
		},
		//添加icon
		renderContent(h, {
			root,
			node,
			data
		}) {
			return h(
				"span", {
					style: {
						display: "inline-block"
					},
					domProps:{
            className:'btn'
        	},
					//点击事件取到节点数据，触发事件
					on: {
						click: (e) => {
							this.selectChange(data);
							let btns=this.$refs.tree.$el.querySelectorAll('.btn')
                for(let i=0;i<btns.length;i++){
                    btns[i].style.backgroundColor='white';
                }
                e.currentTarget.style.backgroundColor="rgb(176, 214, 255)";//当前点击的元素
                	
						}
					}
				}, [
					h("span", [
						h("Icon", {
							props: {
								type: "md-people",
								color: "#f4781f",
								size: "16"
							},
							style: {
								marginRight: "8px"
							},
						}),
						h("span", data.title)
					])
				]
			);
		},
		//异步加载子节点
		loadData(item, callback) {
			let that = this;
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/query_children_dept", {
					"bu": item.SETID,
					"num": item.TREE_NODE_NUM + ''
				})
				.then(function(res) {
					that.$refs.loadings.hide();
					if(res.data.status == "success") {
						res.data.result.forEach((item, i) => {
							item.title = item.DESCRSHORT;
							item.minName = item.title;
							item.children = [];
							//判断是否有子节点
							if(!item.OPEN) {
								item.loading = false;
							}
						});
						callback(res.data.result);
					}
				})
				.catch(function(error) {
					console.log(error);
				});
		},
		//机构树的初始化查询
		searchRoot() {
			let that = this;
			this.searchStr = '';
			this.searchOrgStr = '';
		  this.pageShowFlag = false;
			that.staff = [];
			that.selectArr = [];
			//请求一级机构数据
			this.$refs.loadings.show();
			this.axios
				.post("/inductAuto/eprotocol/common/architecture_query")
				.then(function(res) {
					that.$refs.loadings.hide();
					if(res.data.status == "success") {
						res.data.result.forEach((item, i) => {
							item.title = item.DESCRSHORT;
							item.children = [];
							//判断是否有子节点
							if(!item.OPEN) {
								item.loading = false;
								
							}
						});
						that.department = res.data.result;
					}
				})
				.catch(function(error) {
					console.log(error);
				});
		},
		sure(content) {
			this.dialogType = "sure";
			this.content = content;
			this.$refs.dialog.show();
		}
	}
};</script>
<style lang="scss" scoped>@mixin staffTree {
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
	z-index: 100;
	input {
		width: 100%;
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
.selected{
	background: white;
	color: #f4781f;
}
.tabP{
	height:42px;
	font-size:16px;
	width:125px;
	text-align: center;
	cursor:pointer;
}
/deep/ .ivu-input-wrapper.ivu-input-wrapper-default.ivu-input-type-text{
	z-index: 10;
    width: 114px;
    position: relative;
    top: -29px;
    left: 20px;
    opacity: 0;
}
/deep/ .ivu-modal-header{
	padding-bottom: 0;
}
.liList{
 	padding: 0 5px;
 	p{
 		margin:5px 0;
 		border-bottom:1px solid #eee;
 	}
}
</style>