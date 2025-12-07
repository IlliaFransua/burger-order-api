--liquibase formatted sql

-- changeset IlliaFransua:01-insert-initial-burgers
INSERT INTO burger (id, name, unit_price)
VALUES (nextval('burger_seq'), 'Classic Cheeseburger', 10.99),
       (nextval('burger_seq'), 'Baconator Deluxe', 12.49),
       (nextval('burger_seq'), 'Spicy Jalapeño', 11.99),
       (nextval('burger_seq'), 'Mushroom Swiss', 13.99),
       (nextval('burger_seq'), 'Veggie Delight', 9.99),
       (nextval('burger_seq'), 'BBQ Ranch', 12.99),
       (nextval('burger_seq'), 'Double Stack', 15.99),
       (nextval('burger_seq'), 'Hawaiian Pineapple', 11.49),
       (nextval('burger_seq'), 'Blue Cheese Beast', 14.99),
       (nextval('burger_seq'), 'California Avocado', 13.49);