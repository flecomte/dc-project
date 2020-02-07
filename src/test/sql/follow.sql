do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    created_user2    json  := '{"username": "john", "plain_password": "qwerty", "roles": ["ROLE_USER"]}';
    _citizen_id      uuid;
    _citizen_id2     uuid;
    created_citizen json := $json$
    {
      "name": {
        "first_name": "George",
        "last_name": "MICHEL"
      },
      "birthday": "2001-01-01",
      "email":"george.michel@gmail.com"
    }
    $json$;
    created_citizen2 json := $json$
    {
      "name": {
        "first_name": "John",
        "last_name": "Doe"
      },
      "birthday": "2002-01-01",
      "email":"george.michel2@gmail.com"
    }
    $json$;
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    select insert_user(created_user2) into created_user2;
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', created_user->>'id'), true)::json;
    created_citizen2 := jsonb_set(created_citizen2::jsonb, '{user}'::text[], jsonb_build_object('id', created_user2->>'id'), true)::json;

    -- insert new citizen for context
    select upsert_citizen(created_citizen) into created_citizen;
    _citizen_id := created_citizen->>'id';
    -- insert new citizen for context
    select upsert_citizen(created_citizen2) into created_citizen2;
    _citizen_id2 := created_citizen2->>'id';


    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) > 0 from follow), 'follow must be inserted';

    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) > 0 from follow), 'follow must be inserted';

    perform unfollow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 0 from follow), 'follow must be deleted after unfollow';

    -- delete follow and context
    delete from citizen;
    delete from "user";

    raise notice 'follow test pass';
end;
$$;


-- select uuid_generate_v4();