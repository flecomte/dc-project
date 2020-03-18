create or replace function update_workgroup_members(in _id uuid, inout resource json)
    language plpgsql as
$$
begin
    insert into citizen_in_workgroup (citizen_id, workgroup_id)
    select
        (z->>'id')::uuid,
        _id::uuid
    from json_array_elements(resource) z
    where (z->>'id') is not null
    on conflict do nothing;

    delete from citizen_in_workgroup
    where workgroup_id = _id
      and citizen_id not in(
        select
            (z->>'id')::uuid
        from json_array_elements(resource) z
        where (z->>'id') is not null
      );

    select find_workgroup_members(_id) into resource;
end;
$$;

