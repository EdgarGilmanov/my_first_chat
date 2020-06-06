CREATE TABLE IF NOT EXISTS users
(
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    gender BOOLEAN NOT NULL,
    UNIQUE (user_name)

);