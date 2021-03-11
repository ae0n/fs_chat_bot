create table users
(
    id                     serial                   not null,
    first_name             text                     not null,
    last_name              text,
    username               text,
    chat_id                int                      not null,
    status                 text default 'unchecked' not null,
    first_visit            date default now()       not null,
    last_status_changed_at date default now()       not null
);

create
unique index users_id_uindex
	on users (id);

alter table users
    add constraint users_pk
        primary key (id);

