CREATE TABLE offer (
  id                IDENTITY PRIMARY KEY,
  slug              VARCHAR(140) NOT NULL UNIQUE,
  title             VARCHAR(300) NOT NULL,
  subtitle          VARCHAR(300) NOT NULL,
  description       CLOB         NOT NULL,
  price             DECIMAL      NOT NULL,
  price_description VARCHAR(300) NOT NULL,
  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trip_highlight (
  id          IDENTITY PRIMARY KEY,
  offer_id    BIGINT       NOT NULL,
  highlight   VARCHAR(300) NOT NULL,
  FOREIGN KEY (offer_id) REFERENCES offer(id)
);

CREATE TABLE itinerary (
  id          IDENTITY PRIMARY KEY,
  offer_id    BIGINT       NOT NULL,
  title       VARCHAR(300) NOT NULL,
  FOREIGN KEY (offer_id) REFERENCES offer(id)
);

CREATE TABLE itinerary_entry (
  id           IDENTITY PRIMARY KEY,
  itinerary_id BIGINT       NOT NULL,
  description  VARCHAR(300) NOT NULL,
  FOREIGN KEY (itinerary_id) REFERENCES itinerary(id)
);

CREATE TABLE package_detail (
  id          IDENTITY PRIMARY KEY,
  offer_id    BIGINT       NOT NULL,
  description VARCHAR(300) NOT NULL,
  FOREIGN KEY (offer_id) REFERENCES offer(id)
);

CREATE TABLE best_travel_season (
  id          IDENTITY PRIMARY KEY,
  offer_id    BIGINT       NOT NULL,
  season      VARCHAR(300) NOT NULL,
  description VARCHAR(300) NOT NULL,
  FOREIGN KEY (offer_id) REFERENCES offer(id)
);


INSERT INTO offer (slug, title, subtitle, description, price, price_description) VALUES
  ('7-day-adventure-extreme-sports-alps', '7-Day Adventure & Extreme Sports - Alps', 'Unforgettable Winter Holidays for the Whole Family – All-Inclusive Packages', 'This exhilarating winter holiday package is designed for families and adventure seekers looking to experience Europe’s top ski resorts. From beginner-friendly lessons and kid-focused snow activities to guided mountain tours for experienced skiers, every day is packed with fun. Guests stay in cozy alpine hotels with full-board meals, après-ski entertainment, and access to spas, wellness centers, and snow adventures like tubing and sledding. All flights, transfers, equipment rental, and ski passes are included, making it a stress-free winter escape.', 2350, 'double occupancy, 4-star hotels');

INSERT INTO trip_highlight (offer_id, highlight) VALUES
  (1, 'Ski Europe’s top resorts – French Alps, Austria, Switzerland & Italy'),
  (1, 'Beginner-friendly lessons & kid-friendly snow activities'),
  (1, 'Optional ski pass bundles & equipment rental included'),
  (1, 'Après-ski fun: sledding, tubing, spas & cozy lounges'),
  (1, 'Family-friendly hotels with childcare & kids’ clubs');

INSERT INTO itinerary (offer_id, title) VALUES
  (1, 'Day 1 – Arrival'),
  (1, 'Day 2 – Ski School & Beginners’ Lessons'),
  (1, 'Day 3 – Guided Mountain Tours'),
  (1, 'Day 4 – Snow Adventures'),
  (1, 'Day 5 – Explore Local Villages'),
  (1, 'Day 6 – Free Day'),
  (1, 'Day 7 – Departure');

INSERT INTO itinerary_entry (itinerary_id, description) VALUES
  (1, 'Transfer to your chosen ski resort & welcome dinner'),
  (2, 'Morning: beginner & intermediate classes'),
  (2, 'Evening: family entertainment at hotel'),
  (3, 'Ski or snowboard with experienced instructors'),
  (3, 'Relax at the hotel spa after skiing'),
  (4, 'Snow tubing, sledding & snowshoeing activities'),
  (4, 'Cozy fireside après-ski'),
  (5, 'Optional guided tour of nearby alpine towns'),
  (6, 'Choose between skiing, spa, or wellness activities'),
  (7, 'Morning leisure time & airport transfer');

INSERT INTO package_detail (offer_id, description) VALUES
  (1, 'Return flights from main EU & UK airports'),
  (1, 'Airport transfers to resort'),
  (1, '4-star hotel accommodation (upgradable to 5-star)'),
  (1, 'Full-board meals & après-ski dinners'),
  (1, 'Daily ski lessons & equipment rental');

INSERT INTO best_travel_season (offer_id, season, description) VALUES
  (1, 'December – March', 'peak skiing season');


INSERT INTO offer (slug, title, subtitle, description, price, price_description) VALUES
  ('7-day-cultural-historical-immersion-in-greece',          '7-Day Cultural & Historical Immersion in Greece',        'Walk in the Footsteps of Ancient Philosophers & Heroes',                      'This cultural journey through Greece takes you deep into the heart of ancient history and mythology. Explore Athens’ iconic Acropolis, travel to Delphi’s legendary Oracle of Apollo, and marvel at the Meteora monasteries perched on dramatic cliffs. Along the way, enjoy authentic Greek cuisine, stroll through historic towns, and witness a stunning sunset at Cape Sounion’s Temple of Poseidon. With guided tours by expert historians, comfortable hotels, and seamless transfers, this trip is perfect for lovers of history and culture.',      2250, 'based on double occupancy, 4-star hotels');

INSERT INTO trip_highlight (offer_id, highlight) VALUES
  (2, 'Explore ancient temples & UNESCO sites'),
  (2, 'Discover Greek mythology & classical history'),
  (2, 'Taste authentic Greek cuisine & experience local traditions'),
  (2, 'Guided tours by expert historians & archaeologists');

INSERT INTO itinerary (offer_id, title) VALUES
  (2, 'Day 1 – Arrival in Athens'),
  (2, 'Day 2 – The Acropolis & Ancient Athens'),
  (2, 'Day 3 – Delphi: Oracle of Apollo'),
  (2, 'Day 4 – Meteora Monasteries'),
  (2, 'Day 5 – Back to Athens via Thermopylae'),
  (2, 'Day 6 – Cape Sounion & Poseidon’s Temple'),
  (2, 'Day 7 – Departure');

INSERT INTO itinerary_entry (itinerary_id, description) VALUES
  (8, 'Morning: Arrival at Athens International Airport, private transfer to hotel'),
  (8, 'Afternoon: Guided stroll through Plaka (Athens’ old town)'),
  (8, 'Evening: Welcome dinner at a traditional Greek taverna'),
  (9, 'Morning: Guided tour of the Acropolis, Parthenon, Erechtheion & Dionysus Theater'),
  (9, 'Afternoon: Visit the Acropolis Museum & Ancient Agora'),
  (9, 'Evening: Optional wine & meze tasting experience in Monastiraki'),
  (10, 'Morning: Scenic drive to Delphi (UNESCO site)'),
  (10, 'Tour Highlights: Temple of Apollo, Athenian Treasury & Delphi Museum'),
  (10, 'Evening: Overnight at a boutique hotel in Delphi'),
  (11, 'Morning: Transfer to Meteora – famous for its monasteries perched on rock pillars'),
  (11, 'Afternoon: Visit 2–3 active monasteries with a local historian guide'),
  (11, 'Evening: Sunset view over Meteora rocks, overnight in Kalambaka'),
  (12, 'Morning: Stop at Thermopylae, site of the famous battle of Leonidas & the 300 Spartans'),
  (12, 'Afternoon: Return to Athens, free evening for shopping or relaxation'),
  (13, 'Morning: Relaxing morning in Athens'),
  (13, 'Afternoon Excursion: Drive along the Aegean coast to Cape Sounion'),
  (13, 'Evening: Sunset at the Temple of Poseidon, farewell seafood dinner by the sea'),
  (14, 'Morning: Free time for last-minute shopping'),
  (14, 'Private transfer to Athens airport for your flight home');

INSERT INTO package_detail (offer_id, description) VALUES
  (2, 'Return flights from main EU & UK airports'),
  (2, '4-star or boutique hotels (upgradeable to 5-star luxury)'),
  (2, 'Daily breakfast & 3 special Greek dinners'),
  (2, 'Private transfers & guided tours'),
  (2, 'All entrance fees to historical sites'),
  (2, 'Experienced English-speaking cultural guide');

INSERT INTO best_travel_season (offer_id, season, description) VALUES
  (2, 'April – June', 'pleasant weather'),
  (2, 'September – October', 'fewer crowds');


INSERT INTO offer (slug, title, subtitle, description, price, price_description) VALUES
  ('10-day-luxury-wellness-relaxation-retreat-bali',         '10-Day Luxury Wellness & Relaxation Retreat - Bali',     'Recharge, Rejuvenate & Rediscover Your Inner Balance',                        'This rejuvenating retreat blends luxury living with holistic wellness practices, set amid Bali’s serene jungles and breathtaking beaches. Guests enjoy daily yoga and meditation sessions, Ayurvedic healing treatments, and indulgent spa rituals such as flower baths, hot stone massages, and chakra balancing. Mindfulness walks, plant-based cooking classes, and beachfront candlelit dinners complete the experience. With 5-star accommodations, organic meals, and wellness experts at hand, this retreat promises the ultimate mind-body reset.', 7800, '10 days, double occupancy');

INSERT INTO trip_highlight (offer_id, highlight) VALUES
  (3, '5-star spa resorts & private villas'),
  (3, 'Daily yoga, meditation & breathwork sessions'),
  (3, 'Holistic spa therapies & traditional healing rituals'),
  (3, 'Organic wellness cuisine crafted by private chefs'),
  (3, 'Gentle nature excursions & sunset serenity experiences');

INSERT INTO itinerary (offer_id, title) VALUES
  (3, 'Day 1 – Arrival in Bali'),
  (3, 'Day 2 – Morning Yoga & Balinese Spa Therapy'),
  (3, 'Day 3 – Ayurvedic Healing & Nutrition Workshop'),
  (3, 'Day 4 – Nature & Mindfulness Immersion'),
  (3, 'Day 5 – Transition to Beachfront Luxury'),
  (3, 'Day 6 – Ocean Wellness Experience'),
  (3, 'Day 7 – Detox & Full Spa Day'),
  (3, 'Day 8 – Free Day of Pure Relaxation'),
  (3, 'Day 9 – Cultural Serenity'),
  (3, 'Day 10 – Departure');

INSERT INTO itinerary_entry (itinerary_id, description) VALUES
  (15, 'VIP airport welcome & transfer to a luxury jungle villa'),
  (15, 'Welcome herbal elixir & wellness consultation'),
  (16, 'Sunrise yoga with panoramic jungle views'),
  (16, 'Traditional Balinese massage & aromatic flower bath'),
  (17, 'Personalized Ayurvedic consultation'),
  (17, 'Cooking class: plant-based wellness cuisine'),
  (18, 'Guided mindfulness trek through Ubud’s rice terraces & Sacred Monkey Forest'),
  (18, 'Evening sound bath & meditation session'),
  (19, 'Private transfer to an exclusive oceanfront villa'),
  (19, 'Relaxation massage & sunset seafood wellness dinner'),
  (20, 'Gentle stand-up paddle yoga or snorkeling with tropical fish'),
  (20, 'Beachfront hot stone massage in a private cabana'),
  (21, 'Herbal steam therapy & detoxifying body wrap'),
  (21, 'Chakra balancing or reiki energy healing session'),
  (22, 'Choose between spa, yoga, or simply enjoying your villa & infinity pool'),
  (23, 'Optional half-day excursion to Tanah Lot Temple or Uluwatu Cliff Temple'),
  (23, 'Farewell candlelit dinner on the beach'),
  (24, 'Morning meditation, farewell brunch & private transfer to airport');

INSERT INTO package_detail (offer_id, description) VALUES
  (3, 'Return flights from main EU & UK airports'),
  (3, 'Luxury accommodations (5-star jungle & beachfront villas)'),
  (3, 'Daily spa treatments & wellness activities'),
  (3, 'Gourmet organic meals (breakfast, lunch & dinner)'),
  (3, 'Private airport transfers & 24/7 concierge');

INSERT INTO best_travel_season (offer_id, season, description) VALUES
  (3, 'April – October', 'sunny & dry');


INSERT INTO offer (slug, title, subtitle, description, price, price_description) VALUES
  ('10-day-underwater-exploration-in-the-mediterranean-sea', '10-Day Underwater Exploration in the Mediterranean Sea', 'Dive Into Ancient Shipwrecks, Vibrant Reefs & Crystal-Clear Waters',          'Dive into the Mediterranean’s crystal-clear waters with this unforgettable underwater adventure spanning Greece, Cyprus, and Malta. Explore ancient shipwrecks, volcanic craters, and vibrant marine ecosystems, including the world-famous MS Zenobia and Blue Hole dive sites. Snorkel with sea turtles, visit submerged Roman ruins, and unwind on a luxury sunset yacht cruise. The package includes expert dive guides, full gear, luxury seaside accommodations, and seamless island transfers, offering a perfect mix of adventure and relaxation.', 4950, 'double occupancy, full diving package');

INSERT INTO trip_highlight (offer_id, highlight) VALUES
  (4, 'Dive at world-class Mediterranean sites (wrecks, caves & reefs)'),
  (4, 'Snorkeling with sea turtles & dolphins'),
  (4, 'Ancient underwater ruins exploration'),
  (4, 'Luxury yacht excursions & sunset cruises'),
  (4, 'Authentic Mediterranean cuisine & coastal village tours');

INSERT INTO itinerary (offer_id, title) VALUES
  (4, 'Day 1 – Arrival in Athens, Greece'),
  (4, 'Day 2 – Wreck Diving in Zakynthos (Greece)'),
  (4, 'Day 3 – Blue Caves & Navagio Bay (Greece)'),
  (4, 'Day 4 – Santorini Volcano Dive (Greece)'),
  (4, 'Day 5 – Wreck of MS Zenobia (Cyprus)'),
  (4, 'Day 6 – Caves & Tunnels of Cape Greco (Cyprus)'),
  (4, 'Day 7 – Malta’s Blue Hole & Inland Sea'),
  (4, 'Day 8 – Ancient Ruins & Um El Faroud Wreck (Malta)'),
  (4, 'Day 9 – Relaxation & Sunset Yacht Cruise'),
  (4, 'Day 10 – Departure');

INSERT INTO itinerary_entry (itinerary_id, description) VALUES
  (25, 'Private transfer to boutique seaside hotel'),
  (25, 'Welcome dinner with fresh seafood & dive briefing'),
  (26, 'Morning flight/ferry to Zakynthos'),
  (26, 'Dive Highlights: Keri Caves & dramatic rock arches'),
  (26, 'Optional snorkeling with loggerhead sea turtles'),
  (26, 'Overnight in beachfront resort'),
  (27, 'Two dives at Blue Caves – stunning underwater light play'),
  (27, 'Relaxing afternoon at Shipwreck Beach (Navagio Bay)'),
  (27, 'Sunset beach barbecue'),
  (28, 'Flight to Santorini'),
  (28, 'Dive around underwater volcanic craters & lava formations'),
  (28, 'Explore the famous cliffside villages in the evening'),
  (29, 'Flight to Larnaca, Cyprus'),
  (29, 'Dive Highlight: MS Zenobia, one of the world’s top wreck dives (accessible to advanced divers & beginners on the upper deck)'),
  (29, 'Evening stroll & dinner at Larnaca Marina'),
  (30, 'Two dives exploring underwater tunnels & caverns'),
  (30, 'Optional kayaking or cliffside hiking in the afternoon'),
  (31, 'Flight to Gozo Island, Malta'),
  (31, 'Dive Highlights: Famous Blue Hole & Inland Sea caves'),
  (31, 'Visit picturesque Gozo villages in the evening'),
  (32, 'Dive the Um El Faroud oil tanker wreck'),
  (33, 'Snorkeling & exploration of submerged Roman ruins near Valletta'),
  (33, 'Leisure morning – optional spa or light snorkeling'),
  (33, 'Private sunset yacht cruise along Malta’s coastline with champagne & seafood platter'),
  (34, 'Free morning for shopping & souvenirs'),
  (34, 'Private airport transfer for your flight home');

INSERT INTO package_detail (offer_id, description) VALUES
  (4, 'Return flights from EU & UK airports'),
  (4, '4- & 5-star accommodations at seaside resorts'),
  (4, 'Full diving gear & certified dive guides'),
  (4, '2–3 dives/snorkel sessions daily (customizable for all skill levels)'),
  (4, 'Domestic flights & ferry transfers between islands'),
  (4, 'Daily breakfast, 4 seafood dinners & yacht cruise meal');

INSERT INTO best_travel_season (offer_id, season, description) VALUES
  (4, 'May – October', 'warm waters & best visibility, 20–30m');

INSERT INTO offer (slug, title, subtitle, description, price, price_description) VALUES
  ('14-day-nature-wildlife-expedition–south-africa',         '14-Day Nature & Wildlife Expedition – South Africa',     'Experience the Untamed Beauty of Africa’s Wilderness',                        'This ultimate safari adventure combines thrilling wildlife encounters with breathtaking landscapes and cultural discoveries. Track the Big Five in Kruger National Park, marvel at Blyde River Canyon’s dramatic scenery, and cruise among hippos in St. Lucia Wetlands. Explore Swazi culture, visit Cape Town’s iconic Table Mountain and Cape of Good Hope, and sip fine wines in Stellenbosch. With luxury lodges, private guides, and all transfers included, this trip is perfect for those seeking an immersive African wilderness experience.',     4950, 'based on double occupancy');

INSERT INTO trip_highlight (offer_id, highlight) VALUES
  (5, 'Big Five safari in Kruger National Park'),
  (5, 'Scenic drive along the Panorama Route'),
  (5, 'Blyde River Canyon & God’s Window viewpoint'),
  (5, 'Wildlife encounters in Hluhluwe–Imfolozi Park'),
  (5, 'Birdwatching & hippo cruises at St. Lucia Wetlands (iSimangaliso Wetland Park)'),
  (5, 'Whale watching (seasonal) & penguins at the Cape Peninsula'),
  (5, 'Table Mountain & Cape of Good Hope exploration');

INSERT INTO itinerary (offer_id, title) VALUES
  (5, 'Day 1 – Arrival in Johannesburg'),
  (5, 'Day 2 – Johannesburg → Kruger National Park'),
  (5, 'Days 3–5 – Kruger National Park'),
  (5, 'Day 6 – Panorama Route'),
  (5, 'Day 7 – Swaziland (Eswatini)'),
  (5, 'Days 8–9 – Hluhluwe–Imfolozi Park'),
  (5, 'Day 10 – iSimangaliso Wetland Park (St. Lucia)'),
  (5, 'Day 11 – Durban → Cape Town (Flight)'),
  (5, 'Day 12 – Cape Peninsula Tour'),
  (5, 'Day 13 – Table Mountain & Cape Winelands'),
  (5, 'Day 14 – Departure from Cape Town');

INSERT INTO itinerary_entry (itinerary_id, description) VALUES
  (35, 'Meet & greet at O.R. Tambo International Airport'),
  (35, 'Private transfer to hotel, welcome dinner & trip briefing'),
  (36, 'Scenic drive or flight to Kruger'),
  (36, 'Sunset safari drive – first encounter with elephants, lions, and giraffes'),
  (37, 'Morning & evening game drives with expert rangers'),
  (37, 'Bush walk experience for up-close wildlife tracking'),
  (37, 'Optional hot-air balloon safari'),
  (38, 'Visit Blyde River Canyon, Bourke’s Luck Potholes & God’s Window'),
  (38, 'Overnight at a lodge with stunning canyon views'),
  (39, 'Cultural stopover: Swazi cultural village & local craft markets'),
  (40, 'Game drives focusing on rhino conservation'),
  (40, 'Learn about anti-poaching initiatives'),
  (41, 'Boat cruise to see hippos, crocodiles & exotic birds'),
  (41, 'Sunset beach walk along the Indian Ocean'),
  (42, 'Free afternoon to relax or explore Durban’s beachfront'),
  (43, 'Cape Point, Cape of Good Hope & African penguins at Boulders Beach'),
  (43, 'Scenic Chapman’s Peak Drive'),
  (44, 'Cable car up Table Mountain'),
  (44, 'Wine tasting & gourmet lunch at Stellenbosch vineyards'),
  (45, 'Final shopping & airport transfer');

INSERT INTO package_detail (offer_id, description) VALUES
  (5, 'Return flights from EU & UK airports'),
  (5, 'Accommodation in luxury lodges & boutique hotels'),
  (5, 'Daily breakfast, selected lunches & dinners'),
  (5, 'Private transfers & domestic flights'),
  (5, 'All park entrance fees & guided tours'),
  (5, '24/7 travel concierge support');

INSERT INTO best_travel_season (offer_id, season, description) VALUES
  (5, 'May – October', 'dry season, best for wildlife viewing'),
  (5, 'June – November', 'whale watching season near Cape Town');
