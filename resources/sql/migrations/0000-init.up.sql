-- Users
create extension if not exists pgcrypto;
-- select *
-- from "user"
-- where username = lower('nick@example.com')
--   and password = crypt('12346', password);

create table "user"
(
    id         uuid        default uuid_generate_v4() not null primary key,
    created_at timestamptz default now()              not null,
    updated_at timestamptz default now()              not null check ( updated_at >= created_at ),
    blocked_at timestamptz default null               null,
    username   varchar(64)                            not null check ( username != '' and lower(username) = username) unique,
    password   text                                   not null check ( password != '' )
);

create type "name" as (
    first_name text,
    last_name text,
    civility text
    );

create table citizen
(
    id                uuid        default uuid_generate_v4() not null primary key,
    created_at        timestamptz default now()              not null,
    name              "name"                                 not null check ( name != '' ),
    birthday          date                                   not null,
    user_id           uuid                                   not null references "user" (id),
    vote_annonymous   boolean     default true               not null,
    follow_annonymous boolean     default true               not null
);

create table workgroup
(
    id            uuid        default uuid_generate_v4() not null primary key,
    created_at    timestamptz default now()              not null,
    updated_at    timestamptz default now()              not null check ( updated_at >= created_at ),
    created_by_id uuid                                   not null references citizen (id),
    name          varchar(128)                           not null,
    description   text                                   null,
    annonymous    boolean     default false              not null,
    logo          text                                   null,
    owner_id      uuid                                   not null references citizen (id)
);

create table citizen_in_workgroup
(
    citizen_id uuid                      not null references citizen (id),
    workgroup  uuid                      not null references workgroup (id),
    created_at timestamptz default now() not null,
    primary key (citizen_id, workgroup)
);

create table moderator
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    updated_at      timestamptz default now()              not null check ( updated_at >= created_at ),
    assigned_period tstzrange[] default '{}'               not null,
    user_id         uuid                                   not null references "user" (id)
);

-- Article & Contitution

create or replace function generate_version_number(tablename regclass, version_id uuid) returns int
    language plpgsql as
$$
begin
    return random(); -- TODO
end;
$$;

create or replace function set_version_number() returns trigger
    language plpgsql as
$$
begin
    new.version_number = generate_version_number(tg_table_name::regclass, new.version_id);
end;
$$;

create table article
(
    id             uuid          default uuid_generate_v4() not null primary key,
    created_at     timestamptz   default now()              not null,
    created_by_id  uuid                                     not null references citizen (id),
    version_id     uuid          default uuid_generate_v4() not null,
    version_number int                                      not null unique,
    title          text                                     not null,
    annonymous     boolean       default false              not null,
    content        text                                     not null check ( content != '' ),
    description    text,
    tags           varchar(32)[] default '{}'               not null
);

create trigger generate_version_number_trigger
    before insert
    on article
execute procedure set_version_number();

create table constitution
(
    id             uuid        default uuid_generate_v4() not null primary key,
    created_at     timestamptz default now()              not null,
    created_by_id  uuid                                   not null references citizen (id),
    version_id     uuid        default uuid_generate_v4() not null,
    version_number int                                    not null,
    title          text                                   not null,
    annonymous     boolean     default false              not null
);

create trigger generate_version_number_trigger
    before insert
    on constitution
execute procedure set_version_number();

create table title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references citizen (id),
    name            text                                   not null check ( name != '' ),
    rank            int                                    not null,
    constitution_id uuid                                   not null references constitution (id)
);

create table article_in_title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references citizen (id),
    rank            int                                    not null,
    title_id        uuid                                   not null references title (id),
    article_id      uuid                                   not null references article (id),
    constitution_id uuid                                   not null references constitution (id)
);

create or replace function set_constitution_link() returns trigger
    language plpgsql as
$$
begin
    new.constitution_id = (
        select t.constitution_id
        from title as t
        where t.id = new.title_id
    );
end;
$$;

create trigger set_constitution_link_trigger
    before insert
    on article_in_title
execute procedure set_constitution_link();

create table article_relations
(
    source_id     uuid references article,
    target_id     uuid references article check ( source_id != target_id ),
    created_at    timestamptz default now(),
    created_by_id uuid not null references citizen (id),
    primary key (source_id, target_id)
);

-- Extra resources

create table extra
(
    id               uuid        default uuid_generate_v4() not null primary key,
    created_at       timestamptz default now()              not null,
    citizen_id       uuid                                   not null references citizen (id),
    target_id        uuid                                   not null,
    target_reference regclass                               not null
);

create table follow
(
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (extra);

create table follow_article
(
    foreign key (citizen_id) references citizen (id),
    foreign key (target_id) references article (id),
    primary key (id)
) inherits (follow);

create table follow_constitution
(
    foreign key (citizen_id) references citizen (id),
    foreign key (target_id) references constitution (id),
    primary key (id)
) inherits (follow);

create table follow_citizen
(
    foreign key (citizen_id) references citizen (id),
    foreign key (target_id) references citizen (id),
    primary key (id)
) inherits (follow);



create table comment
(
    updated_at timestamptz default now() not null check ( updated_at >= created_at ),
    "content"  text                      not null check ( content != '' ),
    parent_id  uuid                      null references comment (id),
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (extra);

create table comment_on_article
(
    foreign key (citizen_id) references citizen (id),
    foreign key (target_id) references article (id),
    foreign key (parent_id) references comment_on_article (id),
    primary key (id)
) inherits (comment);

create table comment_on_constitution
(
    foreign key (citizen_id) references citizen (id),
    foreign key (target_id) references constitution (id),
    foreign key (parent_id) references comment_on_constitution (id),
    primary key (id)
) inherits (comment);



create table vote
(
    anonymous boolean default true not null,
    note      int                  not null check ( note >= -1 and note <= 1 ),
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (extra);

create table vote_for_article
(
    foreign key (target_id) references article (id),
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (vote);

create table vote_for_constitution
(
    foreign key (target_id) references constitution (id),
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (vote);

create table vote_for_comment_on_article
(
    foreign key (target_id) references comment_on_article (id),
    foreign key (citizen_id) references citizen (id),
    primary key (id)
) inherits (vote);

create table vote_for_comment_on_constitution
(
    primary key (id),
    foreign key (target_id) references comment_on_constitution (id),
    foreign key (target_id) references citizen (id)
) inherits (vote);
