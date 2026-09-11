#!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1;      export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh                   # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE # fallback
fi

if [ -f ../build-env.sh ]; then . ../build-env.sh
else                         BUILDENV=hb; export BUILDENV   # fallback
fi


echo "Applying site-config-service"
kubectl create cm site-config-service --from-file=configs --dry-run=true -o yaml | kubectl apply -n $NAMESPACE -f -
kubectl apply -n $NAMESPACE -f site-config-service-service.yaml
kubectl apply -n $NAMESPACE -f site-config-service-deployment.yaml
##kubectl apply -n $NAMESPACE -f site-config-service-route.yaml

if [ ${BUILDENV} == "prod" ]
then
	mkdir -p /nfs-data/site-data && chown -R heuboe:heuboe /nfs-data/site-data
fi