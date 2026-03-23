ALTER TABLE `keyword_subscription`
    ADD COLUMN `user_id` bigint NOT NULL,
    ADD COLUMN `group_id` bigint NOT NULL,
    ADD COLUMN `keyword` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL;
