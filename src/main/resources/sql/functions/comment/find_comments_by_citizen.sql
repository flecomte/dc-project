create or replace function find_comments_by_citizen(
    _citizen_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from "comment")
    into resource, total
    from (
        select
            com.*,
            json_build_object('id', com.target_id) as target,
            find_citizen_by_id(com.citizen_id) as citizen
        from "comment" as com
        where citizen_id = _citizen_id
        order by created_at desc,
        com.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_comments_by_citizen(uuid, int, int);
