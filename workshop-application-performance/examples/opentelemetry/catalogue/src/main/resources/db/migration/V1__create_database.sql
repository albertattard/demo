CREATE TABLE catalogue_item (
    id          INT AUTO_INCREMENT NOT NULL,
    caption     VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO catalogue_item (caption, description) VALUES
  ('Leather Sofa',     'A very nice and comfortable sofa'),
  ('Wooden Table',     'A large table ideal for 6 to 8 people'),
  ('Plastic Chair',    'A robust plastic chair ideal for children and adults alike'),
  ('Mug',              'The ideal way to start the day'),
  ('LED TV',           'A very large TV set, ideal for those who love to binge-watch TV shows'),
  ('Bookshelf',        'Spacious and stylish, perfect for organizing books and decor'),
  ('Office Desk',      'A sturdy desk suitable for both home and office use'),
  ('Recliner Chair',   'Relax in comfort with this adjustable recliner'),
  ('Dining Set',       'Includes a table and four chairs, perfect for family meals'),
  ('Floor Lamp',       'Modern lamp that adds ambiance to any room'),
  ('Wall Clock',       'A large decorative clock that fits any interior'),
  ('Microwave Oven',   'Compact microwave for quick and easy meals'),
  ('Blender',          'Perfect for making smoothies, soups, and sauces'),
  ('Vacuum Cleaner',   'Lightweight and powerful for everyday cleaning'),
  ('Bookshelf Speaker','Compact speakers with high-quality sound'),
  ('Coffee Table',     'A stylish centerpiece for your living room'),
  ('Curtains',         'Elegant drapes that complement any decor'),
  ('Bed Frame',        'Strong and durable bed frame for restful nights'),
  ('Mattress',         'Comfortable and supportive for a good night''s sleep'),
  ('Nightstand',       'A sleek nightstand with drawer for bedtime essentials'),
  ('Wardrobe',         'Spacious wardrobe to keep your clothes neatly stored'),
  ('Toaster',          'Toast bread to perfection every time'),
  ('Kettle',           'Boils water quickly and efficiently'),
  ('Laundry Basket',   'Durable and lightweight basket for easy laundry transport'),
  ('Wall Art',         'Add character to your home with this modern art piece');
