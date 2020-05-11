#!/bin/bash

PS3='Please enter your choice: '
options=("RESET DB" "All" "article" "citizen" "comment" "constitution" "follow" "opinion" "user" "vote" "workgroup" "Quit")
select opt in "${options[@]}"
do
    case $opt in
        "RESET DB")
            cat \
              ../../main/resources/sql/migrations/*.down.sql \
              ../../main/resources/sql/migrations/*.up.sql > ./allSQL.sql
            docker exec -i postgresql_dc-project psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
            rm ./allSQL.sql
            break;;
        "All")
            cat ../../main/resources/sql/functions/*/*.sql \
              ./fixtures/*.sql \
              ./*.sql > ./allSQL.sql
            docker exec -i postgresql_dc-project psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
            rm ./allSQL.sql
            break;;
        "Quit")
            break;;
        *) echo "Start tests $opt"
            cat ../../main/resources/sql/functions/*/*.sql \
              ./fixtures/*.sql \
              ./"$opt".sql > ./allSQL.sql
            docker exec -i postgresql_dc-project psql test test -q -b -v "ON_ERROR_STOP=1" < ./allSQL.sql
            rm ./allSQL.sql
            break;;
    esac
done

