create or replace function find_comments_constitution_by_citizen(
    _created_by_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from comment where target_reference = 'constitution'::regclass)
    into resource, total
    from (
        select
            com.*,
            find_constitution_by_id(com.target_id) as target,
            find_citizen_by_id(com.created_by_id) as created_by
        from comment as com
        where created_by_id = _created_by_id
        and target_reference = 'constitution'::regclass
        order by created_at desc,
        com.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_comments_constitution_by_citizen(uuid, int, int);
