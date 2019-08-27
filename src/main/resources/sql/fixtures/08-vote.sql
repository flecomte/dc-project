do
$$
declare
    article_count int = (select count(*) from article);
begin
    delete from vote_for_article;
    delete from vote_for_constitution;
    delete from vote_for_comment_on_article;
    delete from vote_for_comment_on_constitution;
raise notice '%', article_count;
    insert into vote_for_article (id, created_by_id, target_id, note, anonymous)
    select
        uuid_in(md5('vote_for_article'||row_number() over ())::cstring),
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over ()+g % (article_count+7) rn, g from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into vote_for_constitution (id, created_by_id, target_id, note, anonymous)
    select
        uuid_in(md5('vote_for_constitution'||row_number() over ())::cstring),
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % (article_count+7) rn, g from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    insert into vote_for_comment_on_article (id, created_by_id, target_id, note, anonymous)
    select
        uuid_in(md5('vote_for_comment_on_article'||row_number() over ())::cstring),
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % (article_count+7) rn, g from citizen, lateral generate_series(1, 3) g) z
    join (select *, row_number() over () rn from comment_on_article) a using (rn);

    insert into vote_for_comment_on_constitution (id, created_by_id, target_id, note, anonymous)
    select
        uuid_in(md5('vote_for_comment_on_constitution'||row_number() over ())::cstring),
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % (article_count+7) rn, g from citizen, lateral generate_series(1, 2) g) z
    join (select *, row_number() over () rn from comment_on_constitution) a using (rn);

    raise notice 'vote fixtures done';
end;
$$;

