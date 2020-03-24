create or replace function find_workgroup_members(in _id uuid, out resource json) language plpgsql as
$$
begin
    select json_agg(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id) as "user"
        from citizen_in_workgroup as ciw
        join workgroup as w on ciw.workgroup_id = w.id
        join citizen z on z.id = ciw.citizen_id
        where ciw.workgroup_id = _id and (w.deleted_at is null or w.deleted_at > now())
    ) as t;

    resource = coalesce(resource, '[]'::json);
end;
$$;
