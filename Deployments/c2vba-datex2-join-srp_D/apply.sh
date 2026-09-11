#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

sh apply-config-map.sh ${NAMESPACE}
echo "Applying c2vba-datex2-join-srp"
kubectl apply -f c2vba-datex2-join-srp-service.yaml -n ${NAMESPACE}
kubectl apply -f c2vba-datex2-join-srp-deployment.yaml -n ${NAMESPACE}
kubectl apply -f c2vba-datex2-join-srp-pvc.yaml -n ${NAMESPACE}
