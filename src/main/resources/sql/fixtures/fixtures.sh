#bin/bash
echo "Start fixtures"
cat ./*.sql > ./allSQL.sql
docker exec -i postgresql_dc-project psql dc-project dc-project -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
rm ./allSQL.sql
echo "End fixtures"
#sleep 20