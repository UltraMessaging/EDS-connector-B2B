#!/bin/bash
SPARK_HOME=/usr/lib/spark/
LBM_INSTALL=/opt/UMQ/Linux-glibc-2.5-x86_64
export LBM_LICENSE_FILENAME=./Presales-lbm.lic
MORE_JARS=/opt/UMQ/UMS_6.9_jdk1.5.0_12.jar
export LD_LIBRARY_PATH=$LBM_INSTALL/lib:$LD_LIBRARY_PATH

RUNOPTS="\
 -s 5 \
 -S \
 -E \
 -D \
 -I 1 \
 --spark \
 --ascii \
 -c rcv.cfg \
 "

echo RUNOPTS = $RUNOPTS
$SPARK_HOME/bin/spark-submit --jars $MORE_JARS --master 'local[*]' --class com.infa.presales.spark.test.UmqWordCount  umq-spark-0.0.1.jar $RUNOPTS $*
