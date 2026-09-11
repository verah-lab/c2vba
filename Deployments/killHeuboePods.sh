kubectl get pods -n c2vba | while read p x
do
killIt=`kubectl describe pod $p 2>/dev/null | grep -i "Image ID"  | grep heuboe`
# echo $killIt
if [ ! -z  "$killIt" ]
then 
	kubectl delete pod $p -n c2vba& 
fi
done
