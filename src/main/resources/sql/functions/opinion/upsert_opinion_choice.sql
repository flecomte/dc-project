create or replace function upsert_opinion_choice(inout resource json)
    language plpgsql as
$$
declare
    _id uuid = coalesce((resource->>'id')::uuid, uuid_generate_v4());
begin
    insert into opinion_choice (id, name, target)
    select
        _id,
        name,
        target
    from json_populate_record(null::opinion_choice, resource)
    on conflict (name) do update set
        target = excluded.target;

    select find_opinion_choice_by_id(_id) into resource;
end;
$$;

