ALTER TABLE membership
    ADD COLUMN nickname VARCHAR(20) NOT NULL;

ALTER TABLE users
    DROP COLUMN nickname;

ALTER TABLE billage_group
    ADD COLUMN created_at datetime(6) DEFAULT NULL,
    ADD COLUMN updated_at datetime(6) DEFAULT NULL,
    ADD COLUMN deleted_at datetime(6) DEFAULT NULL;
