#!/bin/bash
#
set_domain() {
  SYSTEM_STAGE=$(kubectl get secret application-environment -n $NAMESPACE -o custom-columns=:.data.SYSTEM_STAGE | base64 -d )
#  [ $SYSTEM_STAGE = "s-vz-lev-hbokd" ] && domain="vrz.verkehrszentrale.nrw"
#  [ $SYSTEM_STAGE = "hb" ] && domain="apps.d.uz.komodnext.heuboe.hbintern"
#  [ $SYSTEM_STAGE = "rancher" ] && domain="apps.test.heuboe.hbintern"
}
#
check_generatedBy() {
  generatedBy="__"$(kubectl get ingress $ingressname -n $NAMESPACE -o custom-columns=.:metadata.annotations.generatedBy --no-headers 2>/dev/null )"__"
  if [ $generatedBy = "____" ] || [ $generatedBy = "__create-ingress.sh__" ]
  then
     echo ""
  else
    echo "# Route $thisroute existiert und ist nicht per create-route.sh erzeugt; es wird ${thisroute}-g erzeugt" >> deploy-routes.sh
    echo "-g"
  fi
}
#
write_ingress() {
  echo -n "   -> erzeuge" $ingressname.yaml
  echo "apiVersion: networking.k8s.io/v1 " >$ingressname.yaml
  echo "kind: Ingress" >>$ingressname.yaml
  echo "metadata:" >>$ingressname.yaml
  echo "  annotations:" >>$ingressname.yaml
  echo "    generatedBy: create-ingress.sh" >>$ingressname.yaml
  if [ "$n" = "grpc-ssl" ] || [ "$t" = "grpc-ssl" ]
  then
	echo -n " fuer GRPCS"
    echo "    nginx.ingress.kubernetes.io/backend-protocol: GRPCS" >>$ingressname.yaml
    echo "    nginx.ingress.kubernetes.io/ssl-redirect: \"true\"" >>$ingressname.yaml
  fi
  echo "  name: $ingressname$g" >>$ingressname.yaml
  echo "spec:" >>$ingressname.yaml
  echo "  rules:" >>$ingressname.yaml
  echo "  - host: $ingressname.$domain" >>$ingressname.yaml
  echo "    http:" >>$ingressname.yaml
  echo "      paths:" >>$ingressname.yaml
  echo "      - backend:" >>$ingressname.yaml
  echo "          service:" >>$ingressname.yaml
  echo "            name: $s" >>$ingressname.yaml
  echo "            port:" >>$ingressname.yaml
  echo "              name: $n" >>$ingressname.yaml
  echo "        path: /" >>$ingressname.yaml
  echo "        pathType: Prefix" >>$ingressname.yaml
  if [[ "$ingressname" = *-ssl ]]  || [ "$n" = "grpc-ssl" ] || [ "$t" = "grpc-ssl" ]
    then
	echo -n " mit TLS Secret"
    echo "  tls:" >>$ingressname.yaml
    echo "  - hosts:" >>$ingressname.yaml
    echo "    - $ingressname.$domain" >>$ingressname.yaml
    echo "    secretName: c2vba-tls-secret" >>$ingressname.yaml
  fi
  echo ""
}
#
if   [ $# -gt 0 ];          then NAMESPACE=$1; export NAMESPACE  # das apply.sh wird mit (wenigstens) einem Aufrufparameter aufgerufen, der erste wird als Namespace genommen
elif [ -f ./namespace.sh ]; then . ./namespace.sh                # die namespace.sh Datei existiert, das Skript enthält das export Kommando für NAMESPACE
else                        NAMESPACE=c2vba;   export NAMESPACE  # fallback
fi

set_domain

[ ! -d ingress-definitions ] && mkdir ingress-definitions
cd ingress-definitions
rm -f *.yaml
echo "#!/bin/bash" > deploy-ingress.sh
echo "#" >> deploy-ingress.sh
set -e
#kubectl get secret asfinag-tls-secret -o custom-columns=":.data.tls\.crt" --no-headers | base64 -d > crt.pem
#kubectl get secret asfinag-tls-secret -o custom-columns=":.data.tls\.key" --no-headers | base64 -d > key.pem
set +e
kubectl get services -n $NAMESPACE --no-headers -o custom-columns=.:.metadata.name | grep -v via | while read s
do
  d=$(kubectl get services -n $NAMESPACE $s --no-headers -o custom-columns=.:spec.selector.app)
  if $(kubectl get deployment -n $NAMESPACE $d --no-headers 2>/dev/null >/dev/null)
  then
    i=0
    while $(kubectl get services -n $NAMESPACE $s --no-headers -o custom-columns=.:.spec.ports[$i].name 2>/dev/null >/dev/null)
    do
      p=$(kubectl get services -n $NAMESPACE $s --no-headers -o custom-columns=.:.spec.ports[$i].port)
      t=$(kubectl get services -n $NAMESPACE $s --no-headers -o custom-columns=.:.spec.ports[$i].targetPort)
      n=$(kubectl get services -n $NAMESPACE $s --no-headers -o custom-columns=.:.spec.ports[$i].name)
      echo "$s-$n.$domain" $p $t
#
      if [ "$n" != "<none>" ]; then

        ingressname=$s-$n
        g="$(check_generatedBy)"

        write_ingress
        echo "kubectl apply -f $ingressname.yaml -n $NAMESPACE # generatedBy=$generatedBy" >> deploy-ingress.sh
      fi
#
      if [ "$n" = "actuator" ] || [ "$n" = "map-proxy" ]  || [ "$n" = "web" ] ; then

        ingressname=$s-$n-ssl
        g="$(check_generatedBy)"

        write_ingress
        echo "kubectl apply -f $ingressname.yaml -n $NAMESPACE # generatedBy=$generatedBy" >> deploy-ingress.sh
      fi
      echo "======================================================================================="
      i=$(($i+1))
    done
  else
    echo "# Kein Deployment $d zum Service $s, also auch keine Routen" >> deploy-ingress.sh
  fi
done
