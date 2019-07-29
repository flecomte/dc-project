do
$$
begin
    delete from follow;

    insert into follow_article (citizen_id, target_id)
    select
        z.id,
        a.id
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from article) a using (rn);

    insert into follow_constitution (citizen_id, target_id)
    select
        z.id,
        a.id
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from constitution) a using (rn);

    insert into follow_citizen (citizen_id, target_id)
    select
        z.id,
        a.id
    from (select *, row_number() over () % 995 rn from citizen, lateral generate_series(1, 5)) z
    join (select *, row_number() over () rn from citizen) a using (rn);

    raise notice 'follow fixtures done';
end;
$$;

