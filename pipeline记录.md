1. 在 groovy 语法中通过 sh 引用的 shell 命令不能获取 groovy 定义的变量
2. var 内的是全局变量 直接在 steps 下使用即可,groovy 实例后的需要在 scipt
3. sh 不支持 for i in {} bash 支持 ，但是在 pipeline 中不能用，需要转换为 \$(seq 1 3)
4. 在 sh 中不要使命令输出到&> /dev/null 这样命令是不执行得
5. pipeline 语句返回值为 1 则退出，我们可以设置 set +e 来跳过为 1 时自动终止 pipeline

```
def call() {
    sh '''
set +e
grep ^/swap /etc/fstab
if [ $? -eq 0 ];then
    swapoff -a
    sed -i \'s/^.*swap.img/#&/g\' /etc/fstab
    echo "已关闭swap交换分区"
else
    swapoff -a
fi'''
}
```

6. 流水线使用重定向要用 2>&1 得形式,如果用&>那么就不会输出到指定文件

```
#此方式可以将ls xxx.txt的信息返回到test.log内
nohup ls /root/xxx.txt > /root/test.log 2>&1 &

#此方式不可以将ls xxx.txt的信息返回到test.log内
nohup ls /root/xxx.txt &> /root/test.log
```
