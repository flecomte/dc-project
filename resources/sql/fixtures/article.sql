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

    raise notice 'article fixtures done';
end;
$$;

