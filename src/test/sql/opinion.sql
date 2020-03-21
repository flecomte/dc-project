do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    created_user2    json  := '{"username": "george2", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
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
        "first_name": "George2",
        "last_name": "MICHEL2"
      },
      "birthday": "2001-01-02",
      "email":"george.michel2@gmail.com"
    }
    $json$;
    created_article  json := $json$
    {
      "version_id": "933b6a1b-50c9-42b6-989f-c02a57814ef9",
      "title": "Love the world",
      "anonymous": false,
      "content": "bla bal bla",
      "tags": [
        "love",
        "test"
      ],
      "draft":false
    }
    $json$;
    opinion_choice1_id uuid = uuid_generate_v4();
    opinion_choice2_id uuid = uuid_generate_v4();
    opinion2 json;
    _opinions json;
    _opinions_deleted_ids uuid[];
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    select insert_user(created_user2) into created_user2;
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', created_user->>'id'), true)::json;
    created_citizen2 := jsonb_set(created_citizen2::jsonb, '{user}'::text[], jsonb_build_object('id', created_user2->>'id'), true)::json;

    -- insert new citizen for context
    select upsert_citizen(created_citizen) into created_citizen;
    select upsert_citizen(created_citizen2) into created_citizen2;
    _citizen_id := created_citizen->>'id';
    _citizen_id2 := created_citizen2->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);
    -- upsert article
    select upsert_article(created_article) into created_article;

    select (h->>'id')::uuid into opinion_choice1_id from upsert_opinion_choice('{"name": "Opinion1", "target":["article"]}') h;
    assert opinion_choice1_id is not null, 'Opinion choice must be return json with id';
    select (h->>'id')::uuid into opinion_choice2_id from upsert_opinion_choice('{"name": "Opinion2"}') h;
    perform upsert_opinion_choice('{"name": "Opinion3", "target":["article"]}') h;

    perform upsert_opinion(
        resource => json_build_object(
            'target', json_build_object('id', (created_article->'id'), 'reference', 'article'),
            'created_by', json_build_object('id', _citizen_id),
            'choice', json_build_object('id', opinion_choice1_id)
        )
    );
    select upsert_opinion(
        resource => json_build_object(
            'target', json_build_object('id', (created_article->'id'), 'reference', 'article'),
            'created_by', json_build_object('id', _citizen_id),
            'choice', json_build_object('id', opinion_choice2_id)
        )
    ) into opinion2;
    assert (select count(*) = 2 from opinion_on_article), 'opinions must be inserted';
    assert (select choice_id = opinion_choice1_id from opinion_on_article limit 1), 'opinion must be inserted';

    assert(select (a#>>'{opinions, Opinion1}')::int = 1
    from find_article_by_id((created_article->>'id')::uuid) a), 'the article must be have a opinion';

    assert(select (opinion2#>>'{choice, id}')::uuid = opinion_choice2_id), 'opinion2 is not inserted';
    assert(select (opinion2#>>'{choice, name}') = 'Opinion2'), 'no name for opinion2';

    assert(
        select (o#>>'{0, choice, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_id(_citizen_id, (created_article->>'id')::uuid) o),
            'The opinion must have a name';

    assert(
        select (o#>>'{0, choice, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_ids(_citizen_id, array[(created_article->>'id')::uuid]) o),
            'The first opinion must have a name';

    assert(
        select find_opinion_choices()#>>'{0, name}' = 'Opinion1'
        ), 'find_opinion_choices must be return all opinions';

    assert(
        select find_opinion_choices('{}')#>>'{0, name}' = 'Opinion1'
        ), 'find_opinion_choices must be return all opinions if no target is defined';

    assert(
        select (find_opinion_choice_by_id(opinion_choice1_id)->>'name') = 'Opinion1'
        ), 'find_opinion_choice_by_id must return the opinion_choice';

    assert(
        select json_array_length(resource) = 1 from find_citizen_opinions(_citizen_id, null, null, 1, 1)
    ), 'find_citizen_opinions must return only 1 result if limit is set to 1';

    assert(
        select total = 2 from find_citizen_opinions(_citizen_id, null, null, 2, 1)
    ), 'find_citizen_opinions must return the total and it should be 2';

    assert(
        select (resource#>>'{0, choice, name}') = 'Opinion1' from find_citizen_opinions(_citizen_id, null, null, 1, 0)
    ), 'find_citizen_opinions must return a list of opinion with name';

    -- test update_citizen_opinions_by_target_id
    select opinions into _opinions
    from update_citizen_opinions_by_target_id(
            array[opinion_choice1_id]::uuid[],
        _citizen_id,
        (created_article->>'id')::uuid,
        'article'
    );
    assert (json_array_length(_opinions) = 1), format('Opinions updated must be count of 1. instead of: %s', json_array_length(_opinions));
    assert(select (_opinions#>>'{0, choice, id}')::uuid = opinion_choice1_id), 'opinion1 is not inserted';
    assert(
        select (o#>>'{0, choice, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_id(_citizen_id, (created_article->>'id')::uuid) o),
        'The opinion must have a name';

    -- test update_citizen_opinions_by_target_id with multiple ids
    select opinions into _opinions
    from update_citizen_opinions_by_target_id(
            array[opinion_choice1_id, opinion_choice2_id]::uuid[],
        _citizen_id,
        (created_article->>'id')::uuid,
        'article'
    );
    assert (json_array_length(_opinions) = 2), format('(on multi update) Opinions updated must be count of 1. instead of: %s', json_array_length(_opinions));
    assert(select (_opinions#>>'{0, choice, id}')::uuid = opinion_choice1_id), '(on multi update) opinion1 is not inserted';
    assert(select (_opinions#>>'{1, choice, id}')::uuid = opinion_choice2_id), '(on multi update) opinion2 is not inserted';
    assert(
        select (o#>>'{0, choice, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_id(_citizen_id, (created_article->>'id')::uuid) o),
        '(on multi update) The opinion must have a name';

    -- test update_citizen_opinions_by_target_id if empty
    select opinions, ids_deleted into _opinions, _opinions_deleted_ids
    from update_citizen_opinions_by_target_id(
            '{}'::uuid[],
        _citizen_id,
        (created_article->>'id')::uuid,
        'article'
    );
    assert json_array_length(_opinions) = 0;
    rollback;
    raise notice 'opinion test pass';
end
$$;
