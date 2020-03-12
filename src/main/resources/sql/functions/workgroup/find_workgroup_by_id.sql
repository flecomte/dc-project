create or replace function find_workgroup_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            w.*,
            find_citizen_by_id(w.created_by_id) as created_by,
            find_citizen_by_id(w.owner_id) as owner
        into resource
        from workgroup as w
        left join citizen_in_workgroup ciw on w.id = ciw.workgroup_id
        where w.id = _id
         and deleted_at is null
    ) as t;
end;
$$;

-- drop function if exists find_workgroup_by_id(uuid, out json);
-- select * from find_workgroup_by_id('d011ad4c-fa1b-40a3-593b-7816479ff33b')
