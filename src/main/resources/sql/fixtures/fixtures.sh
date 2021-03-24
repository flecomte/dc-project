#!/usr/bin/env bash
awk 'FNR==1{print "--------------------"}1' `dirname $0`/*.sql > ./allSQL.sql
docker exec -i dc-project_postgresql psql dc-project dc-project -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
rm ./allSQL.sql
echo "End fixtures"