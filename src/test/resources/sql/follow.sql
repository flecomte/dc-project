do
$$
declare
    _citizen_id  uuid := fixture_citizen('george');
    _citizen_id2 uuid := fixture_citizen('john');

    _version_id1 uuid = uuid_generate_v4();
    first_article_id uuid := fixture_article(_citizen_id := _citizen_id, _version_id := _version_id1);
    first_article_updated_id uuid;
    _follow_count int = 0;
begin
    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 1 from follow), 'follow must be inserted';
    assert (select following = true from find_follow(_citizen_id, _citizen_id2, 'citizen')), 'find_follow must return the following';

    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 1 from follow), 're follow must be do nothing';

    perform unfollow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 0 from follow), 'follow must be deleted after unfollow';


    perform follow('article'::regclass, first_article_id, _citizen_id);
    assert (select following = true from find_follow(first_article_id, _citizen_id, 'article')), 'find_follow must return the following';
    assert (select following = false from find_follow(first_article_id, _citizen_id2, 'article')), 'find_follow must not return the following if not followinf';
    assert (select count(*) = 1 from follow), 'must have 1 following';

    -- add new version for article, then unfollow the new one
    select fixture_article(_citizen_id := _citizen_id, _version_id := _version_id1) into first_article_updated_id;
    assert first_article_id != first_article_updated_id;
    assert (select following = true from find_follow(first_article_id, _citizen_id, 'article')), '(v1) find_follow must return the following';
    assert (select following = true from find_follow(first_article_updated_id, _citizen_id, 'article')), '(v2) find_follow must return the following';

    assert (select count(*) = 1 from follow), 'must be only 1 follow';
    perform follow('citizen'::regclass, _citizen_id, _citizen_id2);
    assert (select count(*) = 2 from follow), 'follow citizen must be inserted';

    assert (select json_array_length(f.resource) = 2 from find_follows_article_by_target(first_article_id) as f), 'find_follows_article_by_target must return 2 follows';
    assert (select (f.resource#>>'{0, created_by, id}')::uuid = _citizen_id from find_follows_article_by_target(first_article_id) as f), 'find_follows_article_by_target must return follows with creator';
    assert (select (f.resource#>>'{1, created_by, id}')::uuid = _citizen_id2 from find_follows_article_by_target(first_article_id) as f), 'find_follows_article_by_target must return follows with creator';

    _follow_count = (select count(*) from follow);
    perform unfollow('article'::regclass, first_article_id, _citizen_id);
    assert (select count(*) = _follow_count-1 from follow), 'follow must be deleted after unfollow, event if article is on other version';

    rollback;
    raise notice 'follow test pass';
end;
$$;
