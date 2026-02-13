-- ============================================================================
-- PRODUCTS AND NUTRITION FACTS TEST DATA
-- ============================================================================
-- 50 realistische Produkte mit Nährwertangaben
-- ============================================================================

-- Snacks & Süßigkeiten
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('5000159484237', 'Snickers Bar', 'Mars', 'https://images.openfoodfacts.org/images/products/500/015/948/4237/front_en.jpg'),
('40111445', 'KitKat 4 Finger', 'Nestlé', 'https://images.openfoodfacts.org/images/products/40111445/front_en.jpg'),
('7622210449283', 'Milka Alpenmilch', 'Milka', 'https://images.openfoodfacts.org/images/products/762/221/044/9283/front_de.jpg'),
('4000417025005', 'Hanuta', 'Ferrero', 'https://images.openfoodfacts.org/images/products/400/041/702/5005/front_de.jpg'),
('5449000000996', 'Coca-Cola Classic', 'Coca-Cola', 'https://images.openfoodfacts.org/images/products/544/900/000/0996/front_en.jpg'),
('5449000214911', 'Fanta Orange', 'Fanta', 'https://images.openfoodfacts.org/images/products/544/900/021/4911/front_en.jpg'),
('4008400402123', 'Prinzenrolle', 'De Beukelaer', 'https://images.openfoodfacts.org/images/products/400/840/040/2123/front_de.jpg'),
('4000400127600', 'Toffifee', 'Storck', 'https://images.openfoodfacts.org/images/products/400/040/012/7600/front_de.jpg'),
('8000500037560', 'Nutella', 'Ferrero', 'https://images.openfoodfacts.org/images/products/800/050/003/7560/front_en.jpg'),
('4014400916324', 'Haribo Goldbären', 'Haribo', 'https://images.openfoodfacts.org/images/products/401/440/091/6324/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('5000159484237', 488.0),  -- Snickers
('40111445', 518.0),       -- KitKat
('7622210449283', 530.0),  -- Milka
('4000417025005', 560.0),  -- Hanuta
('5449000000996', 42.0),   -- Coca-Cola
('5449000214911', 38.0),   -- Fanta
('4008400402123', 495.0),  -- Prinzenrolle
('4000400127600', 460.0),  -- Toffifee
('8000500037560', 539.0),  -- Nutella
('4014400916324', 343.0);  -- Haribo

-- Chips & Salzsnacks
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('4001724818809', 'Funny-frisch Chipsfrisch gesalzen', 'Funny-frisch', 'https://images.openfoodfacts.org/images/products/400/172/481/8809/front_de.jpg'),
('4001724005001', 'Funny-frisch Chipsletten', 'Funny-frisch', 'https://images.openfoodfacts.org/images/products/400/172/400/5001/front_de.jpg'),
('5053990101764', 'Pringles Original', 'Pringles', 'https://images.openfoodfacts.org/images/products/505/399/010/1764/front_en.jpg'),
('8710398025456', 'Lay''s Classic', 'Lay''s', 'https://images.openfoodfacts.org/images/products/871/039/802/5456/front_en.jpg'),
('4260173781403', 'Chio Chips Red Paprika', 'Chio', 'https://images.openfoodfacts.org/images/products/426/017/378/1403/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('4001724818809', 525.0),  -- Funny-frisch gesalzen
('4001724005001', 522.0),  -- Chipsletten
('5053990101764', 536.0),  -- Pringles
('8710398025456', 533.0),  -- Lay's
('4260173781403', 530.0);  -- Chio Red Paprika

-- Frühstück & Cerealien
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('5000354854392', 'Kellogg''s Corn Flakes', 'Kellogg''s', 'https://images.openfoodfacts.org/images/products/500/035/485/4392/front_en.jpg'),
('7613033089089', 'Nestlé Fitness', 'Nestlé', 'https://images.openfoodfacts.org/images/products/761/303/308/9089/front_de.jpg'),
('7613035937253', 'Nestlé Lion Cereals', 'Nestlé', 'https://images.openfoodfacts.org/images/products/761/303/593/7253/front_en.jpg'),
('7622210743664', 'Milka Löffel Ei', 'Milka', 'https://images.openfoodfacts.org/images/products/762/221/074/3664/front_de.jpg'),
('8410076472106', 'Choco Krispies', 'Kellogg''s', 'https://images.openfoodfacts.org/images/products/841/007/647/2106/front_en.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('5000354854392', 357.0),  -- Corn Flakes
('7613033089089', 375.0),  -- Fitness
('7613035937253', 417.0),  -- Lion Cereals
('7622210743664', 535.0),  -- Löffel Ei
('8410076472106', 391.0);  -- Choco Krispies

-- Milchprodukte & Joghurt
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('4000400131119', 'Danone Fruchtzwerge Erdbeere', 'Danone', 'https://images.openfoodfacts.org/images/products/400/040/013/1119/front_de.jpg'),
('4025500021412', 'Müller Milchreis Original', 'Müller', 'https://images.openfoodfacts.org/images/products/402/550/002/1412/front_de.jpg'),
('4025500021207', 'Müller Milch Schoko', 'Müller', 'https://images.openfoodfacts.org/images/products/402/550/002/1207/front_de.jpg'),
('7613033493195', 'Nesquik Schoko-Drink', 'Nestlé', 'https://images.openfoodfacts.org/images/products/761/303/349/3195/front_de.jpg'),
('4000400121783', 'Ehrmann Almighurt Erdbeere', 'Ehrmann', 'https://images.openfoodfacts.org/images/products/400/040/012/1783/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('4000400131119', 102.0),  -- Fruchtzwerge
('4025500021412', 112.0),  -- Müller Milchreis
('4025500021207', 70.0),   -- Müller Milch Schoko
('7613033493195', 73.0),   -- Nesquik
('4000400121783', 87.0);   -- Almighurt

-- Pizza & Fertiggerichte
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('4001724825005', 'Wagner Steinofen Pizza Salami', 'Wagner', 'https://images.openfoodfacts.org/images/products/400/172/482/5005/front_de.jpg'),
('4003880011452', 'Dr. Oetker Ristorante Pizza Margherita', 'Dr. Oetker', 'https://images.openfoodfacts.org/images/products/400/388/001/1452/front_de.jpg'),
('4311596484294', 'Original Wagner Die Backfrische Pizza Hawaii', 'Wagner', 'https://images.openfoodfacts.org/images/products/431/159/648/4294/front_de.jpg'),
('5410063011854', 'Maggi Ravioli in Tomatensoße', 'Maggi', 'https://images.openfoodfacts.org/images/products/541/006/301/1854/front_de.jpg'),
('4056489011293', 'Iglo Chicken Nuggets', 'Iglo', 'https://images.openfoodfacts.org/images/products/405/648/901/1293/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('4001724825005', 259.0),  -- Wagner Steinofen Salami
('4003880011452', 225.0),  -- Dr. Oetker Margherita
('4311596484294', 217.0),  -- Wagner Hawaii
('5410063011854', 90.0),   -- Ravioli
('4056489011293', 236.0);  -- Chicken Nuggets

-- Backwaren & Kekse
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('4000446015187', 'Bahlsen Leibniz Butterkeks', 'Bahlsen', 'https://images.openfoodfacts.org/images/products/400/044/601/5187/front_de.jpg'),
('4000446010007', 'Bahlsen Pick Up Choco', 'Bahlsen', 'https://images.openfoodfacts.org/images/products/400/044/601/0007/front_de.jpg'),
('7622210449900', 'Milka Choco Wafer', 'Milka', 'https://images.openfoodfacts.org/images/products/762/221/044/9900/front_de.jpg'),
('8410100017686', 'Oreo Original', 'Oreo', 'https://images.openfoodfacts.org/images/products/841/010/001/7686/front_en.jpg'),
('4011100003699', 'Kinder Bueno', 'Ferrero', 'https://images.openfoodfacts.org/images/products/401/110/000/3699/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('4000446015187', 432.0),  -- Leibniz
('4000446010007', 486.0),  -- Pick Up
('7622210449900', 524.0),  -- Choco Wafer
('8410100017686', 478.0),  -- Oreo
('4011100003699', 574.0);  -- Kinder Bueno

-- Getränke & Energy Drinks
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('9002491000223', 'Red Bull Energy Drink', 'Red Bull', 'https://images.openfoodfacts.org/images/products/900/249/100/0223/front_en.jpg'),
('5449000214799', 'Sprite', 'Sprite', 'https://images.openfoodfacts.org/images/products/544/900/021/4799/front_en.jpg'),
('8410128002862', 'Aquarius Lemon', 'Aquarius', 'https://images.openfoodfacts.org/images/products/841/012/800/2862/front_en.jpg'),
('4260231221476', 'Fritz-Kola', 'Fritz', 'https://images.openfoodfacts.org/images/products/426/023/122/1476/front_de.jpg'),
('5449000133335', 'Fanta Lemon', 'Fanta', 'https://images.openfoodfacts.org/images/products/544/900/013/3335/front_en.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('9002491000223', 45.0),   -- Red Bull
('5449000214799', 37.0),   -- Sprite
('8410128002862', 26.0),   -- Aquarius
('4260231221476', 41.0),   -- Fritz-Kola
('5449000133335', 40.0);   -- Fanta Lemon

-- Eis & Desserts
INSERT INTO PRODUCT (BARCODE, NAME, BRAND, IMAGE_URL) VALUES
('4000467026003', 'Langnese Magnum Classic', 'Langnese', 'https://images.openfoodfacts.org/images/products/400/046/702/6003/front_de.jpg'),
('4056489000846', 'Langnese Cornetto Classico', 'Langnese', 'https://images.openfoodfacts.org/images/products/405/648/900/0846/front_de.jpg'),
('8712100663048', 'Ben & Jerry''s Cookie Dough', 'Ben & Jerry''s', 'https://images.openfoodfacts.org/images/products/871/210/066/3048/front_en.jpg'),
('5000159459227', 'Mars Ice Cream', 'Mars', 'https://images.openfoodfacts.org/images/products/500/015/945/9227/front_en.jpg'),
('4056489016914', 'Langnese Cremissimo Vanille', 'Langnese', 'https://images.openfoodfacts.org/images/products/405/648/901/6914/front_de.jpg');

INSERT INTO NUTRITION_FACTS (BARCODE, KCAL_100G) VALUES
('4000467026003', 329.0),  -- Magnum Classic
('4056489000846', 280.0),  -- Cornetto
('8712100663048', 257.0),  -- Ben & Jerry's
('5000159459227', 260.0),  -- Mars Ice
('4056489016914', 203.0);  -- Cremissimo

