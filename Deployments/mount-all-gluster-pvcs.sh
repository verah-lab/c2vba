#!/bin/bash

if   [ $# -gt 0 ];          then NAMESPACE=$1; export NAMESPACE  # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ./namespace.sh ]; then . ./namespace.sh                # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                        NAMESPACE=c2vba;   export NAMESPACE  # fallback
fi

h=`kubectl get nodes -n ${NAMESPACE} | grep "compute"| grep -v "NotReady" | awk "{ print \\$1 } " | sed "2,\\$d"`
kubectl get pvc -n ${NAMESPACE} | sed "1d" | awk "{ print \$1 \" \"  \$3}" | while read n p
do
	v=`kubectl describe pv -n ${NAMESPACE} ${p} | grep "vol_" | sed "s/^.*vol/vol/"`
	mkdir -p /mnt/pvc/${n} || true
	mount -t glusterfs ${h}:/${v} /mnt/pvc/${n} || true
done

h=`kubectl get nodes -n monitoring | grep "compute"| grep -v "NotReady" | awk "{ print \\$1 } " | sed "2,\\$d"`
kubectl get pvc -n monitoring | sed "1d" | awk "{ print \$1 \" \"  \$3}" | while read n p
do
	v=`kubectl describe pv -n monitoring ${p} | grep "vol_" | sed "s/^.*vol/vol/"`
	mkdir -p /mnt/pvc/${n} || true
	mount -t glusterfs ${h}:/${v} /mnt/pvc/${n} || true
done
