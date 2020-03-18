create or replace function unfollow(reference regclass, _target_id uuid, _created_by_id uuid) returns void
    language plpgsql as
$$
declare
    _targets_ids uuid[];
begin
    if reference = 'article'::regclass then
        select array_agg(a2.id) into _targets_ids
        from article a1
        join article a2 using (version_id)
        where a1.id = _target_id;

        delete
        from follow f
        where f.created_by_id = _created_by_id
          and f.target_id = any(_targets_ids)
          and f.target_reference = reference;
    elseif reference = 'constitution'::regclass then
        select array_agg(c2.id) into _targets_ids
        from constitution c1
        join constitution c2 using (version_id)
        where c1.id = _target_id;
        delete
        from follow f
        where f.created_by_id = _created_by_id
          and f.target_id = any(_targets_ids)
          and f.target_reference = reference;
    elseif reference = 'citizen'::regclass then
        delete
        from follow f
        where f.created_by_id = _created_by_id
          and f.target_id = _target_id
          and f.target_reference = reference;
    else
        raise exception '% no implemented for follow', reference::text;
    end if;
end;
$$;

