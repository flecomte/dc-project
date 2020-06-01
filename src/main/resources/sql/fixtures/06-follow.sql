do
$$
declare
    article_count int = (select count(*) from article);
begin
    insert into follow_article (id, created_by_id, target_id)
    select
        uuid_in(md5('follow_article'||row_number() over ())::cstring),
        z.id,
        a.id
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into follow_constitution (id, created_by_id, target_id)
    select
        uuid_in(md5('follow_constitution'||row_number() over ())::cstring),
        z.id,
        a.id
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    insert into follow_citizen (id, created_by_id, target_id)
    select
        uuid_in(md5('follow_citizen'||row_number() over ())::cstring),
        z.id,
        a.id
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from citizen) a using (rn);

    raise notice 'follow fixtures done';
end;
$$;

