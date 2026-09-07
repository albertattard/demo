ALTER TABLE catalogue_item
    ADD COLUMN guid CHAR(36) NULL;

-- Set the missing guids
-- We are setting the UUID of the first item so that we can refer to it from the example
UPDATE catalogue_item SET guid = '81994f73-50c3-4035-b4e3-e81c5c250ddd' WHERE id = 1;
UPDATE catalogue_item SET guid = UUID() WHERE guid IS NULL;

ALTER TABLE catalogue_item
    ALTER COLUMN guid SET NOT NULL;

ALTER TABLE catalogue_item
    ADD CONSTRAINT uq_catalogue_item_guid UNIQUE (guid);
