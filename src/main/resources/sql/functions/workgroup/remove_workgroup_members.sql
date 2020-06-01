create or replace function remove_workgroup_members(in _id uuid, inout members json)
    language plpgsql as
$$
begin
    delete from citizen_in_workgroup
    where workgroup_id = _id
      and citizen_id in (
        select
            (b#>>'{citizen, id}')::uuid
        from json_array_elements(members) b
    );

    select find_workgroup_members(_id) into members;
end
$$;

