create or replace function upsert_workgroup(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid = coalesce((resource->>'id')::uuid, uuid_generate_v4());
    exists boolean = case when (select true from workgroup where id = new_id) is null then false else true end;
begin
    insert into workgroup (id, created_by_id, name, description, anonymous, logo)
    select
        new_id,
        (resource#>>'{created_by, id}')::uuid,
        name,
        description,
        anonymous,
        logo
    from json_populate_record(null::workgroup, resource)
    on conflict (id) do update set
        name = excluded.name,
        description = excluded.description,
        anonymous = excluded.anonymous,
        logo = excluded.logo;

    -- remove old members
    delete from citizen_in_workgroup cw
    where cw.workgroup_id = new_id
      and cw.citizen_id not in (
          select (m#>>'{citizen,id}')::uuid
          from json_array_elements(resource->'members') m
      );

    -- insert new members
    insert into citizen_in_workgroup (workgroup_id, citizen_id, roles)
    select
        new_id::uuid,
        (m#>>'{citizen,id}')::uuid,
        json_to_array(m#>'{roles}')
    from json_array_elements(resource->'members') m
    on conflict do nothing;

    -- insert master if no members
    if (exists = false) then
        insert into citizen_in_workgroup (workgroup_id, citizen_id, roles)
        select
            new_id::uuid,
            (resource#>>'{created_by, id}')::uuid,
            '{MASTER}'
        on conflict do nothing;
    end if;

    select find_workgroup_by_id(new_id) into resource;
end;
$$;

