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

-- Insert sample data with stock counts
INSERT INTO items (title, description, img_path, price, count) VALUES
                                                                   ('Беспроводная мышь', 'Высокоточная беспроводная мышь с эргономичным дизайном', 'mouse.jpg', 2999, 50),
                                                                   ('Механическая клавиатура', 'RGB механическая клавиатура с синими переключателями', 'keyboard.jpg', 5999, 30),
                                                                   ('Игровой монитор', '27-дюймовый игровой монитор 144Hz', 'monitor.jpg', 24999, 10),
                                                                   ('USB-C Хаб', '7-в-1 USB-C хаб с HDMI и Ethernet', 'hub.jpg', 3999, 100),
                                                                   ('HD Веб-камера', 'Веб-камера 1080p HD со встроенным микрофоном', 'webcam.jpg', 4999, 25),
                                                                   ('Подставка для ноутбука', 'Регулируемая алюминиевая подставка для ноутбука', 'stand.jpg', 2499, 200),
                                                                   ('Наушники с шумоподавлением', 'Bluetooth наушники с активным шумоподавлением', 'headphones.jpg', 8999, 15),
                                                                   ('Внешний SSD', 'Портативный внешний SSD на 1TB', 'ssd.jpg', 12999, 20),
                                                                   ('Умные часы', 'Фитнес-трекер с монитором сердечного ритма', 'watch.jpg', 15999, 5);