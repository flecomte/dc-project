create or replace function fixture_user(in name text default 'george', out user_id uuid)
    language plpgsql as
$$
declare
    created_user json;
begin
    if (name = 'george') then
        created_user = '{"username": "george", "password": "azerty", "roles": ["ROLE_USER"]}';
    elseif (name = 'john') then
        created_user = '{"username": "john", "password": "qwerty", "roles": ["ROLE_USER"]}';
    elseif (name = 'tesla') then
        created_user = '{"username": "tesla", "password": "azerty", "roles": ["ROLE_USER"]}';
    end if;

    select insert_user(created_user) into created_user;
    user_id := created_user->>'id';
end
$$;
