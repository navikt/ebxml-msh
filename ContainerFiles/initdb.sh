#!/bin/bash -ex

psql --username postgres <<-EOSQL
    CREATE USER "$DB_USER_NAME" WITH SUPERUSER PASSWORD '$DB_USER_PASS' ;
EOSQL

psql --username postgres <<-EOSQL
    CREATE DATABASE ebms ;
EOSQL

cat  /work/sql/ebms.sql | psql --username postgres ebms

