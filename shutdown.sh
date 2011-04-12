#!/bin/bash
baseDir=`dirname $0`
cd $baseDir
echo "Shutting down Raston"
java -jar raston.jar shutdown


