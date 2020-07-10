create or replace function find_citizen_by_user_id(in user_id uuid, out resource json) language plpgsql as
$$
declare
    _user_id alias for user_id;
begin
    select to_json(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id) as "user",
            case when ciw.workgroup_id is null then '{}' else array_agg(json_build_object(
                'roles', ciw.roles,
                'workgroup', find_workgroup_by_id_simple(ciw.workgroup_id)
            )) end as "workgroups"
        from citizen as z
        left join citizen_in_workgroup ciw on z.id = ciw.citizen_id
        where z.user_id = _user_id
        group by z.id, ciw.workgroup_id
    ) as t;
end;
$$;

