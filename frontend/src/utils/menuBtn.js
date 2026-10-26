//菜单按钮操作处理
//按钮处理
function butFun(butData) {
    let data = [];
    butData.forEach(function (value, key) {
        if (value)
            data.push(value.operateCode);
    });
    return data;
}

function menuFun(menuData, modeType) {
    //菜单数据处理
    let that = this,
        data = [],
        menuIds = [];
    menuData.forEach(function (value, key) {
        if (value.menuUrl) {
            menuIds.push(value.menuCode);
        }
        if (value.parentMenuCode == modeType) {
            //有下一级菜单
            let temp = menuData.filter(function (item) {
                return item.parentMenuCode === value.menuCode;
            });

            let nodes = [];
            temp.forEach(function (i, j) {
                if (i.isLeaf == "N") {
                    i.nodes = menuNext(i, menuData);
                }
                nodes.push({
                    id: i.menuCode,
                    pid: i.parentMenuCode,
                    name: i.menuName,
                    url: i.menuUrl,
                    open: false,
                    nodes: i.nodes,
                    isThree: i.nodes && i.nodes.length > 0 ? true : false,
                    isActive: false,
                    icon: i.nodes && i.nodes.length > 0 ? "md-add" : ""
                });
            });
            data.push({
                id: value.menuCode,
                pid: null,
                name: value.menuName,
                url: value.menuUrl,
                open: false,
                className: "firstA",
                isActive: false,
                nodes: nodes
            });
        }
    });
    return {
        menu: data,
        menuIds: menuIds
    };
}

function menuNext(i, menuData) {
    let that = this;
    let tempNextT = menuData.filter(function (item) {
        return item.parentMenuCode === i.menuCode;
    });
    let nodesNextT = [];
    tempNextT.forEach(function (p, q) {
        if (p.isLeaf == "N") {
            p.nodes = that.menuNext(p, menuData);
        }
        nodesNextT.push({
            id: p.menuCode,
            pid: p.parentMenuCode,
            name: p.menuName,
            url: p.menuUrl,
            open: false,
            nodes: p.nodes,
            isThree: p.nodes && p.nodes.length > 0 ? true : false,
            isActive: false,
            icon: p.nodes && p.nodes.length > 0 ? "md-add" : ""
        });
    });
    return nodesNextT;
}

export default {butFun,menuFun}