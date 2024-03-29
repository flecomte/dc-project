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
    created_workgroup_updated  json := '{
        "name": "Le groupe des rouge",
        "description": "red",
        "anonymous": false
    }';
    created_workgroup_2  json := '{
        "name": "hello",
        "description": "super",
        "anonymous": false
    }';
    selected_workgroup json;
--     selected_workgroups json;
    members json;
    selected_citizen json;
begin
    created_workgroup := jsonb_set(created_workgroup::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_workgroup#>>'{created_by, id}' = _citizen_id::text, format('citizenId in workgroup must be the same as citizen, %s != %s', created_workgroup#>>'{created_by, id}', _citizen_id::text);

    -- insert workgroup
    select upsert_workgroup(created_workgroup) into created_workgroup;
    assert created_workgroup->>'description' is not null, 'description should not be null';
    assert (created_workgroup->>'name') = 'Le groupe des vert', format('name must be equal to "Le groupe des vert", %s instead', created_workgroup->>'name');
    assert (created_workgroup#>>'{members, 0, citizen, id}') = _citizen_id::text, 'workgroup must have creator in members on creation';

    -- update workgroup
    created_workgroup_updated := jsonb_set(created_workgroup_updated::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    select upsert_workgroup(created_workgroup_updated) into created_workgroup_updated;
    assert created_workgroup_updated->>'description' is not null, 'description should not be null';
    assert (created_workgroup_updated->>'name') = 'Le groupe des rouge', format('name must be equal to "Le groupe des rouge", %s instead', created_workgroup_updated->>'name');
    assert (created_workgroup_updated#>>'{members, 0, citizen, id}') = _citizen_id::text, 'workgroup must have creator in members on update';

    -- insert another workgroup
    created_workgroup_2 := jsonb_set(created_workgroup_2::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
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
        json_build_object('citizen', json_build_object('id', _citizen_id2), 'roles', '{MASTER}'::text[]),
        json_build_object('citizen', json_build_object('id', _citizen_id3), 'roles', '{MASTER}'::text[])
    )) m;

    assert json_array_length(members) = 3, 'The members count must be equal to 3';
    assert (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'id', (created_workgroup->>'id'),
            'citizen', jsonb_build_object('id', _citizen_id3),
            'roles', jsonb_build_array('MASTER')
        )),
        'Members must contain citizen3';

--     select resource into selected_workgroups
--     from find_workgroups(_filter := json_build_object('members', json_build_array(_citizen_id2)));
--     assert (select selected_workgroups#>>'{0,id}' = (created_workgroup->>'id'));

    -- Check if "find_citizen_by_id" retrun citizen
    assert (select find_citizen_by_id(_citizen_id2)#>>'{id}')::uuid = _citizen_id2, 'find_citizen_by_id must return citizen';

    -- Check if "find_citizen_by_id_with_user_and_workgroups" retrun workgroups of citizen
    select find_citizen_by_id_with_user_and_workgroups(_citizen_id3) into selected_citizen;
    assert selected_citizen#>>'{workgroups, 0, roles, 0}' = 'MASTER', format('workgroup must have MASTER role, %s', selected_citizen#>>'{workgroups, 0, roles, 0}');

    -- update
    select m into members from update_workgroup_members((created_workgroup->>'id')::uuid, json_build_array(
            json_build_object('citizen', json_build_object('id', _citizen_id2), 'roles', '{MASTER}'::text[]),
            json_build_object('citizen', json_build_object('id', _citizen_id), 'roles', '{MASTER}'::text[])
        )) m;
    assert json_array_length(members) = 2, 'The members count must be equal to 2';
    assert (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'id', (created_workgroup->>'id'),
            'citizen', jsonb_build_object('id', _citizen_id),
            'roles', jsonb_build_array('MASTER')
        )), 'Members must contain citizen1';
    assert not (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'citizen', jsonb_build_object('id', _citizen_id3)
        )), 'Members must NOT contain citizen3';

    -- remove
    select m into members from remove_workgroup_members((created_workgroup->>'id')::uuid, json_build_array(jsonb_build_object(
            'citizen', json_build_object('id', _citizen_id2)
        ))) m;
    assert json_array_length(members) = 1, 'The members count must be equal to 1';
    assert (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'citizen', jsonb_build_object('id', _citizen_id)
        )), 'Members must contain citizen1';
    assert not (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'citizen', jsonb_build_object('id', _citizen_id2)
        )), 'Members must NOT contain citizen2';

    select m into members from find_workgroup_members((created_workgroup->>'id')::uuid) m;
    assert json_array_length(members) = 1, 'The members count must be equal to 1';
    assert (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'citizen', jsonb_build_object('id', _citizen_id)
        )), 'Members must contain citizen1';
    assert not (members::jsonb) @> jsonb_build_array(jsonb_build_object(
            'citizen', jsonb_build_object('id', _citizen_id2)
        )), 'Members must NOT contain citizen2';

    -- Check if find_workgroup_by_id return members
    select find_workgroup_by_id((created_workgroup->>'id')::uuid) into selected_workgroup;
    assert json_array_length(selected_workgroup->'members') = 1, 'Workgroup must have members';

    perform delete_workgroup((created_workgroup->>'id')::uuid);
    select find_workgroup_by_id((created_workgroup->>'id')::uuid) into selected_workgroup;
    assert selected_workgroup is null, 'Workgroup must be null after deleted';
    select m into members from find_workgroup_members((created_workgroup->>'id')::uuid) m;
    assert json_array_length(members) = 0, 'The members count must be equal to 0 on deleted workgroup';

    rollback;
    raise notice 'workgroup test pass';
end
$$;
