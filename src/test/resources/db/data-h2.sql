-- Тестовые данные для H2

-- Добавляем тестовые товары
INSERT INTO items (title, description, img_path, price, count) VALUES
('Тестовый товар 1', 'Описание тестового товара 1', 'test1.jpg', 1000, 10),
('Тестовый товар 2', 'Описание тестового товара 2', 'test2.jpg', 2000, 5),
('Another item', 'Different description', 'test3.jpg', 1500, 20);

-- Добавляем тестовый заказ
INSERT INTO orders (total_sum) VALUES (3000);

-- Добавляем товары в заказ
INSERT INTO order_items (order_id, title, description, img_path, price, count) VALUES
(1, 'Тестовый товар 1', 'Описание тестового товара 1', 'test1.jpg', 1000, 2),
(1, 'Тестовый товар 2', 'Описание тестового товара 2', 'test2.jpg', 2000, 1);

-- Добавляем тестовый платеж
INSERT INTO payments (order_id, user_id, amount, status) VALUES (1, 123, 3000, 'COMPLETED');