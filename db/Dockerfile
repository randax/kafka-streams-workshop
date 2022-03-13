FROM debezium/postgres:13

COPY dump.pgdata /docker-entrypoint-initdb.d/
COPY init.sh /docker-entrypoint-initdb.d/
