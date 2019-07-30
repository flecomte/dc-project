do
$$
begin
    delete from comment;

    insert into comment_on_article (citizen_id, target_id, content)
    select
        z.id,
        a.id,
        'content' || (row_number() over () * g)
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into comment_on_article (citizen_id, target_id, content, parent_id)
    select
        z.id,
        a.target_id,
        'content' || row_number() over () * g,
        a.id
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from comment_on_article) a using (rn);

    insert into comment_on_article (citizen_id, target_id, content, parent_id)
    select
        z.id,
        a.target_id,
        'content' || row_number() over () * g,
        a.id
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from comment_on_article where parent_id is not null) a using (rn);

    insert into comment_on_constitution (citizen_id, target_id, content)
    select
        z.id,
        a.id,
        'content' || row_number() over () * g
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5) g) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    raise notice 'comment fixtures done';
end;
$$;

