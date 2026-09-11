#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

echo "Waiting for completion of c2vba-parameter-tool-app"
kubectl rollout status -f c2vba-parameter-tool-app-deployment.yaml -n ${NAMESPACE}
