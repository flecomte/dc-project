do
$$
declare
    wrong_citizen              json;
    _user_id                   uuid := fixture_user();
    created_citizen            json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01", "email":"george.michel@gmail.com"}';
    created_citizen_with_user  json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01", "email":"george.michel2@gmail.com", "user":{"username": "george junior", "password": "azerty", "roles": ["ROLE_USER"]}}';
    selected_citizen           json;
begin
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
    assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

    -- insert new citizen
    select upsert_citizen(created_citizen) into created_citizen;
    assert created_citizen->>'birthday' = '2001-01-01'::text, format('birthday of inserted citizen must be the same of the original object, %s != %s', created_citizen->>'birthday', '2001-01-01'::text);

    -- insert new citizen
    select insert_citizen_with_user(created_citizen_with_user) into created_citizen_with_user;
    assert created_citizen_with_user->>'birthday' = '2001-01-01'::text, format('birthday of inserted citizen must be the same of the original object, %s != %s', created_citizen->>'birthday', '2001-01-01'::text);
    assert created_citizen_with_user#>>'{user, username}' = 'george junior', 'username must be george';

    -- insert citizen without first name and test if throw exception
    wrong_citizen := (created_citizen::jsonb - '{name, first_name}'::text[])::json;
    begin
        select upsert_citizen(wrong_citizen) into wrong_citizen;
        assert false, 'upsert_citizen must be throw exception if first_name not exist';
    exception when not_null_violation then
    end;

    -- get citizen by id and check the first name
    select find_citizen_by_id((created_citizen->>'id')::uuid) into selected_citizen;
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    -- get citizen by user id and check the first name
    select find_citizen_by_user_id((created_citizen->>'user_id')::uuid) into selected_citizen;
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    -- get citizen by username and check the first name
    select find_citizen_by_username(created_citizen#>>'{user, username}') into selected_citizen;
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    -- get citizen by name and check the first name
    select find_citizen_by_name(created_citizen->'name') into selected_citizen;
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    rollback;

    -- check if find by id return null if citizen not exist
    select find_citizen_by_user_id((created_citizen->>'user_id')::uuid) into selected_citizen;
    assert selected_citizen is null, format('citizen must be null if not exist, %s', selected_citizen);

    raise notice 'citizen test pass';
end;
$$;
