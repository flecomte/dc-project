do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty"}';
    _user_id         uuid;
    _citizen_id      uuid;
    created_citizen  json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01"}';
    created_article  json := '{"version_id":"933b6a1b-50c9-42b6-989f-c02a57814ef9", "title": "Love the world", "annonymous": false, "content": "bla bal bla", "tags": ["love", "test"]}';
    created_constitution json := '{"version_id":"18ff6dd6-3bc1-4c59-82f0-5e2a8d54ae3e", "title": "Love the world", "annonymous": false, "titles": [{"name":"titleOne"},{"name":"titleTwo"}]}';
begin
    -- insert user for context
    call insert_user(created_user);
    _user_id := created_user->>'id';
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
    assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

    -- insert new citizen for context
    call upsert_citizen(created_citizen);
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);

    -- upsert article for context
    call upsert_article(created_article);
    assert created_article->>'version_id' is not null, 'version_id should not be null';


    -- create new constitution
    created_constitution := jsonb_set(created_constitution::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    call upsert_constitution(created_constitution);
    assert (created_constitution->>'version_number')::int = 1, format('version_number must be equal to 1, %s instead', created_constitution->>'version_number');
    assert created_constitution#>>'{titles, 0, name}' = 'titleOne'::text, format('the name of the first title of contitution must be %s, not %s', 'titleOne', created_constitution#>>'{titles, 0, name}');

    -- delete article and context
    delete from title;
    delete from constitution;
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'constitution test pass';
end;
$$;


-- select uuid_generate_v4();