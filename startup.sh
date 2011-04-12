#!/bin/bash
baseDir=`dirname $0`
cd $baseDir
echo "Launching Raston"
java -jar raston.jar > /dev/null &


