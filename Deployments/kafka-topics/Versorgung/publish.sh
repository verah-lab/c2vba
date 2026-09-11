KAFKA_BOOTSTRAP=`oc get secret application-environment -n komodnext --no-headers -o custom-columns=:.data.KAFKA_BOOTSTRAP | base64 -d`
for i in *.yaml
do
  /root/bin/kafkacli --brokers $KAFKA_BOOTSTRAP --protobuf vmis2proto.json publish $i
done
