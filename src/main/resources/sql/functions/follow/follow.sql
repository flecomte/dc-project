create or replace function follow(reference regclass, _target_id uuid, _created_by_id uuid) returns void
    language plpgsql as
$$
begin
    if reference = 'article'::regclass then
        insert into follow_article (created_by_id, target_id)
        values (_created_by_id, _target_id)
        on conflict (created_by_id, target_id) do nothing;
    elseif reference = 'constitution'::regclass then
        insert into follow_constitution (created_by_id, target_id)
        values (_created_by_id, _target_id)
        on conflict (created_by_id, target_id) do nothing;
    elseif reference = 'citizen'::regclass then
        insert into follow_citizen (created_by_id, target_id)
        values (_created_by_id, _target_id)
        on conflict (created_by_id, target_id) do nothing;
    else
        raise exception '% no implemented for follow', reference::text;
    end if;
end;
$$;

-- drop function if exists follow(regclass, uuid, uuid);