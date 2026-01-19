--liquibase formatted sql

-- changeset IlliaFransua:01-insert-initial-burgers
INSERT INTO burgers (id, name, unit_price)
VALUES (nextval('burger_id_sequence'), 'Classic Cheeseburger', 10.99),
       (nextval('burger_id_sequence'), 'Baconator Deluxe', 12.49),
       (nextval('burger_id_sequence'), 'Spicy Jalapeño', 11.99),
       (nextval('burger_id_sequence'), 'Mushroom Swiss Bliss', 13.50),
       (nextval('burger_id_sequence'), 'Veggie Garden Fresh', 9.99),
       (nextval('burger_id_sequence'), 'BBQ Smokehouse King', 14.99),
       (nextval('burger_id_sequence'), 'Double Stack Attack', 16.00),
       (nextval('burger_id_sequence'), 'Hawaiian Sunset (Pineapple)', 12.00),
       (nextval('burger_id_sequence'), 'Blue Cheese Beast', 15.25),
       (nextval('burger_id_sequence'), 'California Avocado Club', 13.99),
       (nextval('burger_id_sequence'), 'Truffle Oil Gourmet', 18.50),
       (nextval('burger_id_sequence'), 'Breakfast Egg Burger', 11.50),
       (nextval('burger_id_sequence'), 'Texas Chili Fire', 14.00),
       (nextval('burger_id_sequence'), 'Teriyaki Glaze Special', 12.99),
       (nextval('burger_id_sequence'), 'Monster Mac Cheese Burger', 15.99);
