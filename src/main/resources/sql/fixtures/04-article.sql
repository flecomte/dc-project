do
$$
declare
    _tags text[] = $tags$
    {
        "nature", "green", "sky",
        "nuclear", "oil", "black",
        "love", "human", "scuirel"
    }
    $tags$;
    _citizen_count int = (select count(z) from citizen z);
    _workgroup_count int = (select count(w) from workgroup w);
begin
    delete from article_relations;
    delete from article;

    insert into article (id, version_id, created_by_id, workgroup_id, title, anonymous, content, description, tags, created_at, draft)
    select
        uuid_in(md5('article'||row_number() over ())::cstring),
        uuid_in(md5('article_v'||row_number() over () % (_citizen_count / 2))::cstring),
        z.id,
        case when row_number() over () % 2 = 0 then w.id end,
        'title' || row_number() over (),
        row_number() over () % 3 = 0,
        'content' || row_number() over (),
        'description' || row_number() over (),
        _tags[(row_number() over () % 5):(row_number() over () % 9)],
        now() + (row_number() over () * interval '7 minute 3 second'),
        (row_number() over () % 7) = 0
    from (select *, row_number() over () rn from citizen z) z
    join (select *, row_number() over () % _workgroup_count rn from workgroup) w using (rn);

    insert into article_relations (source_id, target_id, created_by_id, comment)
    select
        src.id,
        dest.id,
        src.created_by_id,
        'comment' || rn
    from (select *, row_number() over () rn from article, lateral generate_series(1, 5) g) src
    join (select *, row_number() over () +5 rn from article) dest using (rn);

    raise notice 'article fixtures done';
end;
$$;
