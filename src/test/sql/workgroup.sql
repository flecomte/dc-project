do
$$
declare
    _citizen_id      uuid := fixture_citizen('george');
    _citizen_id2     uuid := fixture_citizen('john');
    _citizen_id3     uuid := fixture_citizen('tesla');
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
    members json;
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

    -------------
    -- members --
    -------------


    -- add
    select m into members from add_workgroup_members((created_workgroup->>'id')::uuid, json_build_array(
        json_build_object('id', _citizen_id2),
        json_build_object('id', _citizen_id3)
    )) m;

    assert json_array_length(members) = 2, 'The members count must be equal to 2';
    assert members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id3)),
        'Members must contain citizen3';

    -- update
    select m into members from update_workgroup_members((created_workgroup->>'id')::uuid, json_build_array(
            json_build_object('id', _citizen_id2),
            json_build_object('id', _citizen_id)
        )) m;
    assert json_array_length(members) = 2, 'The members count must be equal to 2';
    assert members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id)),
        'Members must contain citizen2';
    assert not members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id3)),
        'Members must NOT contain citizen3';

    -- remove
    select m into members from remove_workgroup_members((created_workgroup->>'id')::uuid, json_build_array(
            json_build_object('id', _citizen_id2)
        )) m;
    assert json_array_length(members) = 1, 'The members count must be equal to 1';
    assert members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id)),
        'Members must contain citizen1';
    assert not members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id2)),
        'Members must NOT contain citizen2';

    select m into members from find_workgroup_members((created_workgroup->>'id')::uuid) m;
    assert json_array_length(members) = 1, 'The members count must be equal to 1';
    assert members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id)),
        'Members must contain citizen1';
    assert not members::jsonb @> jsonb_build_array(jsonb_build_object('id', _citizen_id2)),
        'Members must NOT contain citizen2';

    rollback;
    raise notice 'workgroup test pass';
end
$$;



-- select w->>'id' from json_array_elements('[{"id":"plop"}]') w