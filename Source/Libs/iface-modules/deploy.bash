#####################################
# deploy to nexus, git and tag the version
# 
# this script requires a parent pom
# in the current directory
#####################################

# first cleanup repository
if [[ ! -z $(git status -s) ]]
then
  git status
  echo
  echo "There are uncommitted changes. If you want to commit all,"
  echo "enter a commit message. Otherwise the process will abort"
  read msg

  if [[ ! -z "$msg" ]]
  then
    git add .
	git commit -m "$msg"
  else
    exit
  fi
fi

git push

if [[ -z "$msg" ]]
then
  echo "Please enter a message for tagging"
  read msg
fi
if [[ -z "$msg" ]]
then 
  exit
fi

version=$(grep version pom.xml|head -1|sed 's#.*<version>##'|sed 's#</version>.*##')
snapshot=$(echo $version|grep SNAPSHOT)
if [[ -z "$snapshot" ]]
then
	git tag -m "$msg" IFACE-MODULES-$version
	git push --tags 
fi
mvn deploy
