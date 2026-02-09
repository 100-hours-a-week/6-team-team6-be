ALTER TABLE membership
    ADD COLUMN nickname VARCHAR(20) NOT NULL;

ALTER TABLE users
    DROP COLUMN nickname;
