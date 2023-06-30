# UM-Specialists Shared VDS Projects

# ATTENTION!!!!!

The code in this repo is presented as example code. It is not supported product from Informatica. It is not being maintained.

Note that it is dependent on many third-party packages that have serious vulnerabilities in various versions.
Informatica cannot be held responsible for use of this code that results in security flaws.
If you use this code, its continued maintenance is your responsibility.

## Introduction

This was previously a private repository, creating by Paul Young whilst working at Informatica. This repository is now publicly available.

To initialize your copy of the repository, go to a folder set aside to clone these contents and run the following commands:<br>
<pre>
git init
git remote add origin https://github.com/phy2000/VDS-2.3
git pull origin your-working-branch # If you have one
git checkout your-working-branch 
</pre>

# It is known that git clients below version 1.8 will not work<br>
Check version with:<br>
<pre>git --version</pre>
If it is 1.7x or lower, please update. Check here for packages (Except Redhat, CentOS):<br>
https://help.github.com/articles/set-up-git/#platform-all
# Instructions for Centos and Redhat
The git client has to be built for these platforms. Do the following in an empty folder:<br>
<pre>
git clone https://github.com/git/git
# install missing packages
sudo yum install curl-devel expat-devel gettext-devel openssl-devel zlib-devel gcc perl-ExtUtils-MakeMaker
cd git
make prefix=/usr/local/git all
sudo make prefix=/usr/local/git install
# Update PATH
echo "export PATH=$PATH:/usr/local/git/bin" >> /etc/bashrc 
source /etc/bashrc
# Remove old git:
sudo yum remove git
</pre>
Most instructions from : <br>
http://tecadmin.net/install-git-2-0-on-centos-rhel-fedora/<br>

