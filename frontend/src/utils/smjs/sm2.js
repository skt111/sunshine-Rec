/*
 * @Descripttion: 
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
 */
// 生成公私钥
export function generateKeySm2() {
    var sm2 = new SM2();
    var keyPair = sm2.generateKeyPairHex(); //得到的keypair结构：{'privkeyhex': hPrv, 'pubkeyhex': hPub}
    return {
        pubKey: keyPair['pubkeyhex'].toUpperCase(),
        privkey: keyPair['privkeyhex'].toUpperCase()
    };
}
//加密
export function enCodeSm2(pubkey, inputVal) {
    if (pubkey == "" || pubkey.length < 64) {
        alert("公钥为空或错误");
    }
    if (inputVal.length <= 0) {
        alert("输入数据不能为空");
    }
    var sm2DataHex = Hex.utf8StrToHex(inputVal); //转成byte数组

    //sm2加密
    var sm2 = new SM2();
    var cipher = sm2.encrypt(pubkey, sm2DataHex); //函数输入输出都是16进制数据

    return cipher;
}
//解密
export function deCodeSm2(privkey, inputVal) {
    if (privkey == "" || privkey.length < 32) {
        alert("私钥为空或错误");
    }
    if (inputVal.length <= 0) {
        alert("输入数据不能为空");
    }

    //sm2加密
    var sm2 = new SM2();
    var plain = sm2.decrypt(privkey, inputVal);

    var utf8Str = Hex.hexToUtf8Str(plain);
    return utf8Str;
}