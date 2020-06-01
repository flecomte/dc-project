create or replace function update_workgroup_members(in _id uuid, inout members json)
    language plpgsql as
$$
begin
    perform add_workgroup_member(_id, b)
    from json_array_elements(members) b;

    delete from citizen_in_workgroup
    where workgroup_id = _id
      and citizen_id not in(
        select
            (b#>>'{citizen, id}')::uuid
        from json_array_elements(members) b
        where (b#>>'{citizen, id}')::uuid is not null
      );

    select find_workgroup_members(_id) into members;
end;
$$;

