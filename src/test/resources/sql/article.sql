do
$$
declare
    _citizen_id      uuid := fixture_citizen();
    _workgroup_id uuid := fixture_workgroup(_citizen_id => _citizen_id);
    created_article  json := '{"version_id":"933b6a1b-50c9-42b6-989f-c02a57814ef9", "title": "Love the world", "anonymous": false, "content": "bla bal bla", "tags": ["love", "test"], "draft":false}';
    created_article_grouped  json := '{"version_id":"35f5c4c3-0629-4405-9956-12557020c9f5", "title": "Love my group", "anonymous": false, "content": "groupy groupy", "tags": ["love", "group"], "draft":false}';
    created_article_v2 json;
    first_article_id uuid;
    second_article_id uuid;
    selected_article json;
    selected_total   int;
begin
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

    created_article_grouped := jsonb_set(created_article_grouped::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    created_article_grouped := jsonb_set(created_article_grouped::jsonb, '{workgroup}'::text[], jsonb_build_object('id', _workgroup_id::text), true)::json;
    select upsert_article(created_article_grouped) into created_article_grouped;

    -- get articles versions by version_id
    select resource, total into selected_article, selected_total from find_articles_versions_by_version_id((created_article->>'version_id')::uuid);
    assert selected_article#>>'{0,title}' = 'Love the world', format('title must be "Love the world", %s', selected_article#>>'{0,title}');
    assert (selected_article#>>'{0,version_number}')::int = 2, format('version_id must be 2, %s instead', selected_article#>>'{0,version_number}');
    assert selected_total = 2, format('the total must be 2, %s instead', selected_total);

    -- get articles by workgroup id
    select resource, total into selected_article, selected_total from find_articles(_filter => json_build_object('workgroup_id', _workgroup_id));
    assert selected_article#>>'{0,title}' = 'Love my group', format('title must be "Love my group", %s', selected_article#>>'{0,title}');
    assert selected_total = 1, format('the total must be 1, %s instead', selected_total);

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

    rollback;
    raise notice 'article test pass';
end
$$;
