-- Users

create table "user"
(
    id         uuid        default uuid_generate_v4() not null primary key,
    created_at timestamptz default now()              not null,
    updated_at timestamptz default now()              not null,
    blocked_at timestamptz default null               null,
    username   varchar(64)                            not null,
    password   varchar(258)                           not null
);

create type "name" as (
    first_name text,
    last_name text,
    civility text
    );

create table citizen
(
    id         uuid        default uuid_generate_v4() not null primary key,
    created_at timestamptz default now()              not null,
    name       "name"                                 not null,
    birthday   date                                   not null,
    user_id    uuid                                   not null references "user" (id)
);

create table workgroup
(
    id            uuid        default uuid_generate_v4() not null primary key,
    created_at    timestamptz default now()              not null,
    updated_at    timestamptz default now()              not null,
    created_by_id uuid                                   not null references "user" (id),
    name          varchar(128)                           not null,
    description   text                                   not null,
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
    updated_at      timestamptz default now()              not null,
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
    new.version_number = generate_version_number(TG_TABLE_NAME::regclass, new.version_id);
end;
$$;

create table article
(
    id             uuid          default uuid_generate_v4() not null primary key,
    created_at     timestamptz   default now()              not null,
    created_by_id  uuid                                     not null references "user" (id),
    version_id     uuid          default uuid_generate_v4() not null,
    version_number int                                      not null,
    title          text                                     not null,
    annonymous     boolean       default false              not null,
    content        text                                     not null,
    description    text,
    tags           varchar(32)[] default '{}'               not null
);

CREATE TRIGGER generate_version_number_trigger
    BEFORE INSERT
    ON article
EXECUTE PROCEDURE set_version_number();

create table constitution
(
    id             uuid        default uuid_generate_v4() not null primary key,
    created_at     timestamptz default now()              not null,
    created_by_id  uuid                                   not null references "user" (id),
    version_id     uuid        default uuid_generate_v4() not null,
    version_number int                                    not null,
    title          text                                   not null,
    annonymous     boolean     default false              not null
);

CREATE TRIGGER generate_version_number_trigger
    BEFORE INSERT
    ON constitution
EXECUTE PROCEDURE set_version_number();

create table title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references "user" (id),
    name            text                                   not null,
    rank            int                                    not null,
    constitution_id uuid                                   not null references constitution (id)
);

create table article_in_title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references "user" (id),
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

CREATE TRIGGER set_constitution_link_trigger
    BEFORE INSERT
    ON article_in_title
EXECUTE PROCEDURE set_constitution_link();

create table article_relations
(
    source_id     uuid references article,
    target_id     uuid references article check ( source_id != target_id ),
    created_at    timestamptz default now(),
    created_by_id uuid not null references "user" (id),
    primary key (source_id, target_id)
);
