-- Users
create table "user"
(
    id         uuid        default uuid_generate_v4() not null primary key,
    created_at timestamptz default now()              not null,
    updated_at timestamptz default now()              not null check ( updated_at >= created_at ),
    blocked_at timestamptz default null               null,
    username   varchar(64)                            not null check ( username != '' and lower(username) = username) unique,
    password   text                                   not null check ( password != '' ),
    roles      text[]      default '{}'               not null
);

create table citizen
(
    id                uuid        default uuid_generate_v4() not null primary key,
    created_at        timestamptz default now()              not null,
    name              jsonb                                  not null check ( name ? 'first_name' and name ? 'last_name' ),
    birthday          date                                   not null,
    user_id           uuid                                   not null references "user" (id) unique,
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
    citizen_id   uuid                      not null references citizen (id),
    workgroup_id uuid                      not null references workgroup (id),
    created_at   timestamptz default now() not null,
    primary key (citizen_id, workgroup_id)
);

create table moderator
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    updated_at      timestamptz default now()              not null check ( updated_at >= created_at ),
    assigned_period tstzrange[] default '{}'               not null,
    user_id         uuid                                   not null references "user" (id)
);

-- Article & Constitution

create or replace function generate_version_number(tablename regclass, version_id uuid, out generated_number int)
    language plpgsql as
$$
declare
    _version_id alias for version_id;
begin
    if (tablename = 'article'::regclass) then
        select version_number + 1
        into generated_number
        from article as t
        where t.version_id = _version_id
        order by version_number desc
        limit 1;
    elseif tablename = 'constitution'::regclass then
        select version_number + 1
        into generated_number
        from constitution as t
        where t.version_id = _version_id
        order by version_number desc
        limit 1;
    else
        raise exception '% is not implemented', tablename::text;
    end if;

    if not found then
        generated_number := 1;
    end if;
end;
$$;

create or replace function set_version_number() returns trigger
    language plpgsql as
$$
begin
    new.version_number = generate_version_number(tg_table_name::regclass, new.version_id);
    return new;
end;
$$;

create table article
(
    id             uuid          default uuid_generate_v4() not null primary key,
    created_at     timestamptz   default now()              not null,
    created_by_id  uuid                                     not null references citizen (id),
    version_id     uuid          default uuid_generate_v4() not null,
    version_number int                                      not null,
    title          text                                     not null check ( length(title) < 128 ),
    annonymous     boolean       default false              not null,
    content        text                                     not null check ( content != '' ),
    description    text                                     null check ( description != '' ),
    tags           varchar(32)[] default '{}'               not null,
    unique (version_id, version_number)
);

create trigger generate_version_number_trigger
    before insert
    on article
    for each row
execute function set_version_number();

create table constitution
(
    id             uuid        default uuid_generate_v4() not null primary key,
    created_at     timestamptz default now()              not null,
    created_by_id  uuid                                   not null references citizen (id),
    version_id     uuid        default uuid_generate_v4() not null,
    version_number int                                    not null,
    title          text                                   not null check ( length(title) < 128 ),
    annonymous     boolean     default false              not null
);

create trigger generate_version_number_trigger
    before insert
    on constitution
    for each row
execute procedure set_version_number();

create table title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references citizen (id),
    name            text                                   not null check ( name != '' ),
    rank            int                                    not null check ( rank >= 0 ),
    constitution_id uuid                                   not null references constitution (id)
);

create table article_in_title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    created_by_id   uuid                                   not null references citizen (id),
    rank            int                                    not null check ( rank >= 0 ),
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
    return new;
end;
$$;

create trigger set_constitution_link_trigger
    before insert
    on article_in_title
    for each row
execute procedure set_constitution_link();

create table article_relations
(
    source_id     uuid references article,
    target_id     uuid references article check ( source_id != target_id ),
    created_at    timestamptz default now(),
    created_by_id uuid not null references citizen (id),
    comment       text null check ( comment != '' ),
    primary key (source_id, target_id)
);

