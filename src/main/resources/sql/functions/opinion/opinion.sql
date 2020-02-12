create or replace function opinion(reference regclass, _target_id uuid, _created_by_id uuid, _opinion uuid, out resource json)
    language plpgsql as
$$
begin
    if reference = 'article'::regclass then
        insert into opinion_on_article (created_by_id, target_id, choice_id)
        values (_created_by_id, _target_id, _opinion)
        on conflict (created_by_id, target_id, choice_id) do nothing;
    else
        raise exception '% no implemented for opinion', reference::text;
    end if;

    select count_opinion(_target_id) into resource;
end;
$$;

-- drop function if exists vote(regclass,uuid,uuid,integer,boolean);