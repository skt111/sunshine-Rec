/*
 * @Descripttion: 
 inputVal:加密的信息
 encodeKey：秘钥
 * @version: 
 * @Author: wuxiaoling-ghq
 * @LastEditors: wuxiaoling-ghq
 */
//生成私钥key
export function generateKeySm4() {
    var rng = new SecureRandom();
    var keyBit = new BigInteger(128, rng);
    while (keyBit.bitLength() < 128) {
        keyBit = new BigInteger(128, rng);
    }
    var key = ("0000000000" + keyBit.toString(16)).slice(-128 / 4);
    return key.toUpperCase();
}
//加密
export function enCodeSm4(inputVal, encodeKey) {
    var inputBytes = Hex.utf8StrToBytes(inputVal);
    var key = Hex.decode(encodeKey);
    var sm4 = new SM4();
    var cipher = sm4.encrypt_ecb(key, inputBytes);
    return Hex.encode(cipher, 0, cipher.length);
}
//解密
export function deCodeSm4(inputVal, encodeKey) {
    var inputBytes = Hex.decode(inputVal);
    var key = Hex.decode(encodeKey);
    var sm4 = new SM4();
    var plain = sm4.decrypt_ecb(key, inputBytes);
    return Hex.bytesToUtf8Str(plain);
}