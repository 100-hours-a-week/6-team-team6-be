DROP INDEX `idx_post_membership_id_deleted_at` ON `post`;

CREATE INDEX idx_post_membership_deleted_updated_id
    ON post (membership_id, deleted_at, updated_at DESC, id DESC);
