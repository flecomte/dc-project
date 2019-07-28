create or replace function follow(reference regclass, target_id uuid, citizen_id uuid) returns void
    language plpgsql as
$$
declare
    _citizen_id alias for citizen_id;
    _target_id alias for target_id;
begin
    if reference = 'article'::regclass then
        insert into follow_article (citizen_id, target_id)
        values (_citizen_id, _target_id);
    elseif reference = 'constitution'::regclass then
        insert into follow_constitution (citizen_id, target_id)
        values (_citizen_id, _target_id);
    elseif reference = 'citizen'::regclass then
        insert into follow_citizen (citizen_id, target_id)
        values (_citizen_id, _target_id);
    end if;
end;
$$;

-- drop function if exists follow(regclass, uuid, uuid);