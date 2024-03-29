do
$$
declare
    article_count int = (select count(*) from article);
begin
    insert into comment_on_article (id, created_by_id, target_id, content)
    select
        uuid_in(md5('comment_on_article'||row_number() over ())::cstring),
        z.id,
        a.id,
        'content' || (row_number() over () * g)
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into comment_on_article (id, created_by_id, target_id, content, parent_comment_id)
    select
        uuid_in(md5('comment_on_article_2'||row_number() over ())::cstring),
        z.id,
        a.target_id,
        'content' || row_number() over () * g,
        a.id
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from comment_on_article) a using (rn);

    insert into comment_on_article (id, created_by_id, target_id, content, parent_comment_id)
    select
        uuid_in(md5('comment_on_article_3'||row_number() over ())::cstring),
        z.id,
        a.target_id,
        'content' || row_number() over () * g,
        a.id
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from comment_on_article where parent_comment_id is not null) a using (rn);

    insert into comment_on_constitution (id, created_by_id, target_id, content)
    select
        uuid_in(md5('comment_on_constitution'||row_number() over ())::cstring),
        z.id,
        a.id,
        'content' || row_number() over () * g
    from (select *, row_number() over () % (article_count+7) rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    raise notice 'comment fixtures done';
end;
$$;

