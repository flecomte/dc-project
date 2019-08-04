#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
create extension if not exists plpgsql;
create extension if not exists "uuid-ossp";
create extension if not exists pg_trgm;
create extension if not exists pg_stat_statements;
create extension if not exists zombodb;
create extension if not exists pgcrypto;

create database test;
create user test with encrypted password 'test';
grant all privileges on database test to test;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "test" <<-EOSQL
create extension if not exists plpgsql;
create extension if not exists "uuid-ossp";
create extension if not exists pg_trgm;
create extension if not exists zombodb;
create extension if not exists pgcrypto;
alter schema public owner to test;
EOSQL
