ALTER TABLE `users`
ADD COLUMN `web_push_enabled` TINYINT(1) NOT NULL DEFAULT 0
AFTER `avatar_url`;
