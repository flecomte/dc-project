create or replace function vote(reference regclass, _target_id uuid, _created_by_id uuid, _note int, _anonymous bool default true, out resource json)
    language plpgsql as
$$
begin
    if reference = 'article'::regclass then
        insert into vote_for_article (created_by_id, target_id, note, anonymous)
        values (_created_by_id, _target_id, _note, _anonymous)
        on conflict (created_by_id, target_id) do update set
          note = excluded.note,
          anonymous = excluded.anonymous,
          updated_at = now();
    elseif reference = 'constitution'::regclass then
        insert into vote_for_constitution (created_by_id, target_id, note, anonymous)
        values (_created_by_id, _target_id, _note, _anonymous)
        on conflict (created_by_id, target_id) do update set
          note = excluded.note,
          anonymous = excluded.anonymous,
          updated_at = now();
    elseif reference = 'comment_on_article'::regclass then
        insert into vote_for_comment_on_article (created_by_id, target_id, note, anonymous)
        values (_created_by_id, _target_id, _note, _anonymous)
        on conflict (created_by_id, target_id) do update set
          note = excluded.note,
          anonymous = excluded.anonymous,
          updated_at = now();
    elseif reference = 'comment_on_constitution'::regclass then
        insert into vote_for_comment_on_constitution (created_by_id, target_id, note, anonymous)
        values (_created_by_id, _target_id, _note, _anonymous)
        on conflict (created_by_id, target_id) do update set
          note = excluded.note,
          anonymous = excluded.anonymous,
          updated_at = now();
    else
        raise exception '% no implemented for vote', reference::text;
    end if;

    select count_vote(_target_id) into resource;

    insert into vote_cache (id, total, score, percent) values
    (
        _target_id,
        (resource->>'total')::int,
        (resource->>'score')::int,
        (resource->>'percent')::int
    )
    on conflict (id) do update set
        updated_at = now(),
        total = excluded.total,
        score = excluded.score,
        percent = excluded.percent;
end;
$$;

