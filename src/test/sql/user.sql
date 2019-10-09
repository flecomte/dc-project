do
$$
declare
    created_user json := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    user_with_other_password json;
    selected_user json;
    exist_user json;
begin
    -- Insert user and check if username and password is correct
    select insert_user(created_user) into created_user;
    assert created_user->>'username' = 'george', 'username must be george';
    assert created_user->>'password' is null, 'password must not be returned';

    -- get user by there id and check the username is correct
    select find_user_by_id((created_user->>'id')::uuid) into selected_user;
    assert selected_user->>'username' = 'george', format('username must be george, %s instead', selected_user);

    -- get user by username and check the username is correct
    select find_user_by_username(created_user->>'username') into selected_user;
    assert selected_user->>'username' = 'george', 'username must be george';

    -- check if user exist with username and password and verify the reterned user
    select check_user('george', 'azerty') into exist_user;
    assert exist_user is not null, format('the function check_user must be return user object if username and password is correct, %s is return', exist_user::text);
    assert exist_user->>'username' = 'george', format('the function check_user must be return user object with username is "george", %s is return', exist_user::text);
    assert exist_user->>'password' is null, format('the function check_user must not be return the password, %s is return', exist_user::text);

    -- test change password
    user_with_other_password = jsonb_set(created_user::jsonb, '{plain_password}', '"qwerty"'::jsonb);
    perform change_user_password(user_with_other_password);
    select check_user('george', 'qwerty') into exist_user;
    assert exist_user->>'username' = 'george', format('the function change_user_password must change password: %s', exist_user::text);

    -- delete user and check if user is really not exists
    delete from "user" where username = 'george';
    select check_user('george', 'azerty') into exist_user;
    assert exist_user is null, format('the function check_user must be return null if user not exist, %s is return', exist_user::text);

    raise notice 'user test pass';
end;
$$;

