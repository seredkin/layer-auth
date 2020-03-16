#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 <<-EOSQL
    CREATE user sharing_msa with encrypted password 'sharing_msa';
    ALTER ROLE sharing_msa WITH LOGIN;
    ALTER ROLE sharing_msa with SUPERUSER;
    CREATE DATABASE sharing_msa owner sharing_msa;
EOSQL
