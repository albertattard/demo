CREATE TABLE cart (
  id BIGINT AUTO_INCREMENT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE cart_item (
  cart_id     BIGINT NOT NULL,
  item_id     BIGINT NOT NULL,
  quantity    INT NOT NULL,
  PRIMARY KEY (cart_id, item_id),
  CONSTRAINT fk_cart_item_cart_id FOREIGN KEY (cart_id) REFERENCES cart(id)
);

INSERT INTO cart () VALUES
  (),
  (),
  ();

INSERT INTO cart_item (cart_id, item_id, quantity) VALUES
  (1, 1, 1),
  (1, 5, 1),
  (2, 2, 1),
  (2, 3, 6),
  (3, 4, 4);
