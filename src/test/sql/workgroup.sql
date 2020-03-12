do
$$
declare
    _citizen_id      uuid := fixture_citizen();
    created_workgroup  json := '{
        "name": "Le groupe des vert",
        "description": "test",
        "anonymous": false
    }';
    created_workgroup_2  json := '{
        "name": "hello",
        "description": "super",
        "anonymous": false
    }';
    selected_workgroup json;
    selected_workgroup_2 json;
begin
    created_workgroup := jsonb_set(created_workgroup::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    created_workgroup := jsonb_set(created_workgroup::jsonb, '{owner}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_workgroup#>>'{created_by, id}' = _citizen_id::text, format('citizenId in workgroup must be the same as citizen, %s != %s', created_workgroup#>>'{created_by, id}', _citizen_id::text);

    -- upsert workgroup
    select upsert_workgroup(created_workgroup) into created_workgroup;
    assert created_workgroup->>'description' is not null, 'description should not be null';
    assert (created_workgroup->>'name') = 'Le groupe des vert', format('name must be equal to "Le groupe des vert", %s instead', created_workgroup->>'name');

    -- insert another workgroup
    created_workgroup_2 := jsonb_set(created_workgroup_2::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    created_workgroup_2 := jsonb_set(created_workgroup_2::jsonb, '{owner}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_workgroup_2#>>'{created_by, id}' = _citizen_id::text, format('citizenId in workgroup must be the same as citizen, %s != %s', created_workgroup_2#>>'{created_by, id}', _citizen_id::text);
    select upsert_workgroup(created_workgroup_2) into created_workgroup_2;

    -- get workgroup by id and check the name
    select find_workgroup_by_id((created_workgroup->>'id')::uuid) into selected_workgroup;
    assert selected_workgroup->>'name' = 'Le groupe des vert', format('name must be "Le groupe des vert", %s', selected_workgroup->>'name');

    -- search workgroups and check the name
    select (w.resource->0) into selected_workgroup from find_workgroups('Le groupe des vert', "limit" := 1) w;
    assert (selected_workgroup->>'name') = 'Le groupe des vert', format('name must be "Le groupe des vert" instead of : %s', (selected_workgroup->>'name'));

    rollback;
    raise notice 'workgroup test pass';
end
$$;
