#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1;      export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh                   # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba;      export NAMESPACE # fallback
fi


echo "Applying mdp-builder-fg3"
kubectl create cm mdp-builder-fg3 -n ${NAMESPACE} --from-file=configs --dry-run=true -o yaml | kubectl apply -n ${NAMESPACE} -f -
kubectl apply -f mdpBuilder-service.yaml --validate=false -n ${NAMESPACE}
kubectl apply -f mdpBuilder-deployment.yaml -n ${NAMESPACE}
kubectl apply -f mdpBuilder-pvc.yaml -n ${NAMESPACE}
