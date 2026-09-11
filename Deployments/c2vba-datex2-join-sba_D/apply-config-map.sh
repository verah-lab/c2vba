#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

echo "Applying c2vba-datex2-join-sba configs"
kubectl create cm c2vba-datex2-join-sba -n ${NAMESPACE} --from-file=configs --dry-run=true -o yaml > config.yaml
kubectl apply -f config.yaml -n ${NAMESPACE}
rm -f config.yaml
