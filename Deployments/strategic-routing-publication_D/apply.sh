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
echo "Applying strategic-routing-publication"
kubectl apply -f strategic-routing-publication-service.yaml -n ${NAMESPACE}
kubectl apply -f strategic-routing-publication-deployment.yaml -n ${NAMESPACE}
kubectl apply -f strategic-routing-publication-pvc.yaml -n ${NAMESPACE}
