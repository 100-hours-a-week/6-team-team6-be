CREATE INDEX `idx_post_membership_id_deleted_at`
  ON `post` (`membership_id`, `deleted_at`);

CREATE INDEX `idx_membership_group_id`
  ON `membership` (`group_id`);
