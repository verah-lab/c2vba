#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

sh apply-config-map.sh ${NAMESPACE}
echo "Applying geo-manager-c2vba"
kubectl apply -f geo-manager-c2vba-service.yaml -n ${NAMESPACE}
kubectl apply -f geo-manager-c2vba-deployment.yaml -n ${NAMESPACE}
kubectl apply -f geo-manager-c2vba-pvc.yaml -n ${NAMESPACE}
