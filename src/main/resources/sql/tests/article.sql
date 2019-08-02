do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty"}';
    _user_id         uuid;
    _citizen_id      uuid;
    created_citizen  json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01"}';
    created_article  json := '{"version_id":"933b6a1b-50c9-42b6-989f-c02a57814ef9", "title": "Love the world", "annonymous": false, "content": "bla bal bla", "tags": ["love", "test"]}';
    selected_article json;
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    _user_id := created_user->>'id';
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
    assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

    -- insert new citizen for context
    call upsert_citizen(created_citizen);
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);

    -- upsert article
    call upsert_article(created_article);
    assert created_article->>'version_id' is not null, 'version_id should not be null';
    assert (created_article->>'version_number')::int = 1, format('version_number must be equal to 1, %s instead', created_article->>'version_number');
    -- try tu create new version
    call upsert_article(created_article);
    assert (created_article->>'version_number')::int = 2, format('version_number must be equal to 2, %s instead', created_article->>'version_number');

    -- get article by id and check the title
    select find_article_by_id((created_article->>'id')::uuid) into selected_article;
    assert selected_article->>'title' = 'Love the world', format('title must be "Love the world", %s', selected_article->>'title');

    -- get article by version_id and check the title
    select find_last_article_by_version_id((created_article->>'version_id')::uuid) into selected_article;
    assert selected_article->>'title' = 'Love the world', format('title must be "Love the world", %s', selected_article->>'title');
    assert (selected_article->>'version_number')::int = 2, format('version_id must be 2, %s instead', selected_article->>'version_number');
    -- check if user id is returned
    assert (selected_article#>>'{created_by, user, id}')::uuid = _user_id, format('user_id must be %s instead of %s', _user_id, (selected_article#>>'{created_by, user, id}')::uuid);

    -- delete article and context
    delete from article;
    delete from citizen;
    delete from "user";

    -- check if find by id return null if article not exist
    select find_citizen_by_user_id((created_citizen->>'id')::uuid) into selected_article;
    assert selected_article is null, format('article must be null if not exist, %s', selected_article);

    raise notice 'article test pass';
end;
$$;
