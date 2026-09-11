#!/bin/bash
set -e

# Name des Services, Deployments und ConfigMap des Deployments
SERVICENAME=tls-sequencer

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

if [ -f ../build-env.sh ]; then . ../build-env.sh
else                         BUILDENV=hb; export BUILDENV   # fallback
fi


echo "Applying ${SERVICENAME}"
kubectl create cm ${SERVICENAME} -n ${NAMESPACE} --from-file=configs --dry-run=true -o yaml | kubectl apply -n ${NAMESPACE} -f -
kubectl apply -f ${SERVICENAME}-service.yaml -n ${NAMESPACE}
kubectl apply -f ${SERVICENAME}-deployment.yaml -n ${NAMESPACE}

if [ ${BUILDENV} == "prod" ]
then
	mkdir /nfs-data/config-data && chown -R heuboe:heuboe /nfs-data/config-data
else
    kubectl apply -f config-data-pvc.yaml -n ${NAMESPACE}
fi