do
$$
declare
    article_count int = (select count(*) from article);
begin
    delete from opinion_on_article;
    delete from opinion_choice;

    insert into opinion_choice (id, name, target)
    select
           uuid_in(md5('opinion_choice'||row_number() over ())::cstring),
           'Opinion'||row_number() over (),
           case when row_number() over () % 5 = 0 then null else '{article}'::text[] end
        from generate_series(0,20);

    for i in 0..9 loop
        insert into opinion_on_article (id, created_by_id, target_id, choice_id, created_at)
        select
            uuid_in(md5('opinion_on_article'||rn+(article_count*i))::cstring),
            z.id,
            a.id,
            uuid_in(md5('opinion_choice'||((rn+i) % 5 +1))::cstring),
            now() + ((rn+i) || ' minute')::interval
        from (select *, row_number() over ()+i+5 % 5 rn from citizen) z
        join (select *, row_number() over () rn from article) a using (rn);
    end loop;

    raise notice 'opinions fixtures done';
end;
$$;
