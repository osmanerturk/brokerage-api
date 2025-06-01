INSERT INTO "ASSETS" (customer_id, asset_name, size, usable_size) VALUES
                                                                      ('customer_1', 'TRY', 10000, 9000),
                                                                      ('customer_2', 'TRY', 9000, 8500),
                                                                      ('customer_3', 'TRY', 12000, 10000);

INSERT INTO "ORDERS" (customer_id, asset_name, order_side, size, price, status, create_date) VALUES
                                                                                                     ('customer_1', 'TRY', 'BUY', 1000, 15.0, 'PENDING', '2025-05-29 10:00:00'),
                                                                                                     ('customer_1', 'TRY', 'SELL', 500, 16.0, 'MATCHED', '2025-05-28 09:00:00'),
                                                                                                     ('customer_2', 'TRY', 'BUY', 1500, 14.0, 'PENDING', '2025-05-29 11:00:00'),
                                                                                                     ('customer_3', 'TRY', 'SELL', 700, 15.5, 'PENDING', '2025-05-29 12:00:00'),
                                                                                                     ('customer_2', 'TRY', 'SELL', 300, 15.0, 'CANCELED', '2025-05-28 15:00:00');


INSERT INTO "CUSTOMERS" (username, password,customer_id, role) VALUES ('admin', '{noop}admin','admin','ROLE_ADMIN');
INSERT INTO "CUSTOMERS" (username, password,customer_id, role) VALUES ('alice', '{noop}alicepass','customer_1','ROLE_USER');
INSERT INTO "CUSTOMERS" (username, password,customer_id, role) VALUES ('bob', '{noop}bobpass','customer_2','ROLE_USER');
INSERT INTO "CUSTOMERS" (username, password,customer_id, role) VALUES ('ing', '{noop}lion','customer_3','ROLE_USER');
