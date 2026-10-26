/*
 * @Descripttion: 
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
 */
let person={
  colums: [{
    title: "选择",
    width: 80,
    align: "center",
    render: (h, params) => {
      return h(
        "a",
        {
          attrs: {
            class: "tableEdit"
          },
        },
        '选择'
      );
    },
  },

  {
    title: "姓名",
    key: "lastName",
    minWidth: 110,
    align: "center"
  },

  {
    title: "工号",
    key: "emplid",
    minWidth: 110,
    align: "center",
  },

  {
    title: "账号类型",
    key: "oprid",
    minWidth: 110,
    align: "center",
  },

  {
    title: "公司/机构",
    key: "estabDes",
    minWidth: 110,
    align: "center",
  },

  {
    title: "部门",
    key: "deptDes",
    minWidth: 110,
    align: "center",
  },
  {
    title: "岗位",
    key: "posDes",
    minWidth: 110,
    align: "center",
  },],
  titleName: "对接人",
  postData:{
    condition:""
  },
  url: process.env.VUE_APP_CONFIG + "/longterm/unitConfig/selectBySec",
};
define({
  //单位配置--对接人
  'contactPersonList'() {
    return person;
  },
  //单位配置--审批人
  'auditPersonList'() {
    person.titleName="审批人";
    return person;
  },
  //行安全性--集合ID
  'jiheID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "集合ID",
          minWidth: 100,
          key: "id",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "集合描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        id: "",
        descr: ""
      },
      titleName: "集合ID",
      url: process.env.VUE_APP_CONFIG + "/common/pageCollectiveMagnifier",
    };
  },
  //部门ID
  'bumenID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "部门ID",
          minWidth: 100,
          key: "id",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "部门描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        lastId: "",//集合id
        id: "",
        descr: ""
      },
      titleName: "部门ID",
      url: process.env.VUE_APP_CONFIG + "/common/pageDeptMagnifier",
    };
  },

  //流程ID
  'flowID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "流程处理ID",
          minWidth: 100,
          key: "eoawprcsId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        eoawprcsId: "",
        descr: ""
      },
      titleName: "流程处理ID",
      url: process.env.VUE_APP_CONFIG + "/flowAuthorize/flowSelectVague",
    };
  },
  //角色名称
  'roleID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "角色名称",
          minWidth: 100,
          key: "rolename",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        rolename: "",
        descr: ""
      },
      titleName: "角色",
      url: process.env.VUE_APP_CONFIG + "/flowAuthorize/roleSelectVague",
    };
  },
  //用户账号
  'userId'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户ID",
          minWidth: 100,
          key: "oprid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "lastName",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        oprid: ""
      },
      url: process.env.VUE_APP_CONFIG + "/salaryUsers/userSelectVague",
    };
  },
  //薪资组
  'salaryID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "薪资组",
          minWidth: 100,
          key: "gpPaygroup",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        gpPaygroup: "",//薪资组名称
        descr: ""//描述
      },
      url: process.env.VUE_APP_CONFIG + "/salaryUsers/pygrpSelectVague",
    };
  },
  //工资归属
  'moneyID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "工资归属",
          minWidth: 100,
          key: "sigPayinId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "nameDisplay",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        sigPayinId: "",//工资归属
        nameDisplay: ""
      },
      url: process.env.VUE_APP_CONFIG + "/salaryUsers/payInSelectVague",
    };
  },
  //机构安全性
  'orgSafe'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "机构安全性",
          minWidth: 100,
          key: "estabidRole",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "estabidRoleDesc",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        estabidRole: "",
        estabidRoleDesc: ""
      },
      url: "/auMana/authorityHr/securityConfig/queryEstabidSecurity",
    };
  },
  //部门
  'dept'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "部门编码",
          minWidth: 100,
          key: "code",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        descr: ""
      },
      url: "/auMana/authorityHr/securityConfig/getDeptid",
    };
  },
  //部门安全性
  'deptSafe'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "部门安全性",
          minWidth: 100,
          key: "deptidRole",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "deptidRoleDesc",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        deptidRole: "",
        deptidRoleDesc: ""
      },
      url: "/auMana/authorityHr/securityConfig/queryDeptidSecurity",
    };
  },
  //用户账号
  'user'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户账号",
          minWidth: 100,
          key: "oprid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "name",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        name: "",
        oprid: ""
      },
      url: "/auMana/authorityHr/securityConfig/getUser",
    };
  },
  //流程
  'pageFlow'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "流程处理ID",
          minWidth: 100,
          key: "flowId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "流程名称",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        // name: "",
        flowId: ""
      },
      url: process.env.VUE_APP_CONFIG + "/wageAttributionSecurity/pageFlow",
    };
  },
  //用户账号
  'flowId'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户ID",
          minWidth: 100,
          key: "id",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "流程名称",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        // name: "",
        flowId: ""
      },
      url: process.env.VUE_APP_CONFIG + "/common/pageRoleMagnifier",
    };
  },
  //用户账号
  'pageEmplMagnifier'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户ID",
          key: "oprid",
          minWidth: 80,
          align: "center",
        },
        {
          title: "员工工号",
          key: "emplid",
          minWidth: 80,
          align: "center",
        },
        {
          title: "姓名",
          key: "emplName",
          minWidth: 80,
          align: "center",
        },
        {
          title: "业务单位",
          key: "businessUnitDescr",
          minWidth: 80,
          align: "center",
        },
        {
          title: "中心/机构",
          key: "institutionsDescr",
          minWidth: 80,
          align: "center",
        },
        {
          title: "部门",
          key: "deptDescr",
          minWidth: 80,
          align: "center",
        },
        {
          title: "岗位",
          key: "postDescr",
          minWidth: 80,
          align: "center",
        },
      ],
      postData: {
        // name: "",
        emplName: ""
      },
      url: process.env.VUE_APP_CONFIG + "/common/pageEmplMagnifier",
    };
  },
  //试用期开通公司范围
  'roleOpenCompany'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "公司",
          minWidth: 100,
          key: "company",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        company: "",
        descr: "",
      },
      url: "/auMana/authorityHr/ehrAuthConfig/queryCompanyTblPage",
    };
  },
  //  //工资归属审批人维护
  //  'roleID'(){
  //     return {
  //       colums: [
  //         {
  //           title: "序号",
  //           type: "index",
  //           width: 60,
  //           align: "center",
  //         },
  //         {
  //           title: "用户ID",
  //           minWidth: 100,
  //           key: "oprid",
  //           align: "center",
  //           ellipsis: true,
  //           tooltip: true,
  //         },
  //         {
  //           title: "姓名",
  //           key: "oprdefndesc",
  //           minWidth: 90,
  //           align: "center",
  //           ellipsis: true,
  //           tooltip: true,
  //         },
  //         {
  //           title: "机构",
  //           key: "estabDescr",
  //           minWidth: 90,
  //           align: "center",
  //           ellipsis: true,
  //           tooltip: true,
  //         },
  //         {
  //           title: "部门",
  //           key: "deptDescr",
  //           minWidth: 90,
  //           align: "center",
  //           ellipsis: true,
  //           tooltip: true,
  //         },
  //         {
  //           title: "岗位",
  //           key: "posnDescr",
  //           minWidth: 90,
  //           align: "center",
  //           ellipsis: true,
  //           tooltip: true,
  //         }
  //       ],
  //       postData: {
  //         oprid:"",
  //         oprdefndesc:"",
  //         estabDescr:"",
  //         deptDescr:"",
  //         posnDescr:"",
  //       },
  //       url: "/auMana/authorityHr/ehrAuthConfig/queryOprSrchVwPage",
  //     };
  // }, 
  //薪酬修改权限
  'roleUpdateID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户ID",
          minWidth: 100,
          key: "oprid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "oprdefndesc",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "机构",
          key: "estabDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "部门",
          key: "deptDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "岗位",
          key: "posnDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        name: "",
        oprid: ""
      },
      url: "/auMana/authorityHr/ehrAuthConfig/queryOprSrchVwPage",
    };
  },
  //薪酬查看权限
  'roleViewID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户列表",
          minWidth: 100,
          key: "eoawuserListId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "用户列表来源",
          key: "xlatlongname",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        eoawuserListId: "",
        descr: ""
      },
      url: "/auMana/authorityHr/ehrAuthConfig/queryEoawuserListPage",
    };
  },
  //薪酬查看权限用户
  'roleViewInfo'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "用户ID",
          minWidth: 100,
          key: "oprid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "oprdefndesc",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "机构",
          key: "estabDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "部门",
          key: "deptDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "岗位",
          key: "posnDescr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        oprid: "",
        oprdefndesc: "",
        estabDescr: "",
        deptDescr: "",
        posnDescr: "",
      },
      url: "/auMana/authorityHr/ehrAuthConfig/queryOprSrchVwPage",
    };
  },
  //管理用户权限管理角色
  'roleDataName'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "角色名称",
          minWidth: 100,
          key: "rolename",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 90,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        name: "",
        oprid: ""
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryRoleDefnPage",
    };
  },
  //业务单位管理用户权限
  'businessUnit'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "业务单位",
          minWidth: 100,
          key: "businessUnit",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        businessUnit: "",
        descr: ""
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryBusunits",
    };
  },
  //业务单位管理用户权限
  'businessUnitSafe'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "业务单位",
          minWidth: 100,
          key: "businessUnit",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "businessUnitDescr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      postData: {
        businessUnit: "",
        descr: ""
      },
      url: process.env.VUE_APP_CONFIG + "/rowSecurity/pageBusinessUnitSecurityDetail",
    };
  },
  //公司管理用户权限
  'companySafe'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "公司",
          minWidth: 100,
          key: "company",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "companyDescr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }
      ],
      url: process.env.VUE_APP_CONFIG + "/rowSecurity/pageCompanySecurityDetail",
    };
  },
  //人员组织范围管理用户权限
  'businessDept'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "组织范围代码",
          minWidth: 100,
          key: "sigGroupId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        }, {
          title: "生效日期",
          key: "effStatus",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
          render: (h, params) => {
            let typeData = params.row.effStatus;
            if (typeData == 'A') {
              typeData = "有效";
            } else if (typeData == 'I') {
              typeData = "无效";
            }
            return h("span", typeData);
          },
        }
      ],
      postData: {
        businessUnit: "",
        descr: "",
        sigGroupId: ''
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryPersGroupPage",
    };
  },
  //渠道权限分组管理用户权限
  'channelDept'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "渠道权限分组ID",
          minWidth: 100,
          key: "sigQdRightId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        businessUnit: "",
        descr: "",
        sigQdRightId: ''
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryQdFzRightPage",
    };
  },
  //渠道权限分组渠道ID
  'channelID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "渠道代码",
          minWidth: 100,
          key: "channelCod",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      // postData: {
      //   businessUnit:"",
      //   descr:"",
      //   channelCod:''
      // },
      url: "/auMana/authorityHr/shrAuthConfig/queryChannelPage",
    };
  },
  //渠道权限分组渠道业务分类
  'channelBusiness'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "渠道分类代码",
          minWidth: 100,
          key: "sigQdTypeId",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        sigQdTypeId: "",
        descr: "",
        channelCod: ''
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryChannelTypPage",
    };
  },
  //审批角色对应标岗
  'standardPost'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "标准岗位",
          minWidth: 100,
          key: "jobcode",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/querySroleJcodePage",
    };
  },
  //特例配置
  'EHRTypeID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "EHRID",
          minWidth: 100,
          key: "estabid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "sigDescr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryEstabDDvwPage",
    };
  },
  //特例配置 SHR机构ID
  'SHRID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "业务单位",
          minWidth: 100,
          key: "businessUnit",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "业务单位描述",
          key: "sigBusinessDescr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "SHR系统",
          key: "sigShrSys",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "系统描述",
          key: "sigShrSysDescr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "所属机构",
          key: "sigShrEstab",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "机构名称",
          key: "sigShrEstabName",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryShrOrgWfvPage",
    };
  },
  //特例配置 指定审批人
  'approvedBy'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "员工工号",
          minWidth: 100,
          key: "emplid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "lastName",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "用户ID",
          key: "oprid",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryOpridInfoPage",
    };
  },
  //流程审批人修改
  'approvalID'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "员工工号",
          minWidth: 100,
          key: "emplid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "姓名",
          key: "lastName",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "用户ID",
          key: "oprid",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        emplid: "",
        lastName: "",
      },
      url: "/auMana/authorityHr/shrAuthConfig/queryLOpridInfoPage",
    };
  },
  //角色配置维护
  'menuRole'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "菜单ID",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
  //员工工号
  'emplIdGet'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "员工工号",
          minWidth: 100,
          key: "emplid",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "员工姓名",
          key: "lastName",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      postData: {
        emplid: ""
      },
      url: process.env.VUE_APP_CONFIG + "/approvalAuthority/emplIdVague",
    };
  },
  //刷新单账户数据
  'sxzdsj'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "刷新单账户数据",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
  //操作用户ID
  'czyhid'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "操作用户ID",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
  //刷新的数据类型
  'sxsjlx'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "刷新的数据类型",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
  //系统/平台
  'xtpt'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "系统/平台",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "系统/平台描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
  //用户ID
  'oprid'() {
    return {
      colums: [
        {
          title: "序号",
          type: "index",
          width: 60,
          align: "center",
        },
        {
          title: "系统/平台",
          minWidth: 100,
          key: "menuName",
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
        {
          title: "系统/平台描述",
          key: "descr",
          minWidth: 110,
          align: "center",
          ellipsis: true,
          tooltip: true,
        },
      ],
      url: "/auMana/authorityHr/shrAuthConfig/queryMenuListVPage",
    };
  },
});