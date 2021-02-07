create table users
(
    id serial not null,
    first_name text,
    last_name text,
    username text,
    status text,
    first_visit date
);

create unique index users_id_uindex
	on users (id);

alter table users
    add constraint users_pk
        primary key (id);

