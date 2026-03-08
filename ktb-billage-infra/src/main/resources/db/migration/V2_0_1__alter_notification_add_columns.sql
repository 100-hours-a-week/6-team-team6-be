ALTER TABLE `notification`
    ADD COLUMN `user_id` bigint NOT NULL,
    ADD COLUMN `group_id` bigint NOT NULL,
    ADD COLUMN `post_id` bigint DEFAULT NULL,
    ADD COLUMN `chatroom_id` bigint DEFAULT NULL,
    ADD COLUMN `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ADD COLUMN `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL;
