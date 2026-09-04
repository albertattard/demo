CREATE TABLE user_account (
    id       BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled  BOOLEAN NOT NULL
);

CREATE TABLE customer_order (
    id          BIGINT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    owner_id    BIGINT NOT NULL,
    CONSTRAINT customer_order_owner_fk FOREIGN KEY (owner_id) REFERENCES user_account (id)
);
