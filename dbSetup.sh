# To use that file first give it runnable permission by the following command
# chmod +x dbSetup.sh
# Then you can run it by the following;
# ./dbSetup.sh

# This file is created by Mehmet Furkan Sahin

#!/bin/bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
su -c "createdb blockchain" -m "postgres"
#sudo -u postgres createdb blockchain
su -c "psql postgres -c \"DROP TABLE IF EXISTS blockchain;\"" -m "postgres"
su -c "psql postgres -c \"DROP TABLE IF EXISTS blocks;\"" -m "postgres"
su -c "psql postgres -c \"CREATE TABLE blockchain (hash TEXT UNIQUE PRIMARY KEY NOT NULL, data TEXT);\"" -m "postgres"
su -c "psql postgres -c \"alter user postgres password '';\"" -m "postgres"
#psql postgres -c "CREATE TABLE blockchain (hash CHAR(256) UNIQUE PRIMARY KEY NOT NULL, data TEXT);"
