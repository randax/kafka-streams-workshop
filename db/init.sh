#!/bin/bash

file="/docker-entrypoint-initdb.d/dump.pgdata"

echo "Restoring DB using $file"
psql -U admin -d audiobooks --single-transaction < "$file" || exit 1
