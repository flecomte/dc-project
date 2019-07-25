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
    id             uuid          default uuid_generate_v4() not null,
    version_id     uuid          default uuid_generate_v4() not null,
    version_number int                                      not null,
    title          text                                     not null,
    annonymous     boolean       default false              not null,
    content        text                                     not null,
    description    text,
    tags           varchar(32)[] default '{}'               not null,
    created_at     timestamptz   default now()              not null
);

CREATE TRIGGER generate_version_number_trigger
    BEFORE INSERT
    ON article
EXECUTE PROCEDURE set_version_number();
