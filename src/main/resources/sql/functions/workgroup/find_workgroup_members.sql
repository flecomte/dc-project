create or replace function find_workgroup_members(in _id uuid, out resource json) language plpgsql as
$$
begin
    select json_agg(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id) as "user"
        from citizen_in_workgroup as ciw
        join citizen z on z.id = ciw.citizen_id
        where ciw.workgroup_id = _id
    ) as t;
end;
$$;

-- drop function if exists find_workgroup_members(uuid, out json);
