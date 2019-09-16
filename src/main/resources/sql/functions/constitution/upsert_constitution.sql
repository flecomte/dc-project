create or replace function upsert_constitution(inout resource json)
    language plpgsql as
$$
declare
    titles json;
    _title json;
    _citizen_id uuid = (resource#>>'{created_by, id}')::uuid;
    new_id uuid;
    _id_exist boolean;
begin
    -- check if version id already exist
    select count(*) >= 1
    into _id_exist
    from constitution
    where (resource->>'id')::uuid is not null
      and id = (resource->>'id')::uuid;

    insert into constitution (id, version_id, created_by_id, title, anonymous)
    select
        case when _id_exist then uuid_generate_v4()
             else coalesce(id, uuid_generate_v4()) end,
       version_id,
       _citizen_id,
       title,
       anonymous
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