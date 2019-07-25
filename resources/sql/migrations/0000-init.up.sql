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
    name            text                                   not null,
    rank            int                                    not null,
    constitution_id uuid                                   not null references constitution (id)
);

create table article_in_title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
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