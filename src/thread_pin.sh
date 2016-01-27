#!/usr/bin/env bash
LINE=$(jstack -l `ps ax | grep jar | grep macro | awk '{print $1}'` | grep Client | grep nid)
PID_HEX=$(echo $LINE | awk -F'nid=| runnable' '{printf $2}')
PID=$(($PID_HEX))
taskset -p 1 $PID

