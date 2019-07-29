create or replace function follow(reference regclass, _target_id uuid, _citizen_id uuid) returns void
    language plpgsql as
$$
begin
    if reference = 'article'::regclass then
        insert into follow_article (citizen_id, target_id)
        values (_citizen_id, _target_id)
        on conflict (citizen_id, target_id) do nothing;
    elseif reference = 'constitution'::regclass then
        insert into follow_constitution (citizen_id, target_id)
        values (_citizen_id, _target_id)
        on conflict (citizen_id, target_id) do nothing;
    elseif reference = 'citizen'::regclass then
        insert into follow_citizen (citizen_id, target_id)
        values (_citizen_id, _target_id)
        on conflict (citizen_id, target_id) do nothing;
    else
        raise exception '% no implemented', reference::text;
    end if;
end;
$$;

-- drop function if exists follow(regclass, uuid, uuid);