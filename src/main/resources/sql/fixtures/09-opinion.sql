do
$$
declare
    article_count int = (select count(*) from article);
    _citizensIds uuid[] = (select array_agg(id) from citizen);
begin
    delete from opinion_on_article;
    delete from opinion_list;

    insert into opinion_list (id, name, target)
    select uuid_in(md5('opinion_list'||row_number() over ())::cstring), 'Opinion'||row_number() over (), 'article'
        from generate_series(0,20);

    for i in 0..9 loop
        insert into opinion_on_article (id, created_by_id, target_id, opinion)
        select
            uuid_in(md5('opinion_on_article'||rn+(article_count*i))::cstring),
            z.id,
            a.id,
            uuid_in(md5('opinion_list'||((rn+i) % 5 +1))::cstring)
        from (select *, row_number() over ()+i+5 % 5 rn from citizen) z
        join (select *, row_number() over () rn from article) a using (rn);
    end loop;

    raise notice '% opinion inserted', (select count(*) from opinion_on_article);

    raise notice 'opinions fixtures done';
end;
$$;
