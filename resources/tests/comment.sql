do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty"}';
    created_user2    json  := '{"username": "john", "plain_password": "qwerty"}';
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
      "annonymous": false,
      "content": "bla bal bla",
      "tags": [
        "love",
        "test"
      ]
    }
    $json$;
    _comment_id uuid;
begin
    -- insert user for context
    call insert_user(created_user);
    call insert_user(created_user2);
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', created_user->>'id'), true)::json;

    -- insert new citizen for context
    call upsert_citizen(created_citizen);
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);
    -- upsert article
    call upsert_article(created_article);


    select comment(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        citizen_id => _citizen_id,
        content => 'Ho my god !'::text
    ) into _comment_id;
    assert (select count(*) = 1 from "comment"), 'comment must be inserted';

    perform edit_comment(
        reference => 'article'::regclass,
        id => _comment_id,
        content => 'edited'::text
    );
    assert (select count(*) = 1 from "comment"), 'edit comment must not insert new comment';
    assert (select count(*) = 1 from "comment" where content = 'edited'), 'edit comment must not insert new comment';

    -- delete article and context
    delete from "comment";
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'comment test pass';
end;
$$;


-- select uuid_generate_v4();