create or replace function upsert_constitution(inout resource json)
    language plpgsql as
$$
declare
    titles json;
    _title json;
    _citizen_id uuid = (resource#>>'{created_by, id}')::uuid;
    new_id uuid;
begin
    insert into constitution (version_id, created_by_id, title, annonymous)
    select
       version_id,
       _citizen_id,
       title,
       annonymous
    from json_populate_record(null::constitution, resource)
    returning id into new_id;

    titles := (resource->>'titles');

    for _title in select json_array_elements(titles) loop
        if _title#>>'{created_by, id}' is null then
            _title := jsonb_set(_title::jsonb, '{created_by}'::text[], jsonb_build_object('id', _citizen_id::text), true)::json;
        end if;

        perform create_title_in_constitution(_title, new_id);
    end loop;

    select find_constitution_by_id(new_id) into resource;
end;
$$;

-- drop function if exists upsert_constitution(inout json);