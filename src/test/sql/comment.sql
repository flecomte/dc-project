do
$$
declare
    _comment_id uuid;
    _comment_id_response uuid;
    _comment_id_response2 uuid;
    _selected_comments json;
    _selected_comments_total int;
    _find_comments_by_target_result json;
    _find_comments_by_parent_result json;
    _find_comments_by_id_result json;
    _comment json;
    _article_id uuid := fixture_article();
    _citizen_id uuid := fixture_citizen('john');
begin
    _comment = json_build_object(
        'id', 'a2962f49-74e6-4f20-9c13-36f3ccbc4ad7',
        'target', jsonb_build_object('id', _article_id),
        'created_by', jsonb_build_object('id', _citizen_id),
        'content', 'Ho my god !'
    );
    select "comment"(
        reference => 'article'::regclass,
        resource => _comment
    ) into _comment_id;

    assert (select count(*) = 1 from "comment"), 'comment must be inserted, "' || (select count(*) from "comment") || '" exist';
    assert (select com.content = 'Ho my god !' from "comment" com), 'the content of comment must be "Ho my god !" instead of "' || (select com.content from "comment" as com) || '"';

    select find_comment_by_id(_comment_id) into _find_comments_by_id_result;
    assert (_find_comments_by_id_result->>'content' = 'Ho my god !'), format('content of comment must be "Ho my god !" instead of %s', _find_comments_by_id_result->>'content');

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


    _comment = json_build_object(
        'id', '50962646-07b6-42a3-9798-d756b9b6e2ba',
        'target', jsonb_build_object('id', _article_id),
        'created_by', jsonb_build_object('id', _citizen_id),
        'content', 'God not exist',
        'parent', json_build_object('id', _comment_id)
    );
    select "comment"(
        reference => 'article'::regclass,
        resource => _comment
    ) into _comment_id_response;


    _comment = json_build_object(
            'id', 'ce82e683-23a8-4977-92fb-8d61a3ec995a',
            'target', jsonb_build_object('id', _article_id),
            'created_by', jsonb_build_object('id', _citizen_id),
            'content', 'are you really sure ?',
            'parent', json_build_object('id', _comment_id_response)
        );
    select "comment"(
        reference => 'article'::regclass,
        resource => _comment
    ) into _comment_id_response2;
    assert (select count(*) = 3 from "comment"), 'response must be inserted';
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response);
    assert (select com.parents_ids @> ARRAY[_comment_id_response] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id_response::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);
    assert (select com.parents_ids @> ARRAY[_comment_id] from "comment" com where id = _comment_id_response2), 'parents_ids not contain "' || _comment_id::text || '" ' || (select com.parents_ids::text[] from "comment" com where id = _comment_id_response2);

    select resource into _find_comments_by_target_result
    from find_comments_by_target((_article_id)::uuid);
    assert json_array_length(_find_comments_by_target_result) = 1,
        'the result should contain 1 comment, ' || json_array_length(_find_comments_by_target_result) || ' returned';
    assert (_find_comments_by_target_result#>>'{0,content}') = 'edited content', 'the first content must contain "edited content", "' || (_find_comments_by_target_result#>>'{0,content}') || '" returned';

    select resource into _find_comments_by_parent_result
    from find_comments_by_parent(_comment_id_response);
    assert json_array_length(_find_comments_by_parent_result) = 1,
        'the result should contain 1 comment, ' || json_array_length(_find_comments_by_parent_result) || ' returned';
    assert (_find_comments_by_parent_result#>>'{0,content}') = 'are you really sure ?', 'the third content must contain "are you really sure ?", "' || (_find_comments_by_parent_result#>>'{1,content}') || '" returned';

    rollback;
    raise notice 'comment test pass';
end
$$;
