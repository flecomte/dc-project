create or replace procedure upsert_constitution(inout resource json)
    language plpgsql as
$$
declare
    titles json;
    new_id uuid;
begin
    insert into constitution (version_id, created_by_id, title, annonymous)
    select
       version_id,
       (resource#>>'{created_by, id}')::uuid,
       title,
       annonymous
    from json_populate_record(null::constitution, resource)
    returning id into new_id;

    titles := (resource->>'titles');

    insert into title (created_by_id, name, rank, constitution_id)
    select
        coalesce((ti#>>'{created_by, id}')::uuid, (resource#>>'{created_by, id}')::uuid),
        ti->>'name',
        row_number() OVER (),
        new_id
    from json_array_elements(titles) ti,
    lateral json_populate_record(null::title, ti);

    select find_constitution_by_id(new_id) into resource;
end;
$$;

-- drop procedure if exists upsert_constitution(inout json);