-- Extra resources

create table extra
(
    id               uuid        default uuid_generate_v4() not null primary key,
    created_at       timestamptz default now()              not null,
    created_by_id    uuid                                   not null references citizen (id),
    target_id        uuid                                   not null,
    target_reference regclass                               not null
);

create table follow
(
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (extra);

create table follow_article
(
    target_reference regclass default 'article'::regclass not null,
    foreign key (created_by_id) references citizen (id),
    foreign key (target_id) references article (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (follow);

create table follow_constitution
(
    target_reference regclass default 'constitution'::regclass not null,
    foreign key (created_by_id) references citizen (id),
    foreign key (target_id) references constitution (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (follow);

create table follow_citizen
(
    target_reference regclass default 'citizen'::regclass not null,
    foreign key (created_by_id) references citizen (id),
    foreign key (target_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (follow);



create table comment
(
    updated_at  timestamptz default now() not null check ( updated_at >= created_at ),
    "content"   text                      not null check ( content != '' and length(content) < 4096),
    parent_id   uuid references comment (id),
    parents_ids uuid[],
    foreign key (created_by_id) references citizen (id),
    primary key (id)
) inherits (extra);

create index comment_parents_ids_idx
    on comment (parents_ids);

create or replace function set_comment_parents_ids() returns trigger
    language plpgsql as
$$
begin
    if (new.parent_id is not null) then
        new.parents_ids = (
            select com.parents_ids || com.id
            from "comment" com
            where com.id = new.parent_id
        );
    else
        new.parents_ids = null;
    end if;

    return new;
end;
$$;

create trigger set_comment_parents_ids_trigger
    before insert
    on comment
    for each row
execute procedure set_comment_parents_ids();

create table comment_on_article
(
    target_reference regclass default 'article'::regclass not null,
    foreign key (created_by_id) references citizen (id),
    foreign key (target_id) references article (id),
    foreign key (parent_id) references comment_on_article (id),
    primary key (id)
) inherits (comment);

create index comment_on_article_parents_ids_idx
    on comment_on_article (parents_ids);

create trigger set_comment_on_article_parents_ids_trigger
    before insert
    on comment_on_article
    for each row
execute procedure set_comment_parents_ids();

create table comment_on_constitution
(
    target_reference regclass default 'constitution'::regclass not null,
    foreign key (created_by_id) references citizen (id),
    foreign key (target_id) references constitution (id),
    foreign key (parent_id) references comment_on_constitution (id),
    primary key (id)
) inherits (comment);

create index comment_on_constitution_parents_ids_idx
    on comment_on_constitution (parents_ids);

create trigger set_comment_on_constitution_parents_ids_trigger
    before insert
    on comment_on_constitution
    for each row
execute procedure set_comment_parents_ids();



create table vote
(
    anonymous boolean default true not null,
    note      int                  not null check ( note >= -1 and note <= 1 ),
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (extra);

create table vote_for_article
(
    target_reference regclass default 'article'::regclass not null,
    foreign key (target_id) references article (id),
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (vote);

create table vote_for_constitution
(
    target_reference regclass default 'constitution'::regclass not null,
    foreign key (target_id) references constitution (id),
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (vote);

create table vote_for_comment_on_article
(
    target_reference regclass default 'comment_on_article'::regclass not null,
    foreign key (target_id) references comment_on_article (id),
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (vote);

create table vote_for_comment_on_constitution
(
    target_reference regclass default 'comment_on_constitution'::regclass not null,
    foreign key (target_id) references comment_on_constitution (id),
    foreign key (created_by_id) references citizen (id),
    primary key (id),
    unique (created_by_id, target_id)
) inherits (vote);

-- Stats
create table resource_view
(
    id            uuid        default uuid_generate_v4() not null primary key,
    type          regclass                               not null,
    created_at    timestamptz default now()              not null,
    created_by_id uuid                                   null references citizen (id),
    ip            cidr                                   null
);