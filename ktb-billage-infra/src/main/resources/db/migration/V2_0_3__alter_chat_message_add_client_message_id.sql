ALTER TABLE `chat_message`
    ADD COLUMN `client_message_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL;

UPDATE `chat_message`
SET `client_message_id` = UUID()
WHERE `client_message_id` IS NULL
   OR `client_message_id` = '';

ALTER TABLE `chat_message`
    MODIFY COLUMN `client_message_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL;
