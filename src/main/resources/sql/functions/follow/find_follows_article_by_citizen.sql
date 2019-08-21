create or replace function find_follows_article_by_citizen(
    _citizen_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from follow)
    into resource, total
    from (
        select
            f.*,
            find_article_by_id(f.target_id) as target,
            find_citizen_by_id(f.citizen_id) as citizen
        from follow as f
        where citizen_id = _citizen_id
        order by created_at desc,
        f.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_follows_article_by_citizen(uuid, int, int);
