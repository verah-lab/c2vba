#!/bin/bash
kubectl get secret rook-ceph-admin-keyring -n rook-ceph >/dev/null 2>&1 || exit "no ceph"
CEPH_SECRET=$(kubectl get secret rook-ceph-admin-keyring -n rook-ceph --no-headers -o custom-columns=:.data.keyring | base64 -d | grep "key " | sed "s/.* = //")
CEPH_ENDPOINTS=$(kubectl get configmap rook-ceph-mon-endpoints -n rook-ceph --no-headers -o custom-columns=:.data.data | sed 's/[a-z0-9_-]\+=//g')
CEPH_MNT=/mnt/cephfs
mkdir -p $CEPH_MNT >/dev/null 2>&1
mount -t ceph -o mds_namespace=myfs,name=admin,secret=$CEPH_SECRET $CEPH_ENDPOINTS:/ $CEPH_MNT

kubectl get pvc --all-namespaces --no-headers -o custom-columns=:.spec.volumeName,:.metadata.namespace,:metadata.name | \
while read PVC NAMESPACE PVC_NAME
do
   SUBVOLUMEPATH=$(kubectl get pv $PVC -n $NAMESPACE --no-headers -o custom-columns=:.spec.csi.volumeAttributes.subvolumePath)
   PVC_MNT=/mnt/pvc/$NAMESPACE/$PVC_NAME
   mkdir -p $PVC_MNT >/dev/null 2>&1
   mount --bind  $CEPH_MNT$SUBVOLUMEPATH $PVC_MNT
done
