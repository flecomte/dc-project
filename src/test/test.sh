#bin/bash
echo "Start tests"
cat ../main/resources/sql/functions/*/*.sql ../main/resources/sql/migrations/*.sql ./sql/fixtures/*.sql ./sql/*.sql > ./allSQL.sql
docker exec -i postgresql_dc-project psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
rm ./allSQL.sql
#sleep 20