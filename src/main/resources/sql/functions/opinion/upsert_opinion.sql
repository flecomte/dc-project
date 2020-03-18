create or replace function upsert_opinion(inout resource json)
    language plpgsql as
$$
declare
    _reference regclass = (resource#>>'{target, reference}')::regclass;
    _id uuid = coalesce((resource->>'id')::uuid, uuid_generate_v4());
    _target_id uuid = (resource#>>'{target, id}')::uuid;
    _created_by_id uuid = (resource#>>'{created_by, id}')::uuid;
    _choice_id uuid = (resource#>>'{choice, id}')::uuid;
begin
    if _reference = 'article'::regclass then
        insert into opinion_on_article (id, created_by_id, target_id, choice_id)
        values (_id, _created_by_id, _target_id, _choice_id)
        on conflict (created_by_id, target_id, choice_id) do nothing;
    else
        raise exception '% no implemented for opinion', _reference::text;
    end if;

    select find_opinion_by_opinion(resource) into resource;
end;
$$;

