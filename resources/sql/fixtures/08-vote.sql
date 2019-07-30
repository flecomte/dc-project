do
$$
begin
    delete from vote_for_article;
    delete from vote_for_constitution;
    delete from vote_for_comment_on_article;
    delete from vote_for_comment_on_constitution;

    insert into vote_for_article (citizen_id, target_id, note, anonymous)
    select
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % 995 rn, g from citizen, lateral generate_series(1, 10) g) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into vote_for_constitution (citizen_id, target_id, note, anonymous)
    select
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % 995 rn, g from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    insert into vote_for_comment_on_article (citizen_id, target_id, note, anonymous)
    select
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % 995 rn, g from citizen, lateral generate_series(1, 3) g) z
    join (select *, row_number() over () rn from comment_on_article) a using (rn);

    insert into vote_for_comment_on_constitution (citizen_id, target_id, note, anonymous)
    select
        z.id,
        a.id,
        (row_number() over () % 3) -1,
        (row_number() over () % 3 = 1)
    from (select *, row_number() over () % 995 rn, g from citizen, lateral generate_series(1, 2) g) z
    join (select *, row_number() over () rn from comment_on_constitution) a using (rn);

    raise notice 'vote fixtures done';
end;
$$;

