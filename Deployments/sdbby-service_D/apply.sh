#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

if [ -f ../build-env.sh ]; then . ../build-env.sh
else                         BUILDENV=hb; export BUILDENV   # fallback
fi

sh apply-config-map.sh ${NAMESPACE}
echo "Applying sdbby-service"
kubectl apply -f sdbby-service-service.yaml -n ${NAMESPACE}
kubectl apply -f sdbby-service-deployment.yaml -n ${NAMESPACE}



if [ ${BUILDENV} == "prod" ]
then
	echo "Verwendet die app PVC?"
else
    kubectl apply -f sdbby-service-pvc.yaml -n ${NAMESPACE}
fi