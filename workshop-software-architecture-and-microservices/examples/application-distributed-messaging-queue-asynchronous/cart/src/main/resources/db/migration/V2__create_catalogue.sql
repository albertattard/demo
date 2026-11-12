CREATE TABLE external_catalogue_item (
  id          BIGINT NOT NULL,
  caption     VARCHAR(128) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO external_catalogue_item (id, caption) VALUES
  (1, 'Leather Sofa'),
  (2, 'Wooden Table'),
  (3, 'Plastic Chair'),
  (4, 'Mug'),
  (5, 'LED TV');

ALTER TABLE cart_item
  ADD CONSTRAINT fk_cart_item_catalogue_id FOREIGN KEY (item_id) REFERENCES external_catalogue_item(id);
