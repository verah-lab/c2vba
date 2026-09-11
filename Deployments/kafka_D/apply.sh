#!/bin/bash

if   [ $# -gt 0 ];          then NAMESPACE=$1; export NAMESPACE  # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ./namespace.sh ]; then . ./namespace.sh                # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                        NAMESPACE=c2vba;   export NAMESPACE  # fallback
fi


MY_STAGE=`kubectl get secrets/application-environment -n c2vba --template={{.data.SYSTEM_STAGE}} | base64 -d`
echo "Deployment on stage ${MY_STAGE} ==> server.properties entsprechend umkopieren"
cp config/server.properties-${MY_STAGE} config/server.properties

kubectl delete cm kafka-config -n ${NAMESPACE}
kubectl create cm kafka-config --from-file=config/ -n ${NAMESPACE}

kubectl apply -f deploy.yaml -n ${NAMESPACE}
kubectl apply -f pvc.yaml -n ${NAMESPACE}
kubectl apply -f svc.yaml -n ${NAMESPACE}
