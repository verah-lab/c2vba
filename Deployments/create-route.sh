#!/bin/bash

#!/bin/bash

if   [ $# -gt 0 ];          then NAMESPACE=$1; export NAMESPACE  # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ./namespace.sh ]; then . ./namespace.sh                # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                        NAMESPACE=c2vba;   export NAMESPACE  # fallback
fi

#ggfls die Routen-Domaine anpassen
SYSTEM_STAGE=$(kubectl get secret application-environment -n $NAMESPACE -o custom-columns=:.data.SYSTEM_STAGE | base64 -d )
#[ $SYSTEM_STAGE = "s-vz-lev-hbokd" ] && domain="vrz.verkehrszentrale.nrw"
#[ $SYSTEM_STAGE = "hb" ] && domain="apps.d.uz.komodnext.heuboe.hbintern"
#[ $SYSTEM_STAGE = "rancher" ] && domain="apps.test.heuboe.hbintern"

[ ! -d route-definitions ] && mkdir route-definitions
cd route-definitions
rm -f *.yaml
echo "#!/bin/bash" > deploy-routes.sh
echo "#" >> deploy-routes.sh
set -e
#kubectl get secret asfinag-tls-secret -o custom-columns=":.data.tls\.crt" --no-headers | base64 -d > crt.pem
#kubectl get secret asfinag-tls-secret -o custom-columns=":.data.tls\.key" --no-headers | base64 -d > key.pem
set +e
kubectl get services -n $NAMESPACE --no-headers -o custom-columns=.:.metadata.name | grep -v via | while read s
do
  d=$(kubectl get services -n $NAMESPACE  --export=true $s --no-headers -o custom-columns=.:spec.selector.app)
  if $(kubectl get deployment -n $NAMESPACE $d --no-headers 2>/dev/null >/dev/null)
  then
    i=0
    while $(kubectl get services -n $NAMESPACE  --export=true $s --no-headers -o custom-columns=.:.spec.ports[$i].name 2>/dev/null >/dev/null)
    do
      p=$(kubectl get services -n $NAMESPACE  --export=true $s --no-headers -o custom-columns=.:.spec.ports[$i].port)
      t=$(kubectl get services -n $NAMESPACE  --export=true $s --no-headers -o custom-columns=.:.spec.ports[$i].targetPort)
      n=$(kubectl get services -n $NAMESPACE  --export=true $s --no-headers -o custom-columns=.:.spec.ports[$i].name)
      echo "$s-$n.$domain" $p $t
      if [ "$n" != "<none>" ]; then
        generatedBy="__"$(kubectl get route $s-$n -n $NAMESPACE -o custom-columns=.:metadata.annotations.generatedBy --no-headers 2>/dev/null )"__"
        if [ $generatedBy = "____" ] || [ $generatedBy = "__create-route.sh__" ]
        then
          g=""
        else
          echo "# Route $s-$n existiert und ist nicht per create-route.sh erzeugt; es wird $s-$n$g erzeugt" >> deploy-routes.sh
          g="-g"
        fi
        echo "apiVersion: route.openshift.io/v1" >$s-$n.yaml
        echo "kind: Route" >>$s-$n.yaml
        echo "metadata:" >>$s-$n.yaml
        echo "  name: $s-$n$g" >>$s-$n.yaml
        echo "  annotations:" >>$s-$n.yaml
        echo "    generatedBy: create-route.sh" >>$s-$n.yaml
        echo "spec:" >>$s-$n.yaml
        echo "  host: $s-$n.$domain" >>$s-$n.yaml
        #if [ "$n" != "grpc-secure" ] &&  [ "$n" != "grpc-ssl" ] ; then
        if [[ "$n" != *ssl ]] ; then
          echo "  path: /" >>$s-$n.yaml
        fi
        echo "  port:" >>$s-$n.yaml
        echo "    targetPort: $n" >>$s-$n.yaml
        echo "  to:" >>$s-$n.yaml
        echo "    kind: Service" >>$s-$n.yaml
        echo "    name: $s" >>$s-$n.yaml
        echo "    weight: 100" >>$s-$n.yaml
#          if [ "$n" = "grpc-secure" ] || [ "$n" = "grpc-ssl" ] ; then
        if [[ "$n" == *ssl ]] ; then
          echo "  tls:" >>$s-$n.yaml
          echo "    termination: passthrough" >>$s-$n.yaml
        fi
        echo "  wildcardPolicy: None" >>$s-$n.yaml
        echo "kubectl apply -f $s-$n.yaml -n $NAMESPACE # generatedBy=$generatedBy" >> deploy-routes.sh
      fi
#
      if [ "$n" = "actuator" ] || [ "$n" = "map-proxy" ]  || [ "$n" = "web" ] ; then
        generatedBy="__"$(kubectl get route $s-$n-ssl -n $NAMESPACE -o custom-columns=.:metadata.annotations.generatedBy --no-headers 2>/dev/null )"__"
        if [ $generatedBy = "____" ] || [ $generatedBy = "__create-route.sh__" ]
        then
          g=""
        else
          echo "# Route $s-$n-ssl existiert und ist nicht per create-route.sh erzeugt; es wird $s-$n-ssl$g erzeugt" >> deploy-routes.sh
          g="-g"
        fi
        echo "apiVersion: route.openshift.io/v1" >$s-$n-ssl.yaml
        echo "kind: Route" >>$s-$n-ssl.yaml
        echo "metadata:" >>$s-$n-ssl.yaml
        echo "  name: $s-$n-ssl$g" >>$s-$n-ssl.yaml
        echo "  annotations:" >>$s-$n-ssl.yaml
        echo "    generatedBy: create-route.sh" >>$s-$n-ssl.yaml
        echo "spec:" >>$s-$n-ssl.yaml
        if [[ "$s" = "keycloak" ]]
        then
          echo "  host: $s-ssl.$domain" >>$s-$n-ssl.yaml
        else
          echo "  host: $s-$n-ssl.$domain" >>$s-$n-ssl.yaml
        fi
        echo "  path: /" >>$s-$n-ssl.yaml
        echo "  port:" >>$s-$n-ssl.yaml
        echo "    targetPort: $n" >>$s-$n-ssl.yaml
        echo "  to:" >>$s-$n-ssl.yaml
        echo "    kind: Service" >>$s-$n-ssl.yaml
        echo "    name: $s" >>$s-$n-ssl.yaml
        echo "    weight: 100" >>$s-$n-ssl.yaml
        echo "  tls:" >>$s-$n-ssl.yaml
        echo "    termination: edge" >>$s-$n-ssl.yaml
        #echo "    key: |-" >>$s-$n-ssl.yaml
        #cat ./key.pem | sed "s/^/      /" >>$s-$n-ssl.yaml
        #echo >> $s-$n-ssl.yaml
        #echo "    certificate: |-" >>$s-$n-ssl.yaml
        #cat ./crt.pem | sed "s/^/      /" >>$s-$n-ssl.yaml
        #echo >> $s-$n-ssl.yaml
        echo "  wildcardPolicy: None" >>$s-$n-ssl.yaml
        echo "kubectl apply -f $s-$n-ssl.yaml -n $NAMESPACE # generatedBy=$generatedBy" >> deploy-routes.sh
      fi
      echo "======================================================================================="
      i=$(($i+1))
    done
  else
    echo "# Kein Deployment $d zum Service $s, also auch keine Routen" >> deploy-routes.sh
  fi
done
