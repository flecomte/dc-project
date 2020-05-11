create or replace function find_workgroup_by_id_simple(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(t)
    from (
        select
            w.*,
            json_build_object('id', w.created_by_id) as created_by,
            json_build_object('id', w.owner_id) as owner
        into resource
        from workgroup as w
        join citizen_in_workgroup ciw on w.id = ciw.workgroup_id
        where w.id = _id
         and deleted_at is null
        group by w.id
    ) as t;
end;
$$;
