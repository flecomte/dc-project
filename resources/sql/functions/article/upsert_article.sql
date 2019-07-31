create or replace function upsert_article(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into article (version_id, created_by_id, title, annonymous, content, description, tags)
    select
       coalesce(version_id, uuid_generate_v4()),
       (resource#>>'{created_by, id}')::uuid,
       title,
       annonymous,
       content,
       description,
       tags
    from json_populate_record(null::article, resource)
    returning id into new_id;

    if resource->>'relations' is not null then
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