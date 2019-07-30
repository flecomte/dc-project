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
begin
    delete from article_relations;
    delete from article;

    insert into article (version_id, created_by_id, title, annonymous, content, description, tags)
    select
        uuid_generate_v4(),
        z.id,
        'title' || row_number() over (),
        row_number() over () % 3 = 0,
        'content' || row_number() over (),
        'description' || row_number() over (),
        _tags[(row_number() over () % 5):(row_number() over () % 9)]
    from citizen z;

    insert into article_relations (source_id, target_id, created_by_id)
    select
        src.id,
        dest.id,
        src.created_by_id
    from (select *, row_number() over () rn from article, lateral generate_series(1, 5) g) src
    join (select *, row_number() over () +5 rn from article) dest using (rn);

    raise notice 'article fixtures done';
end;
$$;

