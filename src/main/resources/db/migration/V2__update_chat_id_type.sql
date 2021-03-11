alter table users alter column chat_id type bigint using chat_id::bigint;

alter table users alter column first_visit type timestamp using first_visit::timestamp;

alter table users alter column last_status_changed_at type timestamp using last_status_changed_at::timestamp;
