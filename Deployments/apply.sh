#!/bin/bash

if   [ $# -gt 0 ];          then NAMESPACE=$1; export NAMESPACE  # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ./namespace.sh ]; then . ./namespace.sh                # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                        NAMESPACE=c2vba;   export NAMESPACE  # fallback
fi

echo '######  APPLY NAMESPACE c2vba CONFIG ######'
if ! kubectl describe namespace ${NAMESPACE} >/dev/null 2>&1
then
	kubectl create namespace ${NAMESPACE}
fi
[ -f c2vba-logs-pvc.yaml ] && kubectl apply -f c2vba-logs-pvc.yaml   -n ${NAMESPACE}
kubectl apply -f application-environment.yaml -n ${NAMESPACE}

[ -f ./cert/c2vba-tls-secret.key ] && ! kubectl get secret c2vba-tls-secret -n ${NAMESPACE} >/dev/null 2>&1  && kubectl create secret tls c2vba-tls-secret --key=./cert/c2vba-tls-secret.key --cert=./cert/c2vba-tls-secret.crt  -n ${NAMESPACE}


echo '######  APPLY c2vba APPLICATIONS ######'
for i in *_D *_d
do
(
	[ -d ${i} ] && cd ${i} && chmod 755 ./apply.sh && ./apply.sh ${NAMESPACE}
)
done

hasRoute=$(kubectl get route --all-namespaces --no-headers -o custom-columns=:.metadata.namespace,:.metadata.name 2>/dev/null| grep -v "^${NAMESPACE}" | wc -l)
hasIngress=$(kubectl get ingress --all-namespaces --no-headers -o custom-columns=:.metadata.namespace,:.metadata.name 2>/dev/null| grep -v "^${NAMESPACE}" | wc -l)
if [ $hasRoute -gt 0 ]
then
	mkdir -p ../tmp >/dev/null 2>/dev/null
	cp create-route.sh ../tmp
	cd ../tmp
	chmod 755 create-route.sh && ./create-route.sh
	cd route-definitions
	chmod 755 deploy-routes.sh && ./deploy-routes.sh
elif [ $hasIngress -gt 0 ]
then
	mkdir -p ../tmp >/dev/null 2>/dev/null
	cp create-ingress.sh ../tmp
	cd ../tmp
	chmod 755 create-ingress.sh && ./create-ingress.sh
	cd ingress-definitions
	chmod 755 deploy-ingress.sh && ./deploy-ingress.sh
else
	echo "Weder Route noch Ingress Deployment"
fi

storageclass=$(kubectl get storageclass --no-headers -o custom-columns=:.metadata.name | grep -iv block)
[ $storageclass = "glusterfs-storage" ] && [ -f mount-all-gluster-pvcs.sh ] && sh mount-all-gluster-pvcs.sh ${NAMESPACE}
[ $storageclass = "csi-cephfs" ]        && [ -f mount-all-ceph-pvcs.sh ]    && sh mount-all-ceph-pvcs.sh ${NAMESPACE}
