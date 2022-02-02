CREATE TABLE author
(
    id   bigint NOT NULL,
    name text   NOT NULL,
    CONSTRAINT author_pkey PRIMARY KEY (id)
);

CREATE TABLE book
(
    isbn        text   NOT NULL,
    title       text   NOT NULL,
    author_id   bigint NOT NULL,
    description text,
    CONSTRAINT book_pkey PRIMARY KEY (isbn),
    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id)
);

-- REPLICA IDENTITY is a PostgreSQL specific table-level setting which determines the amount of information
-- that is available to logical decoding in case of UPDATE and DELETE events.
-- To show the previous values of all the table’s columns, please set the REPLICA IDENTITY level to FULL:
ALTER TABLE author
    REPLICA IDENTITY FULL;

ALTER TABLE book
    REPLICA IDENTITY FULL;

INSERT INTO author
values (1, 'Bill Bryson');

INSERT INTO book
values ('0-7679-0817-1', 'A Short History of Nearly Everything', 1,
        'A popular science book that explains some areas of science, using easily accessible language that appeals more to the general public than many other books dedicated to the subject.');

INSERT INTO book
values ('0-7679-0251-3', 'A Walk in the Woods: Rediscovering America on the Appalachian Trail', 1,
        'A 1997 travel book by the writer Bill Bryson, chronicling his attempt to thru-hike the Appalachian Trail during the spring and summer of 1996');