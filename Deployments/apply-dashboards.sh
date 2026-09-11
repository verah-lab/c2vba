#!/bin/bash
P=$(pwd)/`dirname $0`
export P

if [ $# -gt 0 ]
then
	SERVER=$1
fi

if [ -z "$SERVER" ]; then
    echo Bitte SERVER Variable setzen oder als ersten Aufrufparameter übergeben
    echo "e.g. http://admin:<password>@grafana.monitoring.svc"
		echo "Passwort ist auf den Kundensystemene geheim, deshalb ist die SERVER Variable nicht vor-versorgt"
    exit 1
fi
chmod 755 $P/dashboard-utils/jq-linux64

## Gets or creates the given dashboard folder
## @param $1 - The name of the Folder
## @return $folderId - The id of the folder
function createDashboardFolder() {
	if [ "$1" == "General" ]; then
		folderId="0"
		return;
	fi
	printf 'Checking folder %s\n' "$1"
	# Detect Folder Id
	folderId=$(curl -s "$SERVER/api/folders" -H "Content-Type: application/json;charset=utf-8" | \
		$P/dashboard-utils/jq-linux64 --arg title $1 '.[] | select(.title == $title) | .id')
	echo folderId = $folderId
	if [ -z "$folderId" ]
	then
		# Does not exist, create it
		printf 'Missing -> Creating folder\n'
		echo "{}" | $P/dashboard-utils/jq-linux64 --arg title $1 '.title = $title' \
			> json.request
		folderId=$(curl -s "$SERVER/api/folders" \
			-H "Content-Type: application/json;charset=utf-8" \
			--data-binary "@json.request" | \
			$P/dashboard-utils/jq-linux64 '.id')
		printf 'Done\n\n'
		rm json.request
		sleep 1s
	fi
}

## Imports the given dashboard
## @param $1 The file to import
## @param $2 The folderId to import to
function importDashboardFile() {
	printf 'Importing %s\n' "$1"

	# Actual Import
	$P/dashboard-utils/jq-linux64 --argfile dashboard $1 '.dashboard = $dashboard' $P/dashboard-utils/import-query-template.json | \
		$P/dashboard-utils/jq-linux64 '.dashboard.id = null' | \
		$P/dashboard-utils/jq-linux64 --argjson folder $2 '.folderId = $folder' \
		> json.request
	curl -s "$SERVER/api/dashboards/import" \
		-H "Content-Type: application/json;charset=utf-8" \
		--data-binary "@json.request" > /dev/null

	printf 'Done\n\n'
	rm json.request
	sleep 1s
}

## Imports all dashboards in the given folder
## @param $1 - The name of the folder to import
function importDashboardFolder() {
	createDashboardFolder $1
	for file in $1/*.json
	do
	importDashboardFile $file $folderId
	done
}

## Actually import the folders

cd $P/dashboards

for i in *
do
	[ -d $i ] && importDashboardFolder $i
done

echo "Dashboards mit Alerts muessen zusaetzlich noch manuell gesichert werden"
echo "https://github.com/grafana/grafana/issues/11419"
echo ""
echo "Sind die Datasource schon konfiguriert?"
