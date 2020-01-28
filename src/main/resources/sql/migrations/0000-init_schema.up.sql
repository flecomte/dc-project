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
    id               uuid        default uuid_generate_v4() not null primary key,
    created_at       timestamptz default now()              not null,
    name             jsonb                                  not null check ( name ? 'first_name' and name ? 'last_name' ),
    birthday         date                                   not null,
    user_id          uuid                                   not null references "user" (id) unique,
    vote_anonymous   boolean     default true               not null,
    follow_anonymous boolean     default true               not null,
    email            text                                   not null check ( email ~* '.+@.+\..+' ) unique
);

create table workgroup
(
    id            uuid        default uuid_generate_v4() not null primary key,
    created_at    timestamptz default now()              not null,
    updated_at    timestamptz default now()              not null check ( updated_at >= created_at ),
    created_by_id uuid                                   not null references citizen (id),
    name          varchar(128)                           not null,
    description   text                                   null,
    anonymous     boolean     default false              not null,
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


-------------------------------------
-- Article & Constitution triggers --
-------------------------------------

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
        raise exception '% is not implemented for function "generate_version_number"', tablename::text;
    end if;

    if not found then
        generated_number := 1;
    end if;
end;
$$;

create or replace function set_all_version_to_old(tablename regclass, version_id uuid) returns void
    language plpgsql as
$$
declare
    _version_id alias for version_id;
begin
    if (tablename = 'article'::regclass) then
        update article a
        set last_version = false
        where a.version_id = _version_id
          and a.last_version = true;
    elseif (tablename = 'constitution'::regclass) then
        update constitution c
        set last_version = false
        where c.version_id = _version_id
          and c.last_version = true;
    else
        raise exception '% is not implemented for function "set_all_version_to_old"', tablename::text;
    end if;
end;
$$;

create or replace function set_correct_last_version(tablename regclass, version_id uuid) returns void
    language plpgsql as
$$
declare
    _version_id alias for version_id;
begin
    perform set_all_version_to_old(tablename, _version_id);

    if (tablename = 'article'::regclass) then
        update article a1
        set last_version = true
        from (
            select id
            from article a2
            where a2.version_id = _version_id
              and a2.draft = false
              and a2.deleted_at is null
            order by version_number desc
            limit 1
        ) as a3
        where a1.version_id = _version_id
          and a1.id = a3.id;
    elseif (tablename = 'constitution'::regclass) then
        update constitution c1
        set last_version = true
        from (
            select id
            from constitution c2
            where c2.version_id = _version_id
              and c2.draft = false
              and c2.deleted_at is null
            order by version_number desc
            limit 1
        ) as c3
        where c1.version_id = _version_id
          and c1.id = c3.id;
    else
        raise exception '% is not implemented for function "set_correct_last_version"', tablename::text;
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

create or replace function set_to_last_version() returns trigger
    language plpgsql as
$$
begin
    if (new.draft = false and new.deleted_at is null) then
        perform set_all_version_to_old(tg_table_name::regclass, new.version_id);
        new.last_version = true;
    else
        new.last_version = false;
    end if;
    return new;
end;
$$;

create or replace function set_last_version() returns trigger
    language plpgsql as
$$
begin
    if (new.draft != old.draft or new.deleted_at != old.deleted_at) then
        perform set_correct_last_version(tg_table_name::regclass, new.version_id);
    end if;
    return new;
end;
$$;

-------------
-- Article --
-------------
create table article
(
    id             uuid          default uuid_generate_v4() not null primary key,
    created_at     timestamptz   default now()              not null,
    created_by_id  uuid                                     not null references citizen (id),
    version_id     uuid          default uuid_generate_v4() not null,
    version_number int                                      not null,
    title          text                                     not null check ( length(title) < 128 ),
    anonymous      boolean       default false              not null,
    content        text                                     not null check ( content != '' and length(content) < 4096 ),
    description    text                                     null check ( description != '' and length(description) < 4096 ),
    tags           varchar(32)[] default '{}'               not null,
    deleted_at     timestamptz   default null               null,
    draft          boolean       default false              not null,
    last_version   boolean       default false              not null,
    unique (version_id, version_number)
);

create unique index last_version_article_idx on article (last_version, version_id) where last_version = true;

create trigger generate_version_number_trigger
    before insert
    on article
    for each row
execute function set_version_number();

create trigger set_to_last_version_trigger
    before insert
    on article
    for each row
execute function set_to_last_version();

create trigger set_last_version_trigger
    after update
    on article
    for each row
execute function set_last_version();

------------------
-- Constitution --
------------------

create table constitution
(
    id             uuid        default uuid_generate_v4() not null primary key,
    created_at     timestamptz default now()              not null,
    created_by_id  uuid                                   not null references citizen (id),
    version_id     uuid        default uuid_generate_v4() not null,
    version_number int                                    not null,
    title          text                                   not null check ( length(title) < 128 ),
    intro          text                                   null check ( length(intro) < 4096 ),
    anonymous      boolean     default false              not null,
    deleted_at     timestamptz default null               null,
    draft          boolean     default false              not null,
    last_version   boolean     default false              not null,
    unique (version_id, version_number)
);

create unique index last_version_constitution_idx on constitution (last_version, version_id) where last_version = true;

create trigger generate_version_number_trigger
    before insert
    on constitution
    for each row
execute procedure set_version_number();

create trigger set_to_last_version_trigger
    before insert
    on constitution
    for each row
execute function set_to_last_version();

create trigger set_last_version_trigger
    after update
    on constitution
    for each row
execute function set_last_version();

------


create table title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
    name            text                                   not null check ( name != '' ),
    rank            int                                    not null check ( rank >= 0 ),
    constitution_id uuid                                   not null references constitution (id)
);

