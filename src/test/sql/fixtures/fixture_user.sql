create or replace function fixture_user(in name text default 'george', out user_id uuid)
    language plpgsql as
$$
declare
    created_user1 json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    created_user2 json  := '{"username": "john", "plain_password": "qwerty", "roles": ["ROLE_USER"]}';
begin
    if (name = 'george') then
        select insert_user(created_user1) into created_user1;
        user_id := created_user1->>'id';
    elseif (name = 'john') then
        select insert_user(created_user2) into created_user2;
        user_id := created_user2->>'id';
    end if;
end
$$