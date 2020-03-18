create or replace function add_workgroup_members(in _id uuid, inout resource json)
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

    select find_workgroup_members(_id) into resource;
end;
$$;


