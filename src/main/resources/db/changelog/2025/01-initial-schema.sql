--liquibase formatted sql

-- changeset IlliaFransua:01-create-sequences
CREATE SEQUENCE burger_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE orders_seq START WITH 1 INCREMENT BY 50;

-- changeset IlliaFransua:02-create-tables
CREATE TABLE burger
(
    id         BIGINT         NOT NULL,
    name       VARCHAR(255)   NOT NULL,
    unit_price NUMERIC(38, 2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id         BIGINT                      NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

-- changeset IlliaFransua:03-create-join-table
CREATE TABLE orders_burgers
(
    burgers_id BIGINT NOT NULL,
    order_id   BIGINT NOT NULL,
    PRIMARY KEY (order_id, burgers_id)
);

-- changeset IlliaFransua:04-add-foreign-keys
ALTER TABLE orders_burgers
    ADD CONSTRAINT FKrfleg1ccyj0ldqu1huv9pojht
        FOREIGN KEY (burgers_id)
            REFERENCES burger (id);

ALTER TABLE orders_burgers
    ADD CONSTRAINT FKnil3e41suy7j6d2ckjort0yqx
        FOREIGN KEY (order_id)
            REFERENCES orders (id);