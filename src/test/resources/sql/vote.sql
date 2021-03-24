do
$$
declare
    _citizen_id  uuid := fixture_citizen('george');
    _citizen_id2 uuid := fixture_citizen('john');
    _article_id  uuid := fixture_article(_citizen_id := _citizen_id);
    votes jsonb;
    votes_of_citizen json;
    votes_of_citizen_for_targets json;
    votes_total int;
begin
    perform vote(
        reference => 'article'::regclass,
        _target_id => _article_id,
        _created_by_id => _citizen_id,
        _note => 1
    );
    assert (select count(*) = 1 from vote_for_article), 'vote must be inserted';
    assert (select note = 1 from vote_for_article limit 1), 'vote must be equal to 1';

    perform vote(
        reference => 'article'::regclass,
        _target_id => _article_id,
        _created_by_id => _citizen_id,
        _note => -1
    );
    assert (select count(*) = 1 from vote_for_article), 'vote must be inserted';
    assert (select note = -1 from vote_for_article limit 1), 'vote must be equal to -1';

    begin
        perform vote(
            reference => 'article'::regclass,
            _target_id => _article_id,
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
    select find_citizen_votes_by_target_ids(_citizen_id, array[_article_id]::uuid[]) into votes_of_citizen_for_targets;
    assert (json_array_length(votes_of_citizen_for_targets) = 1), format('the function must be return 1 vote, instead of %s', json_array_length(votes_of_citizen_for_targets));
    assert (votes_of_citizen_for_targets#>>'{0,target_id}' = _article_id::text), format('target_id of vote must be %s, instead of %s', _article_id, votes_of_citizen_for_targets#>>'{0,target_id}');

    rollback;
    raise notice 'vote test pass';
end;
$$;
