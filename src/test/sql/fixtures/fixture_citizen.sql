create or replace function fixture_citizen(in name text default 'george', out _citizen_id uuid)
    language plpgsql as
$$
declare
    _user_id uuid;
    created_citizen json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01", "email":"george.michel@gmail.com"}';
    created_citizen2 json := '{"name": {"first_name":"john", "last_name":"DOE"}, "birthday": "2001-01-01", "email":"john.doe@gmail.com"}';
begin
    _user_id = fixture_user(name);
    if (name = 'george') then
        created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
        assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

        select upsert_citizen(created_citizen) into created_citizen;
        _citizen_id := created_citizen->>'id';
    elseif (name = 'john') then
        created_citizen2 := jsonb_set(created_citizen2::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
        assert created_citizen2#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen2#>>'{user, id}', _user_id::text);

        select upsert_citizen(created_citizen2) into created_citizen2;
        _citizen_id := created_citizen2->>'id';
    end if;
end
$$