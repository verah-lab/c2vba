#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

if [ -f ../build-env.sh ]; then . ../build-env.sh
else                         BUILD_ENV=hb; export BUILD_ENV   # fallback
fi


sh apply-config-map.sh ${NAMESPACE}
echo "Applying c2vba-datex2-region-receiver-www"
kubectl apply -f c2vba-datex2-region-receiver-www-service.yaml -n ${NAMESPACE}
kubectl apply -f c2vba-datex2-region-receiver-www-deployment.yaml -n ${NAMESPACE}
kubectl apply -f c2vba-datex2-region-receiver-www-pvc.yaml -n ${NAMESPACE}
