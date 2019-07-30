do
$$
begin
    delete from article_in_title;
    delete from title;
    delete from constitution;

    insert into constitution (version_id, created_by_id, title, annonymous)
    select
        uuid_generate_v4(),
        z.id,
        'title' || row_number() over (),
        row_number() over () % 3 = 0
    from citizen z;

    insert into title (created_by_id, name, rank, constitution_id)
    select
        c.created_by_id,
        'name' || row_number() over (),
        row_number() over (),
        c.id
    from constitution c,
    lateral generate_series(1, 5) g;

    insert into article_in_title (created_by_id, rank, title_id, article_id, constitution_id)
    select
        ti.created_by_id,
        row_number() over (),
        ti.id,
        a.id,
        ti.constitution_id
    from (select *, (row_number() over () % 1005) rn from title, lateral generate_series(1, 3) g) ti
    join (select *, row_number() over () rn from article) a using (rn);

    raise notice 'constitution fixtures done';
end;
$$;

