CREATE TABLE catalogue_item (
    id          INT AUTO_INCREMENT NOT NULL,
    guid        VARCHAR(40) NOT NULL,
    caption     VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX catalogue_guid ON catalogue_item(guid);

INSERT INTO catalogue_item (guid, caption, description) VALUES
  ('81994f73-50c3-4035-b4e3-e81c5c250ddd', 'Leather Sofa',  'A very nice and comfortable sofa'),
  ('1a1d3681-3dbf-4cde-849d-0372fd77c1f7', 'Wooden Table',  'A large table ideal for 6 to 8 people'),
  ('3787efde-9365-4b4b-bd67-ce567393afb1', 'Plastic Chair', 'A robust plastic chair ideal for children and adults alike'),
  ('54411222-b919-4cc1-9c68-0f40d4ff3640', 'Mug',           'The ideal way to start the day'),
  ('722c76af-82f8-4124-ab28-b2cb392e04ea', 'LED TV',        'A very large TV set, ideal for those who love to binge-watch TV shows');
