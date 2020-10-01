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
    insert into article (id, version_id, created_by_id, workgroup_id, title, anonymous, content, description, tags, created_at, draft)
    select
        uuid_in(md5('article'||rn)::cstring),
        uuid_in(md5('article_v'||rn % ((_citizen_count * 50) / 2))::cstring),
        z.id,
        case when rn % 2 = 0 then w.id end,
        'title' || rn,
        rn % 3 = 0,
        'content' || rn,
        'description' || rn,
        _tags[(rn % 5):(rn % 9)],
        now() + (rn * interval '7 minute 3 second'),
        (rn % 7) = 0
    from (select *, row_number() over () rn from citizen z, lateral generate_series(1, 50) g) z
    left join (select *, row_number() over () % _workgroup_count rn from workgroup) w using (rn);

    insert into article_relations (source_id, target_id, created_by_id, comment)
    select
        src.id,
        dest.id,
        src.created_by_id,
        'comment' || rn
    from (select *, row_number() over () rn from article) src
    join (select *, row_number() over () +5 rn from article) dest using (rn);

    raise notice 'article fixtures done';
end;
$$;
