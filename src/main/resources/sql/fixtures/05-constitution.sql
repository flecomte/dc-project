do
$$
declare
    article_count int = (select count(*) from article);
begin
    delete from article_in_title;
    delete from title;
    delete from constitution;

    insert into constitution (id, version_id, created_by_id, title, anonymous)
    select
        uuid_in(md5('constitution'||row_number() over ())::cstring),
        uuid_in(md5('constitution_v'||row_number() over ())::cstring),
        z.id,
        'title' || row_number() over (),
        row_number() over () % 3 = 0
    from citizen z;

    insert into title (id, created_by_id, name, rank, constitution_id)
    select
        uuid_in(md5('constitution_title'||row_number() over ())::cstring),
        c.created_by_id,
        'name' || row_number() over (),
        row_number() over (),
        c.id
    from constitution c,
    lateral generate_series(1, 5) g;

    insert into article_in_title (id, created_by_id, rank, title_id, article_id, constitution_id)
    select
        uuid_in(md5('article_in_title'||row_number() over ())::cstring),
        ti.created_by_id,
        row_number() over (),
        ti.id,
        a.id,
        ti.constitution_id
    from (select *, (row_number() over () % (article_count+7)) rn from title, lateral generate_series(1, 3) g) ti
    join (select *, row_number() over () rn from article) a using (rn);

    raise notice 'constitution fixtures done';
end;
$$;

