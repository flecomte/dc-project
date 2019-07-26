do
$$
declare
    wrong_citizen    json;
    created_user     json  := '{"username": "george", "plain_password": "azerty"}';
    _user_id         uuid;
    created_citizen  json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01"}';
    selected_citizen json;
begin
    -- insert user for context
    call insert_user(created_user);
    _user_id := created_user->>'id';
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
    assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

    -- insert new citizen
    call upsert_citizen(created_citizen);
    assert created_citizen->>'birthday' = '2001-01-01'::text, format('birthday of inserted citizen must be the same of the original object, %s != %s', created_citizen->>'birthday', '2001-01-01'::text);

    -- insert citizen without first name and test if throw exception
    wrong_citizen := (created_citizen::jsonb - '{name, first_name}'::text[])::json;
    begin
        call upsert_citizen(wrong_citizen);
        assert false, 'upsert_citizen must be throw exception if first_name not exist';
    exception when not_null_violation then
    end;

    -- get citizen by id and check the first name
    call find_citizen_by_id((created_citizen->>'id')::uuid, selected_citizen);
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    -- get citizen by user id and check the first name
    call find_citizen_by_user_id((created_citizen->>'user_id')::uuid, selected_citizen);
    assert selected_citizen#>>'{name, first_name}' = 'George', format('first name must be George, %s', selected_citizen#>>'{name, first_name}');

    -- delete citizen
    delete from citizen where user_id = _user_id;
    delete from "user" where username = 'george';

    -- check if fint by id return null if citizen not exist
    call find_citizen_by_user_id((created_citizen->>'user_id')::uuid, selected_citizen);
    assert selected_citizen is null, format('citizen must be null if not exist, %s', selected_citizen);

    raise notice 'citizen test pass';
end;
$$;
