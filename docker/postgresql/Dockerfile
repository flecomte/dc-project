FROM postgres:11

# ZomboDB
RUN apt-get update && apt-get -y install wget libcurl4-openssl-dev
COPY zombodb_stretch_pg11-10-1.0.3_amd64.deb /tmp/zombodb_stretch_pg11-10-1.0.3_amd64.deb
RUN dpkg -i /tmp/zombodb_stretch_pg11-10-1.0.3_amd64.deb && \
  rm -f /tmp/zombodb_stretch_pg11-10-1.0.3_amd64.deb


COPY postgresql.conf /tmp/postgresql.conf
COPY extension.sh    /docker-entrypoint-initdb.d/000-extension.sh
COPY setup.sh        /docker-entrypoint-initdb.d/100-setup.sh

ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 5432
CMD ["postgres"]
