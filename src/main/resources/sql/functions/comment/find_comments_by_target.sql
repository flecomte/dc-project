create or replace function find_comments_by_target(
    _target_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from "comment" c3 where c3.target_id = _target_id)
    into resource, total
    from (
        select
            com.*,
            (select count(*) from "comment" c2 where c2.parents_ids @> array[com.id]) as children_count,
            find_reference_by_id(com.target_id, com.target_reference) as target,
            find_citizen_by_id(com.created_by_id) as created_by
        from "comment" as com
        where com.target_id = _target_id
        order by created_at asc,
        com.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_comments_by_target(uuid, int, int);
