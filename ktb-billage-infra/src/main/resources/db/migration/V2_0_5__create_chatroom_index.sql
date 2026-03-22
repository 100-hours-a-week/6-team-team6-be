create index idx_chatroom_buyer_deleted on chatroom (buyer_id, deleted_at);
create index idx_post_membership_id on post (membership_id, id);
