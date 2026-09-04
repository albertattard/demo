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

CREATE TABLE users (
    id      VARCHAR(40) NOT NULL,
    name    VARCHAR(40) NOT NULL,
    pass    VARCHAR(56) NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO users (id, name, pass) VALUES
  ('1fa664b3-dfa9-4d08-a3d8-fd4981afae5d', 'Alice', '9817fd4e8ae39d6e41532989e4422c5a7e46411dab4d2fdfa2270dad'),
  ('5c0e4d85-b2f8-4562-baf4-db46045a3ec4', 'Bob',   '6be790258b73da9441099c4cb6aeec1f0c883152dd74e7581b70a648'),
  ('fec8e43c-df24-4d32-8c94-548cafcb913c', 'Carol', 'c3f847612c3780385a859a1993dfd9fe7c4e6d7f477148e527e9374c'),
  ('22f02702-0abb-43e8-acdc-f61872041ddb', 'Dave',  'dc90c84556782c69d8a1f0c4335cb8a62421b881967da419d2c05530'),
  ('86eff67e-ea89-4adb-baff-babd20525f93', 'Ella',  '90abb2b597fde9552143f564ae0a247b11983f74ef836be504e7d808'),
  ('3182d1b4-da6b-4241-9615-29cc7a994f4f', 'Frank', '5795c3d628fd638c9835a4c79a55809f265068c88729a1a3fcdf8522'),
  ('57c2d9ac-c6a8-4c25-8b21-e195d7be044c', 'Gina',  '9c5f9835aa7ba0f4dd9ecb265277ee59c39016492b26c63371573481'),
  ('66745ec2-c19e-48aa-b4cd-b5fa8ffe953b', 'Harry', '64f977868a7a63bb65ddcd10c587b50ad21fe885fe667c99acc5d6f1'),
  ('d3ccb984-e394-4d1d-b58b-0ad93cf493be', 'Ivy',   '94cc697550f5c7399d179e206cf1e7bf90e17de8a87ff0f9368ec839'),
  ('9a595dd8-24e9-42a1-8efc-0ac96cdb7f7e', 'Jerry', '026727ec105a060b02a0086a2181748f6b9ac3cea3fc347ca8675984');
