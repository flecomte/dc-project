do
$$
declare
    article_count int = (select count(*) from article);
begin
    insert into constitution (id, version_id, created_by_id, title, anonymous, created_at)
    select
        uuid_in(md5('constitution'||row_number() over ())::cstring),
        uuid_in(md5('constitution_v'||row_number() over ())::cstring),
        z.id,
        'title' || row_number() over (),
        row_number() over () % 3 = 0,
        now() + (row_number() over () * interval '7 minute 3 second')
    from citizen z;

    insert into title (id, name, rank, constitution_id)
    select
        uuid_in(md5('constitution_title'||row_number() over ())::cstring),
        'name' || row_number() over (),
        row_number() over (),
        c.id
    from constitution c,
    lateral generate_series(1, 5) g;

    insert into article_in_title (id, rank, title_id, article_id, constitution_id)
    select
        uuid_in(md5('article_in_title'||row_number() over ())::cstring),
        row_number() over (),
        ti.id,
        a.id,
        ti.constitution_id
    from (select *, (row_number() over () % (article_count+7)) rn from title, lateral generate_series(1, 3) g) ti
    join (select *, row_number() over () rn from article) a using (rn);

    raise notice 'constitution fixtures done';
end;
$$;