create table article_in_title
(
    id              uuid        default uuid_generate_v4() not null primary key,
    created_at      timestamptz default now()              not null,
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
    updated_at        timestamptz default now() not null check ( updated_at >= created_at ),
    "content"         text                      not null check ( content != '' and length(content) < 4096),
    parent_id         uuid                      not null,
    parent_comment_id uuid references comment (id),
    parents_ids       uuid[],
    deleted_at        timestamptz               null,
    foreign key (created_by_id) references citizen (id),
    primary key (id)
) inherits (extra);

create index comment_parents_ids_idx
    on comment (parents_ids);

create index parent_id
    on comment (parent_id);

create or replace function set_comment_parents_ids() returns trigger
    language plpgsql as
$$
begin
    if (new.parent_comment_id is not null) then
        new.parents_ids = (
            select com.parents_ids || com.id
            from "comment" com
            where com.id = new.parent_comment_id
        );
    else
        new.parents_ids = array [new.target_id]::uuid[];
    end if;

    new.parent_id = (new.parents_ids[array_upper(new.parents_ids, 1)]);

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
    foreign key (parent_comment_id) references comment_on_article (id),
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
    foreign key (parent_comment_id) references comment_on_constitution (id),
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
    updated_at timestamptz default now() not null check ( updated_at >= created_at ),
    anonymous  boolean     default true  not null,
    note       int                       not null check ( note >= -1 and note <= 1 ),
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



--------------
-- ZOMBO DB --
--------------

-- Filter
select zdb.define_filter('french_stop', '{
  "type": "stop",
  "stopwords": "_french_",
  "ignore_case": true
}');

select zdb.define_filter('french_elision', '{
  "type": "elision",
  "articles": [
    "à",
    "ainsi",
    "alors",
    "assez",
    "au",
    "aussi",
    "aux",
    "c",
    "ça",
    "car",
    "ce",
    "cela",
    "ces",
    "ceux",
    "ci",
    "celle",
    "celles",
    "d",
    "de",
    "déjà",
    "depuis",
    "des",
    "donc",
    "du",
    "et",
    "ici",
    "l",
    "la",
    "là",
    "le",
    "les",
    "leur",
    "leurs",
    "ma",
    "mais",
    "même",
    "mes",
    "mon",
    "ne",
    "ni",
    "notre",
    "nous",
    "ou",
    "où",
    "s",
    "sa",
    "ses",
    "son",
    "t",
    "ta",
    "tant",
    "tantôt",
    "tels",
    "tes",
    "ton",
    "tôt",
    "toujours",
    "trop",
    "un",
    "une",
    "votre",
    "vos"
  ],
  "ignore_case": true
}');

select zdb.define_filter('french_stemmer', '{
  "type": "stemmer",
  "language": "light_french"
}');

select zdb.define_filter('worddelimiter', '{
  "type": "word_delimiter"
}');

-- Tokenizer
select zdb.define_tokenizer('ngram_tokenizer', '{
  "type": "nGram",
  "min_gram": 3,
  "max_gram": 7,
  "token_chars": [
    "letter",
    "digit"
  ]
}');

-- Analyzer
select zdb.define_analyzer('name_analyzer', '{
  "type": "custom",
  "tokenizer": "ngram_tokenizer",
  "filter": [
    "lowercase",
    "asciifolding"
  ]
}');

select zdb.define_analyzer('fr_analyzer', '{
  "tokenizer": "standard",
  "filter": [
    "french_elision",
    "worddelimiter",
    "asciifolding",
    "lowercase",
    "french_stop",
    "french_stemmer"
  ]
}');

-- INDEX article table
select zdb.define_field_mapping('article', 'title', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

select zdb.define_field_mapping('article', 'content', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

select zdb.define_field_mapping('article', 'description', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

select zdb.define_field_mapping('article', 'tags', '{
  "type": "text",
  "analyzer": "name_analyzer",
  "search_analyzer": "name_analyzer"
}');

create index article_idx
    on article
        using zombodb ((article.*))
    with (alias ='article_idx');

reindex index article_idx;


-- INDEX constitution table
select zdb.define_field_mapping('constitution', 'title', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

select zdb.define_field_mapping('constitution', 'intro', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

create index constitution_idx
    on constitution
        using zombodb ((constitution.*))
    with (alias ='constitution_idx');

reindex index constitution_idx;


-- INDEX coment table
select zdb.define_field_mapping('comment', 'content', '{
  "type": "text",
  "analyzer": "fr_analyzer",
  "search_analyzer": "fr_analyzer"
}');

create index comment_idx
    on comment
        using zombodb ((comment.*))
    with (alias ='comment_idx');

reindex index comment_idx;


-- INDEX citizen table
select zdb.define_field_mapping('citizen', 'first_name', '{
  "type": "text",
  "analyzer": "name_analyzer",
  "search_analyzer": "name_analyzer"
}');

select zdb.define_field_mapping('citizen', 'last_name', '{
  "type": "text",
  "analyzer": "name_analyzer",
  "search_analyzer": "name_analyzer"
}');

create index citizen_idx
    on citizen
        using zombodb ((citizen.*))
    with (alias ='citizen_idx');

reindex index citizen_idx;