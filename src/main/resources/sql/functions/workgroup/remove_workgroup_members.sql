create or replace function remove_workgroup_members(in _id uuid, inout resource json)
    language plpgsql as
$$
begin
    delete from citizen_in_workgroup
    where workgroup_id = _id
      and citizen_id in (
        select
            (z->>'id')::uuid
        from json_array_elements(resource) z
    );

    select find_workgroup_members(_id) into resource;
end
$$;

-- drop procedure if exists remove_workgroup_members(in uuid, inout json);