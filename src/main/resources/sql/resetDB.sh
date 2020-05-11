#bin/bash
echo "Reset DB"
awk 'FNR==1{print "--------------------"}1' \
  ./migrations/*.down.sql \
  ./migrations/*.up.sql \
  ./functions/*/*.sql > ./allSQL.sql
docker exec -i postgresql_dc-project psql dc-project dc-project -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
rm ./allSQL.sql
echo "End reset"