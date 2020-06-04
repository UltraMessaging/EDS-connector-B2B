#!/bin/bash
LBM_INSTALL=/opt/UMQ/Linux-glibc-2.5-x86_64
export LBM_LICENSE_FILENAME=./Presales-lbm.lic

export LD_LIBRARY_PATH=$LBM_INSTALL/lib:$LD_LIBRARY_PATH

RUNOPTS="\
--stdin \
-c src.cfg \
-A 0:1 \
-i \
-M 100 \
-P 1000"

echo RUNOPTS = $RUNOPTS
java -cp '.:umq-spark-0.0.1.jar:/opt/UMQ/*' com.infa.presales.umqsrc $RUNOPTS $*
