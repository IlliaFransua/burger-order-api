--liquibase formatted sql

-- changeset IlliaFransua:01-create-sequences
CREATE SEQUENCE burger_id_sequence START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE order_id_sequence START WITH 1 INCREMENT BY 1;

-- changeset IlliaFransua:02-create-tables
CREATE TABLE burgers
(
    id         BIGINT         NOT NULL,
    name       VARCHAR(255)   NOT NULL,
    unit_price NUMERIC(38, 2) NOT NULL,
    CONSTRAINT pk_burgers PRIMARY KEY (id)
);

CREATE TABLE orders
(
    id         BIGINT                      NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

-- changeset IlliaFransua:03-create-join-table
CREATE TABLE order_burgers
(
    burger_id BIGINT NOT NULL,
    order_id  BIGINT NOT NULL
);

-- changeset IlliaFransua:04-add-foreign-keys
ALTER TABLE order_burgers
    ADD CONSTRAINT fk_order_burgers_burger FOREIGN KEY (burger_id) REFERENCES burgers (id);

ALTER TABLE order_burgers
    ADD CONSTRAINT fk_order_burgers_order FOREIGN KEY (order_id) REFERENCES orders (id);
