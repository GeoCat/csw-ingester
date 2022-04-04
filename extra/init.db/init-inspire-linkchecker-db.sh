#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE inspire_linkchecker;
    GRANT ALL PRIVILEGES ON DATABASE inspire_linkchecker TO postgres;
EOSQL

