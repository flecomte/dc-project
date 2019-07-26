do
$$
declare
    created_user json := '{"username": "george", "plain_password": "azerty"}';
    selected_user json;
    exist_user json;
begin
    -- Insert user and check if username and password is correct
    call insert_user(created_user);
    assert created_user->>'username' = 'george', 'username must be george';
    assert created_user->>'password' is not null, 'password must be generated';

    -- get user by there id and check the username is correct
    call find_user_by_id((created_user->>'id')::uuid, selected_user);
    assert selected_user->>'username' = 'george', 'username must be george';

    -- get user by username and check the username is correct
    call find_user_by_username(created_user->>'username', selected_user);
    assert selected_user->>'username' = 'george', 'username must be george';

    -- check if user exist with username and password and verify the reterned user
    select check_user('george', 'azerty') into exist_user;
    assert exist_user is not null, format('the function check_user must be return user object if username and password is correct, %s is return', exist_user::text);
    assert exist_user->>'username' = 'george', format('the function check_user must be return user object with username is "george", %s is return', exist_user::text);
    assert exist_user->>'password' is null, format('the function check_user must not be return the password, %s is return', exist_user::text);

    -- delete user and check if user is really not exists
    delete from "user" where username = 'george';
    select check_user('george', 'azerty') into exist_user;
    assert exist_user is null, format('the function check_user must be return null if user not exist, %s is return', exist_user::text);

    raise notice 'user test pass';
end;
$$;

