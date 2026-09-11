# #!/bin/bash
set -e

if   [ $# -gt 0 ];           then NAMESPACE=$1; export NAMESPACE # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ../namespace.sh ]; then . ../namespace.sh              # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                         NAMESPACE=c2vba; export NAMESPACE   # fallback
fi

if [ -f ../build-env.sh ]; then . ../build-env.sh
else                         BUILDENV=hb; export BUILDENV   # fallback
fi


if [ ${BUILDENV} == "prod" ]
then
	mkdir -p /nfs-data/parameter-data && chown -R heuboe:heuboe /nfs-data/parameter-data
else
    kubectl apply -f parameter-data-pvc.yaml -n ${NAMESPACE}
fi


sh apply-config-map.sh ${NAMESPACE}
echo "Applying c2vba-parameter-tool-app"

# create and initialize persistent volume
kubectl apply -f parameter-data-initializer.yaml -n ${NAMESPACE}
kubectl wait -n ${NAMESPACE} --for=condition=Ready pod/parameter-data-initializer
kubectl exec parameter-data-initializer -n ${NAMESPACE} -- sh -c 'rm -rf /parameter-data/converter-jars'
kubectl exec parameter-data-initializer -n ${NAMESPACE} -- sh -c 'rm -rf /parameter-data/parameter-definitions'
###kubectl cp pvc-content/converter-jars parameter-data-initializer:/parameter-data -n ${NAMESPACE}
kubectl cp pvc-content/parameter-definitions parameter-data-initializer:/parameter-data -n ${NAMESPACE}
kubectl delete -f parameter-data-initializer.yaml -n ${NAMESPACE}

# apply deployment
kubectl apply -f c2vba-parameter-tool-app-service.yaml -n ${NAMESPACE}
kubectl apply -f c2vba-parameter-tool-app-deployment.yaml -n ${NAMESPACE}

