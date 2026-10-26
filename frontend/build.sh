#动态设置版本
NOW=$(date +"%Y%m%d%H%M%S")

export VUE_APP_RELEASE_VERSION="teste$NOW"

echo $VUE_APP_RELEASE_VERSION


#判断传入的参数
if [ "$1" = "production" ]; then

echo "正在为生产环境打包…"

npm run build

elif [ "$1" = "test" ]; then

echo "正在为测试环境打包…"

npm run int

else

echo "参数错误：请指定打包环境。使用 ‘production’ 或 ‘test’"

exit 1

fi


#检查构建是否成功

if [ -d 'dist' ]; then

echo '构建成功，正在删除.map文件…'

# 寻找并删除dist目录下的所有.map文件

find dist -name '*.map' -type f -delete

echo ".map文件已删除。"

else

echo "构建失败，dist目录不存在。"

fi