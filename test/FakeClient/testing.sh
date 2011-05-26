#!/bin/sh

for i in `seq 1 1 60`; do
	java JixyClient client${i} &
done
