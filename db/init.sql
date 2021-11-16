CREATE TABLE IF NOT EXISTS recipes (
									   id bigint NOT NULL,
									   name text NOT NULL,
									   date_created character varying(255) COLLATE pg_catalog."default",
									   CONSTRAINT recipes_pkey PRIMARY KEY (id)
);


INSERT INTO recipes values (1,'Spaghetti bolognese','2021-01-21');
INSERT INTO recipes values (2,'Fårikål','2021-09-22');
INSERT INTO recipes values (3,'Bakalao','1999-05-05');

-- before is an optional field that if present contains the state of the row before the event occurred.
-- Whether or not this field is available is highly dependent on the REPLICA IDENTITY setting for each table.
-- REPLICA IDENTITY is a PostgreSQL specific table-level setting which determines the amount of information
-- that is available to logical decoding in case of UPDATE and DELETE events.
-- To show the previous values of all the table’s columns, please set the REPLICA IDENTITY level to FULL:
ALTER TABLE recipes REPLICA IDENTITY FULL;
