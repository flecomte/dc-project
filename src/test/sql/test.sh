#!/bin/bash

options=("All" "RESET DB" "article" "citizen" "comment" "constitution" "follow" "opinion" "user" "vote" "workgroup" "Quit")
if [ -z "$1" ]; then
  PS3='Please enter your choice: '
  select ch in "${options[@]}"
  do
    opt=$ch
    break
  done
else
  opt=${options[${1}-1]}
fi

case $opt in
  "RESET DB")
      awk 'FNR==1{print "--------------------"}1' \
        ../../main/resources/sql/migrations/*.down.sql \
        ../../main/resources/sql/migrations/*.up.sql > ./allSQL.sql
      docker exec -i dc-project_postgresql_test psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
      rm ./allSQL.sql
      ;;
  "All")
      echo "Start ALL tests"
      awk 'FNR==1{print "--------------------"}1' \
        ../../main/resources/sql/functions/*/*.sql \
        ./fixtures/*.sql \
        ./*.sql > ./allSQL.sql
      docker exec -i dc-project_postgresql_test psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
      rm ./allSQL.sql
      ;;
  "Quit")
      ;;
  *)
    echo "Start tests $opt"
    awk 'FNR==1{print "--------------------"}1' \
      ../../main/resources/sql/functions/*/*.sql \
      ./fixtures/*.sql \
      ./"$opt".sql > ./allSQL.sql
    docker exec -i dc-project_postgresql_test psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
    rm ./allSQL.sql
    ;;
esac