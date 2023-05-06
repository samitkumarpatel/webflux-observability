CREATE TABLE IF NOT EXISTS person
(
    id SERIAL PRIMARY KEY,
    name varchar(255) NOT NULL,
    age int NOT NULL
);