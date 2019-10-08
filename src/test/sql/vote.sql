do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    created_user2    json  := '{"username": "george2", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    _citizen_id      uuid;
    _citizen_id2      uuid;
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
      "email":"george.michel@gmail.com"
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
    votes_of_citizen json;
    votes_of_citizen_for_targets json;
    votes_total int;
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

    select count_vote('933b6a1b-50c9-42b6-989f-c02a57814ef9') into votes;
    assert ((votes->>'up')::int = 0), 'vote.up must be 0';

    -- Test "find_votes_by_citizen"
    select resource, total into votes_of_citizen, votes_total from find_votes_by_citizen(_citizen_id2);
    assert (votes_total = 0), format('votes count for user %s must be 0, instead of %s', _citizen_id2, votes_total);

    select resource, total into votes_of_citizen, votes_total from find_votes_by_citizen(_citizen_id);
    assert (votes_total = 1), format('votes count for user %s must be 1, instead of %s', _citizen_id, votes_total);
    assert ((votes_of_citizen#>>'{0,note}')::int = -1), format('the note must be -1, instead of %s', (votes_of_citizen#>>'{0,note}'));

    -- test "find_citizen_votes_by_target_ids"
    select find_citizen_votes_by_target_ids(_citizen_id, array[(created_article->>'id')]::uuid[]) into votes_of_citizen_for_targets;
    assert (json_array_length(votes_of_citizen_for_targets) = 1), format('the function must be return 1 vote, instead of %s', json_array_length(votes_of_citizen_for_targets));
    assert (votes_of_citizen_for_targets#>>'{0,target_id}' = created_article->>'id'), format('target_id of vote must be %s, instead of %s', created_article->>'id', votes_of_citizen_for_targets#>>'{0,target_id}');

    -- delete vote and context
    delete from vote;
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'vote test pass';
end;
$$;


-- select uuid_generate_v4();