create or replace function find_workgroup_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            w.*,
            find_citizen_by_id_with_user(w.created_by_id) as created_by,
            find_workgroup_members(w.id) as members
        into resource
        from workgroup as w
        where w.id = _id
         and deleted_at is null
    ) as t;
end;
$$;


-- select * from find_workgroup_by_id('d011ad4c-fa1b-40a3-593b-7816479ff33b')
