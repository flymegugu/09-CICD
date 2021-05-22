//获取标准输出
//第一种
result = sh returnStdout: true ,script: "<shell command>"
result = result.trim()
//第二种
result = sh(script: "<shell command>", returnStdout: true).trim()
//第三种
sh "<shell command> > commandResult"
result = readFile('commandResult').trim()

//获取执行状态
//第一种
result = sh returnStatus: true ,script: "<shell command>"
result = result.trim()
//第二种
result = sh(script: "<shell command>", returnStatus: true).trim()
//第三种
sh '<shell command>; echo \$? > status'
def r = readFile('status').trim()

//无需返回值，仅执行 shell 命令
//最简单的方式
sh '<shell command>'

工作中需要获取 shell 命令的执行状态，返回 0 或者非 0
groovy 语句写法为：

    def exitValue = sh(script: "grep -i 'xxx' /etc/myfolder", returnStatus: true)
    echo "return exitValue :${exitValue}"
    if(exitValue != 0){
    	执行操作
    }

如果 grep 命令执行没有报错，正常情况下 exitValue 为 0，报错则为非 0

需要注意的是当命令中存在重定向的时候，会出现返回状态异常，因为我们要返回状态，删除重定向（&>/dev/null）即可，比如：

    def exitValue = sh(script: "grep -i 'xxx' /etc/myfolder &>/dev/null", returnStatus: true)
    xxx不存在，正常逻辑是返回非0，但是实际中返回的是0 。可以理解为先执行命令然后赋值操作，类似下面的动作：（个人理解）

sh "ls -l > commandResult"
result = readFile('commandResult').trim()
