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
    opinion1 uuid = uuid_generate_v4();
    opinion2 uuid = uuid_generate_v4();
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


    insert into opinion_list(id, name, target)
    values (opinion1, 'Opinion1', '{article}');

    insert into opinion_list(id, name, target)
    values (opinion2, 'Opinion2', '{article}');

    insert into opinion_list(name, target)
    values ('Opinion3', '{article}');

    perform opinion(
        reference => 'article'::regclass,
        _target_id => (created_article->>'id')::uuid,
        _created_by_id => _citizen_id,
        _opinion => opinion1
    );
    assert (select count(*) = 1 from opinion_on_article), 'opinion must be inserted';
    assert (select opinion = opinion1 from opinion_on_article limit 1), 'opinion must be inserted';

    assert(select (a#>>'{opinions, Opinion1}')::int = 1
    from find_article_by_id((created_article->>'id')::uuid) a), 'the article must be have a opinion';

    assert(
        select (o#>>'{0, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_id(_citizen_id, (created_article->>'id')::uuid) o),
            'The opinion must have a name';

    assert(
        select (o#>>'{0, name}') = 'Opinion1'
        from find_citizen_opinions_by_target_ids(_citizen_id, array[(created_article->>'id')::uuid]) o),
            'The first opinion must have a name';

    assert(
        select find_opinion_choices()#>>'{0, name}' = 'Opinion1'
        ), 'find_opinion_choices mst be return all opinions';

    assert(
        select (find_opinion_choice_by_id(opinion1)->>'name') = 'Opinion1'
        ), 'find_opinion_choice_by_id must return the opinion_choice';

    -- delete vote and context
    delete from opinion;
    delete from opinion_list;
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'opinion test pass';
end
$$;
