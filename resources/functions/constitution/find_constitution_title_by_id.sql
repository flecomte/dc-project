create or replace function find_constitution_title_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            ti.id,
            ti.name,
            ti.rank,
            array_agg(a order by ait.rank) as articles
        into resource
        from title as ti
        left join article_in_title ait on ti.id = ait.title_id
        left join article a on ait.article_id = a.id
        where ti.id = _id
        group by ti.id
        order by ti.rank
    ) as t;
end;
$$;

-- drop function if exists find_constitution_title_by_id(uuid, out json);