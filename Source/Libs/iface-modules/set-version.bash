#####################################
# set a new version for all modules 
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

# get and check the version number
VERSION=$1
if [[ -z "$VERSION" ]]
then
  echo
  echo "please enter the new version"
  read v
  VERSION=$v
fi
if [[ -z "$VERSION" ]]
then
  exit
fi

# change the version for all modules using maven
mvn versions:set -DnewVersion=$VERSION

# run maven install
mvn install

# commit and push to git
echo 
echo "Do you want to commit and push your changes?"
echo "then type y or a commit message"
read answer
if [[ -z "$answer" ]]
then
  exit
fi
msg=$answer
if [[ "$msg" = "y" ]]
then 
  msg="set modules to version $VERSION"
fi
git add .
git commit -m "$msg"
git push
