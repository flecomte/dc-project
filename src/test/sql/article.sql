do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    _user_id         uuid;
    _citizen_id      uuid;
    created_citizen  json := '{"name": {"first_name":"George", "last_name":"MICHEL"}, "birthday": "2001-01-01", "email":"george.michel@gmail.com"}';
    created_article  json := '{"version_id":"933b6a1b-50c9-42b6-989f-c02a57814ef9", "title": "Love the world", "anonymous": false, "content": "bla bal bla", "tags": ["love", "test"], "draft":false}';
    created_article_v2 json;
    first_article_id uuid;
    second_article_id uuid;
    selected_article json;
    selected_total   int;
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    _user_id := created_user->>'id';
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', _user_id::text), true)::json;
    assert created_citizen#>>'{user, id}' = _user_id::text, format('userId in citizen must be the same as user, %s = %s', created_citizen#>>'{user, id}', _user_id::text);

    -- insert new citizen for context
    select upsert_citizen(created_citizen) into created_citizen;
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);

    -- upsert article
    select upsert_article(created_article) into created_article;
    assert created_article->>'version_id' is not null, 'version_id should not be null';
    assert (created_article->>'version_number')::int = 1, format('version_number must be equal to 1, %s instead', created_article->>'version_number');
    assert (created_article->>'last_version')::bool = true, 'The first insert must be set to the last version';
    first_article_id = (created_article->>'id')::uuid;

    -- try to create new version
    select upsert_article(created_article) into created_article_v2;
    assert (created_article_v2->>'version_number')::int = 2, format('version_number must be equal to 2, %s instead', created_article_v2->>'version_number');
    assert (created_article_v2->>'last_version')::bool = true, 'The second insert must be set to the last version';
    second_article_id = (created_article_v2->>'id')::uuid;

    -- get articles versions by version_id
    select resource, total into selected_article, selected_total from find_articles_versions_by_version_id((created_article->>'version_id')::uuid);
    assert selected_article#>>'{0,title}' = 'Love the world', format('title must be "Love the world", %s', selected_article#>>'{0,title}');
    assert (selected_article#>>'{0,version_number}')::int = 2, format('version_id must be 2, %s instead', selected_article#>>'{0,version_number}');
    assert selected_total = 2, format('the total must be 2, %s instead', selected_total);

    -- get articles versions by id
    select resource, total into selected_article, selected_total from find_articles_versions_by_id((created_article->>'id')::uuid);
    assert selected_article#>>'{0,title}' = 'Love the world', format('title must be "Love the world", %s', selected_article#>>'{0,title}');
    assert (selected_article#>>'{0,version_number}')::int = 2, format('version_id must be 2, %s instead', selected_article#>>'{0,version_number}');
    assert selected_total = 2, format('the total must be 2, %s instead', selected_total);

    -- get article by id and check the title
    select find_article_by_id((created_article_v2->>'id')::uuid) into selected_article;
    assert selected_article->>'title' = 'Love the world', format('title must be "Love the world", %s', selected_article->>'title');

    -- get article by version_id and check the title
    select find_last_article_by_version_id((created_article_v2->>'version_id')::uuid) into selected_article;
    assert selected_article->>'title' = 'Love the world', format('title must be "Love the world", %s', selected_article->>'title');
    assert (selected_article->>'version_number')::int = 2, format('version_id must be 2, %s instead', selected_article->>'version_number');

    -- update to draft, then the last_version column must be change
    update article
    set draft = true
    where id = second_article_id;

    select find_last_article_by_version_id((created_article_v2->>'version_id')::uuid) into selected_article;
    assert (selected_article->>'version_number')::int = 1, format('version_id must be 1, %s instead', selected_article->>'version_number');

    update article
    set draft = false
    where id = second_article_id;

    select find_last_article_by_version_id((created_article_v2->>'version_id')::uuid) into selected_article;
    assert (selected_article->>'version_number')::int = 2, format('version_id must be 2, %s instead', selected_article->>'version_number');

--     -- check if user id is returned
--     assert (selected_article#>>'{created_by, user, id}')::uuid = _user_id, format('user_id must be %s instead of %s', _user_id, (selected_article#>>'{created_by, user, id}')::uuid);

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
