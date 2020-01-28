create or replace function upsert_constitution(inout resource json)
    language plpgsql as
$$
declare
    titles json;
    _title json;
    _citizen_id uuid = (resource#>>'{created_by, id}')::uuid;
    new_id uuid;
    _id_exist boolean;
    _existing_draft_id uuid = (
        select c.id from constitution c
        where c.version_id = (resource->>'version_id')::uuid
          and c.draft = true
    );
begin
    -- check if version id already exist
    select count(*) >= 1
    into _id_exist
    from constitution
    where (resource->>'id')::uuid is not null
      and id = (resource->>'id')::uuid;

    if (_existing_draft_id is not null) then
        update constitution c2 set
            title = c.title,
            anonymous = c.anonymous,
            intro = c.intro,
            draft = c.draft
        from json_populate_record(null::constitution, resource) c
        where c2.id = (_existing_draft_id)::uuid
        returning c2.id into new_id;
    else
        insert into constitution (id, version_id, created_by_id, title, intro, anonymous)
        select
            case when _id_exist then uuid_generate_v4()
                 else coalesce(id, uuid_generate_v4()) end,
           version_id,
           _citizen_id,
           title,
           intro,
           anonymous
        from json_populate_record(null::constitution, resource)
        returning id into new_id;
    end if;

    titles := (resource->>'titles');

    for _title in select json_array_elements(titles) loop
        perform create_title_in_constitution(_title, new_id);
    end loop;

    select find_constitution_by_id(new_id) into resource;
end;
$$;

-- drop function if exists upsert_constitution(inout json);