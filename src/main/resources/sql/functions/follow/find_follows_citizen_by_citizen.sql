create or replace function find_follows_citizen_by_citizen(
    _created_by_id uuid,
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
            find_citizen_by_id_with_user(f.target_id) as target,
            find_citizen_by_id_with_user(f.created_by_id) as created_by
        from follow as f
        where created_by_id = _created_by_id
        order by created_at desc,
        f.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;
