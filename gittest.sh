#!/bin/bash -x
GITMAIL=roderick.clemente@gmail.com
GITNAME="roderick-clemente"
GITBRANCH=rac-working-branch

git init
git --version
git config credential.helper store
git remote add origin https://github.com/phy2000/VDS-2.3
git pull origin $GITBRANCH
git checkout $GITBRANCH
git status
touch $GITBRANCH-file
git add $GITBRANCH-file 
git config user.email $GITMAIL
git config user.name $GITNAME
git commit -m $GITBRANCH-file 
git push
git rm $GITBRANCH-file
git commit -m "Remove $GITBRANCH-file"
git push
