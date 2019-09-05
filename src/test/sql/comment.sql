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
      "anonymous": false,
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
    _find_comments_by_target_result json;
    _find_comments_by_parent_result json;
    _find_comments_by_id_result json;
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
        created_by_id => _citizen_id,
        content => 'Ho my god !'::text
    ) into _comment_id;
    assert (select count(*) = 1 from "comment"), 'comment must be inserted, "' || (select count(*) from "comment") || '" exist';
    assert (select com.content = 'Ho my god !' from "comment" com), 'the content of comment must be "Ho my god !" instead of "' || (select com.content from "comment" as com) || '"';

    select find_comment_by_id(_comment_id) into _find_comments_by_id_result;
    assert (_find_comments_by_id_result->>'content' = 'Ho my god !'), 'content of comment must be "Ho my god !"';

    perform edit_comment(
        _id => _comment_id,
        _content => 'edited content'::text
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
    from find_comments_by_citizen(_citizen_id);
    assert (_selected_comments_total = 1), 'the number of comments for this citizen must be 1, "' || _selected_comments_total || '" returned';
    assert (_selected_comments#>>'{0,content}' = 'edited content'),
        'the content of first comment for this citizen must be "edited content", "' || (_selected_comments#>>'{0,content}') || '" returned';

    select resource, total
    into _selected_comments, _selected_comments_total
    from find_comments_by_citizen(_citizen_id, 'constitution'::regclass);
    assert (_selected_comments_total = 0), 'the number of comments for this citizen must be 0, "' || _selected_comments_total || '" returned';


    select "comment"(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        created_by_id => _citizen_id,
        content => 'God not exist'::text,
        parent_id => _comment_id::uuid
    ) into _comment_id_response;

    select "comment"(
        reference => 'article'::regclass,
        target_id => (created_article->>'id')::uuid,
        created_by_id => _citizen_id,
        content => 'are you really sure ?'::text,
        parent_id => _comment_id_response::uuid
    ) into _comment_id_response2;
    assert (select count(*) = 3 from "comment"), 'response must be inserted';
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response);
    assert (select com.parents_ids @> ARRAY[_comment_id_response] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id_response::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);

    select resource into _find_comments_by_target_result
    from find_comments_by_target((created_article->>'id')::uuid);
    assert json_array_length(_find_comments_by_target_result) = 3,
        'the result should contain 3 comment, ' || json_array_length(_find_comments_by_target_result) || ' returned';
    assert (_find_comments_by_target_result#>>'{0,content}') = 'edited content', 'the first content must contain "edited content", "' || (_find_comments_by_target_result#>>'{0,content}') || '" returned';
    assert (_find_comments_by_target_result#>>'{1,content}') = 'God not exist', 'the second content must contain "God not exist", "' || (_find_comments_by_target_result#>>'{1,content}') || '" returned';
    assert (_find_comments_by_target_result#>>'{2,content}') = 'are you really sure ?', 'the third content must contain "are you really sure ?", "' || (_find_comments_by_target_result#>>'{2,content}') || '" returned';

    select resource into _find_comments_by_parent_result
    from find_comments_by_parent((_find_comments_by_target_result#>>'{0,id}')::uuid);
    assert json_array_length(_find_comments_by_parent_result) = 2,
        'the result should contain 2 comment, ' || json_array_length(_find_comments_by_parent_result) || ' returned';
    assert (_find_comments_by_parent_result#>>'{0,content}') = 'God not exist', 'the second content must contain "God not exist", "' || (_find_comments_by_parent_result#>>'{0,content}') || '" returned';
    assert (_find_comments_by_parent_result#>>'{1,content}') = 'are you really sure ?', 'the third content must contain "are you really sure ?", "' || (_find_comments_by_parent_result#>>'{1,content}') || '" returned';

    -- delete comment and context
    delete from "comment";
    delete from article;
    delete from citizen;
    delete from "user";

    raise notice 'comment test pass';
end;
$$;


-- select uuid_generate_v4();