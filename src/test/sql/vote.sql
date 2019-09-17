do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    _citizen_id      uuid;
    created_citizen json := $json$
    {
      "name": {
        "first_name": "George",
        "last_name": "MICHEL"
      },
      "birthday": "2001-01-01"
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
    votes jsonb;
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', created_user->>'id'), true)::json;

    -- insert new citizen for context
    select upsert_citizen(created_citizen) into created_citizen;
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);
    -- upsert article
    select upsert_article(created_article) into created_article;


    perform vote(
        reference => 'article'::regclass,
        _target_id => (created_article->>'id')::uuid,
        _created_by_id => _citizen_id,
        _note => 1
    );
    assert (select count(*) = 1 from vote_for_article), 'vote must be inserted';
    assert (select note = 1 from vote_for_article limit 1), 'vote must be equal to 1';

    perform vote(
        reference => 'article'::regclass,
        _target_id => (created_article->>'id')::uuid,
        _created_by_id => _citizen_id,
        _note => -1
    );
    assert (select count(*) = 1 from vote_for_article), 'vote must be inserted';
    assert (select note = -1 from vote_for_article limit 1), 'vote must be equal to -1';

    begin
        perform vote(
            reference => 'article'::regclass,
            _target_id => (created_article->>'id')::uuid,
            _created_by_id => _citizen_id,
            _note => -10
        );
        assert false, 'vote must be throw exception if note is not -1, 0 or 1';
    exception when check_violation then
    end;

    select count_vote('article', '933b6a1b-50c9-42b6-989f-c02a57814ef9') into votes;
    assert ((votes->>'up')::int = 0), 'vote.up must be 0';

    -- delete vote and context
    delete from vote;
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'vote test pass';
end;
$$;


-- select uuid_generate_v4();