CREATE TABLE recipe (
  id                  BIGSERIAL NOT NULL PRIMARY KEY,
  title               VARCHAR(255) NOT NULL,
  description         CLOB NOT NULL,
  type                ENUM('DESSERT', 'MAIN', 'STARTER') NOT NULL,
  vegan               BOOLEAN NOT NULL,
  time_needed_minutes INT NOT NULL
);

INSERT INTO recipe (title, description, type, vegan, time_needed_minutes) VALUES
  ('Chocolate Chip Muffins',                  FILE_READ('classpath:recipes/desserts/Chocolate Chip Muffins.md'),              'DESSERT', false,  45),
  ('Classic Rainbow Layer Cake',              FILE_READ('classpath:recipes/desserts/Classic Rainbow Layer Cake.md'),          'DESSERT', false, 180),
  ('Classic Garlic & Rosemary Roast Lamb',    FILE_READ('classpath:recipes/main/Classic Garlic & Rosemary Roast Lamb.md'),    'MAIN',    false, 120),
  ('Creamy Chickpea & Spinach Coconut Curry', FILE_READ('classpath:recipes/main/Creamy Chickpea & Spinach Coconut Curry.md'), 'MAIN',    true,  120),
  ('Classic Tomato Basil Soup',               FILE_READ('classpath:recipes/starters/Classic Tomato Basil Soup.md'),           'STARTER', false,  30),
  ('Garlic Mushroom Bruschetta',              FILE_READ('classpath:recipes/starters/Garlic Mushroom Bruschetta.md'),          'STARTER', false,  20);
