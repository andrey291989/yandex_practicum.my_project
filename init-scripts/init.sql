CREATE TABLE IF NOT EXISTS items (
                                     id BIGSERIAL PRIMARY KEY,
                                     title VARCHAR(200) NOT NULL,
    description TEXT,
    img_path VARCHAR(500),
    price BIGINT NOT NULL,
    count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_items_title ON items(title);
CREATE INDEX idx_items_price ON items(price);

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      total_sum BIGINT NOT NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGSERIAL PRIMARY KEY,
                                           order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    img_path VARCHAR(500),
    price BIGINT NOT NULL,
    count INTEGER NOT NULL
    );

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Insert sample data
INSERT INTO items (title, description, img_path, price, count) VALUES
                                                                   ('Беспроводная мышь', 'Высокоточная беспроводная мышь с эргономичным дизайном', 'images/mouse.jpg', 2999, 0),
                                                                   ('Механическая клавиатура', 'RGB механическая клавиатура с синими переключателями', 'images/keyboard.jpg', 5999, 0),
                                                                   ('Игровой монитор', '27-дюймовый игровой монитор 144Hz', 'images/monitor.jpg', 24999, 0),
                                                                   ('USB-C Хаб', '7-в-1 USB-C хаб с HDMI и Ethernet', 'images/hub.jpg', 3999, 0),
                                                                   ('HD Веб-камера', 'Веб-камера 1080p HD со встроенным микрофоном', 'images/webcam.jpg', 4999, 0),
                                                                   ('Подставка для ноутбука', 'Регулируемая алюминиевая подставка для ноутбука', 'images/stand.jpg', 2499, 0),
                                                                   ('Наушники с шумоподавлением', 'Bluetooth наушники с активным шумоподавлением', 'images/headphones.jpg', 8999, 0),
                                                                   ('Внешний SSD', 'Портативный внешний SSD на 1TB', 'images/ssd.jpg', 12999, 0),
                                                                   ('Умные часы', 'Фитнес-трекер с монитором сердечного ритма', 'images/watch.jpg', 15999, 0);