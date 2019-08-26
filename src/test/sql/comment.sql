do
$$
declare
    created_user     json  := '{"username": "george", "plain_password": "azerty", "roles": ["ROLE_USER"]}';
    created_user2    json  := '{"username": "john", "plain_password": "qwerty", "roles": ["ROLE_USER"]}';
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
    _comment_id_response uuid;
    _comment_id_response2 uuid;
    _selected_comments json;
    _selected_comments_total int;
begin
    -- insert user for context
    select insert_user(created_user) into created_user;
    select insert_user(created_user2) into created_user2;
    created_citizen := jsonb_set(created_citizen::jsonb, '{user}'::text[], jsonb_build_object('id', created_user->>'id'), true)::json;

    -- insert new citizen for context
    select upsert_citizen(created_citizen) into created_citizen;
    _citizen_id := created_citizen->>'id';
    created_article := jsonb_set(created_article::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
    assert created_article#>>'{created_by, id}' = _citizen_id::text, format('citizenId in article must be the same as citizen, %s != %s', created_article#>>'{created_by, id}', _citizen_id::text);
    -- upsert article
    select upsert_article(created_article) into created_article;


    select "comment"(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        citizen_id => _citizen_id,
        content => 'Ho my god !'::text
    ) into _comment_id;
    assert (select count(*) = 1 from "comment"), 'comment must be inserted';

    perform edit_comment(
        reference => 'article'::regclass,
        id => _comment_id,
        content => 'edited content'::text
    );
    assert (select count(*) = 1 from "comment"), 'edit comment must not insert new comment';
    assert (select count(*) = 1 from "comment" where content = 'edited content'), 'edit comment must not insert new comment';

    select resource, total
    into _selected_comments, _selected_comments_total
    from find_comments_by_citizen(_citizen_id);
    assert (_selected_comments_total = 1), 'the number of comments for this citizen must be 1, "' || _selected_comments_total || '" returned';
    assert (_selected_comments#>>'{0,content}' = 'edited content'),
        'the content of first comment for this citizen must be "edited content", "' || (_selected_comments#>>'{0,content}') || '" returned';

    select resource, total
    into _selected_comments, _selected_comments_total
    from find_comments_article_by_citizen(_citizen_id);
    assert (_selected_comments_total = 1), 'the number of comments for this citizen must be 1, "' || _selected_comments_total || '" returned';
    assert (_selected_comments#>>'{0,content}' = 'edited content'),
        'the content of first comment for this citizen must be "edited content", "' || (_selected_comments#>>'{0,content}') || '" returned';

    select resource, total
    into _selected_comments, _selected_comments_total
    from find_comments_constitution_by_citizen(_citizen_id);
    assert (_selected_comments_total = 0), 'the number of comments for this citizen must be 0, "' || _selected_comments_total || '" returned';


    select "comment"(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        citizen_id => _citizen_id,
        content => 'No is not exist'::text,
        _parent_id => _comment_id::uuid
    ) into _comment_id_response;

    select "comment"(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        citizen_id => _citizen_id,
        content => 'No is not exist'::text,
        _parent_id => _comment_id_response::uuid
    ) into _comment_id_response2;
    assert (select count(*) = 3 from "comment"), 'comment must be inserted';
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response);
    assert (select com.parents_ids @> ARRAY[_comment_id_response] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id_response::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);

    -- delete comment and context
    delete from "comment";
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'comment test pass';
end;
$$;


-- select uuid_generate_v4();