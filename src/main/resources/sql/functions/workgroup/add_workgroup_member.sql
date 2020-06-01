create or replace function add_workgroup_member(in _id uuid, inout member json)
    language plpgsql as
$$
begin
    insert into citizen_in_workgroup (workgroup_id, citizen_id, roles)
    values (
       _id,
       (member#>>'{citizen, id}')::uuid,
       (select array_agg(t) from json_array_elements_text(member#>'{roles}') t)
    )
    on conflict do nothing;
end;
$$;
