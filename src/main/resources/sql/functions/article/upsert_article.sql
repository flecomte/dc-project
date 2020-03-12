create or replace function upsert_article(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid;
    _id_exist boolean;
    _existing_draft_id uuid = (
        select a.id from article a
        where a.version_id = (resource->>'version_id')::uuid
          and a.draft = true
    );
begin
    -- check if version id already exist
    select count(*) >= 1
    into _id_exist
    from article
    where (resource->>'id')::uuid is not null
      and id = (resource->>'id')::uuid;

    if (_existing_draft_id is not null) then
        update article a2 set
            created_by_id = (resource#>>'{created_by, id}')::uuid,
            workgroup_id = (resource#>>'{workgroup, id}')::uuid,
            title = a.title,
            anonymous = a.anonymous,
            content = a.content,
            description = a.description,
            tags = a.tags,
            draft = a.draft
        from json_populate_record(null::article, resource) a
        where a2.id = (_existing_draft_id)::uuid
        returning a2.id into new_id;
    else
        insert into article (id, version_id, created_by_id, workgroup_id, title, anonymous, content, description, tags, draft)
        select
            case when _id_exist then uuid_generate_v4()
                 else coalesce(id, uuid_generate_v4()) end,
            coalesce(version_id, uuid_generate_v4()),
            (resource#>>'{created_by, id}')::uuid,
            (resource#>>'{workgroup, id}')::uuid,
            title,
            anonymous,
            content,
            description,
            tags,
            draft
        from json_populate_record(null::article, resource)
        returning id into new_id;
    end if;

    if resource->>'relations' is not null then
        delete from article_relations
        where source_id = (resource->>'id')::uuid;

        insert into article_relations (source_id, target_id, created_by_id)
        select
            (resource->>'id')::uuid,
            id,
            (resource#>>'{created_by, id}')::uuid
        from json_populate_recordset(null::article, resource->>'relations');
    end if;

    select find_article_by_id(new_id) into resource;
end;
$$;

-- drop procedure if exists upsert_article(inout json);