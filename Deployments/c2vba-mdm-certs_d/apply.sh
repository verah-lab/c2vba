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


if [ ${BUILDENV} == "prod" ]
then
	echo "Deployment in Umgebung ${BUILDENV}"
	cd prod
	kubectl apply -f mdm-certs-environment.yaml -n ${NAMESPACE}
	kubectl apply -f mdm-urls-environment.yaml -n ${NAMESPACE}
    	kubectl apply -f publication-version-environment.yaml -n ${NAMESPACE}
	cd ..
else
	echo "Deployment in Umgebung ${BUILDENV}"
	kubectl apply -f mdm-certs-environment.yaml -n ${NAMESPACE}
    	kubectl apply -f mdm-urls-environment.yaml -n ${NAMESPACE}
    	kubectl apply -f publication-version-environment.yaml -n ${NAMESPACE}
fi
