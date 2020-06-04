#!/bin/bash
LBM_INSTALL=/opt/UMQ/Linux-glibc-2.5-x86_64
export LBM_LICENSE_FILENAME=./Presales-lbm.lic

export LD_LIBRARY_PATH=$LBM_INSTALL/lib:$LD_LIBRARY_PATH

RUNOPTS="\
 -s 5 \
 -S \
 --ascii \
 -D \
 -I 196 \
 -c rcv.cfg \
 "
MORE_CP='.:/usr/lib/spark/lib/*:umq-spark-0.0.1.jar:/opt/UMQ/*' 
echo RUNOPTS = $RUNOPTS
java -cp $MORE_CP com.infa.presales.spark.UmqSparkReceiver $RUNOPTS $*
