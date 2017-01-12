#!/bin/bash
. /etc/profile
. /etc/bashrc
. ~/.bash_profile
. ~/.bashrc

APP_NAME="MaijiaMQ"
MAIN_CLASS="com.maijia.mq.console.Main"
JAVA_OPTS="-D$APP_NAME -server -Xms512m -Xmx512m -XX:MaxDirectMemorySize=64m -XX:PermSize=128m -XX:MaxPermSize=128m"
CLASSPATH=$CLASSPATH:../config
for i in ../lib/*jar ; do
	CLASSPATH=$CLASSPATH:$i
done

export CLASSPATH=$CLASSPATH

check() {
    ERROR=0
    if [ "-1" -eq $ERROR ]; then
        echo
        exit 1
    fi
}

start() {
    echo "JAVA_HOME=$JAVA_HOME"
    echo "CLASSPATH=$CLASSPATH"
    echo "MAIN_CALSS=$MAIN_CLASS"
    echo "$APP_NAME trying to start ..."
    check || exit 1
    nohup $JAVA_HOME/bin/java $JAVA_OPTS $MAIN_CLASS > ../log/console.log &
    echo "$APP_NAME started success."
}

stop() {
    echo "Stopping $APP_NAME ..."
    kill -9 `ps -ef|grep $APP_NAME|grep -v grep|grep -v stop|awk '{print $2}'`
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo $"Usage: $0 {start|stop|restart}"
        exit 1
esac
