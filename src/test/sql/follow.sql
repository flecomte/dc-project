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

    created_article  json := '{"version_id":"933b6a1b-50c9-42b6-989f-c02a57814ef9", "title": "Love the world", "anonymous": false, "content": "bla bal bla", "tags": ["love", "test"], "draft":false}';
    first_article_id uuid;
    first_article_updated_id uuid;
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
    assert (select count(*) = 1 from follow), 'follow must be inserted';
    assert (select following = true from find_follow(_citizen_id, _citizen_id2)), 'find_follow must return the following';

    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 1 from follow), 'follow must be inserted';

    perform unfollow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 0 from follow), 'follow must be deleted after unfollow';

    -- upsert article
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    select upsert_article(created_article) into created_article;
    first_article_id = (created_article->>'id')::uuid;

    perform follow('article'::regclass, first_article_id, _citizen_id);
    assert (select following = true from find_follow(first_article_id, _citizen_id)), 'find_follow must return the following';
    assert (select following = false from find_follow(first_article_id, _citizen_id2)), 'find_follow must not return the following if not followinf';
    assert (select count(*) = 1 from follow), 'must have 1 following';

    -- add new version for article, then unfollow the new one
    select upsert_article(created_article) into created_article;
    first_article_updated_id = (created_article->>'id')::uuid;
    assert first_article_id != first_article_updated_id;

    perform unfollow('article'::regclass, first_article_id, _citizen_id);
--     perform unfollow('article'::regclass, first_article_updated_id, _citizen_id);
    assert (select count(*) = 0 from follow), 'follow must be deleted after unfollow, event if article is on other version';


    -- delete follow and context
    delete from follow;
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'follow test pass';
end;
$$;


-- select uuid_generate_v4();