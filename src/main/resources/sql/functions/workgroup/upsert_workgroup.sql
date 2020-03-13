create or replace function upsert_workgroup(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid = coalesce((resource->>'id')::uuid, uuid_generate_v4());
begin
    insert into workgroup (id, created_by_id, name, description, anonymous, logo, owner_id)
    select
        new_id,
        (resource#>>'{created_by, id}')::uuid,
        name,
        description,
        anonymous,
        logo,
        (resource#>>'{owner, id}')::uuid
    from json_populate_record(null::workgroup, resource)
    on conflict (id) do update set
        name = excluded.name,
        description = excluded.description,
        anonymous = excluded.anonymous,
        logo = excluded.logo,
        owner_id = excluded.owner_id;

--     insert into citizen_in_workgroup (citizen_id, workgroup_id)
--     select
--         (resource->>'id')::uuid,
--         new_id::uuid
--     from json_populate_recordset(null::workgroup, resource->'members');

    select find_workgroup_by_id(new_id) into resource;
end;
$$;

-- drop procedure if exists upsert_workgroup(inout json);