create or replace function find_comment_parent_by_id(
    _parent_id uuid,
    out resource json
) language plpgsql as
$$
begin
    select to_json(t)
    into resource
    from (
        select
            id,
            deleted_at,
            json_build_object('id', target_id, 'reference', target_reference) as target
        from "comment" cp
        where id = _parent_id
    ) as t;
end;
$$;


