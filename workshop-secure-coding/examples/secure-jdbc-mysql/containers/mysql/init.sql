DROP TABLE IF EXISTS catalogue_item;

CREATE TABLE catalogue_item (
    id          INT AUTO_INCREMENT NOT NULL,
    caption     VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO catalogue_item (caption, description) VALUES
  ('Leather Sofa',  'A very nice and comfortable sofa'),
  ('Wooden Table',  'A large table ideal for 6 to 8 people'),
  ('Plastic Chair', 'A robust plastic chair ideal for children and adults alike'),
  ('Mug',           'The ideal way to start the day'),
  ('LED TV',        'A very large TV set, ideal for those who love to binge-watch TV shows');
