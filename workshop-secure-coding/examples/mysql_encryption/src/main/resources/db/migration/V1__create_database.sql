CREATE TABLE customer (
    id          INT AUTO_INCREMENT NOT NULL,
    guid        VARCHAR(40) NOT NULL,
    name        VARCHAR(128) NOT NULL,
    ssn         VARCHAR(50),
    address     VARCHAR(128),
    postal_code VARCHAR(10),
    PRIMARY KEY (id)
);
