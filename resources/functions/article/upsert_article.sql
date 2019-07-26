create or replace procedure upsert_article(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into article (version_id, created_by_id, title, annonymous, content, description, tags)
    select
       version_id,
       (resource#>>'{created_by, id}')::uuid,
       title,
       annonymous,
       content,
       description,
       tags
    from json_populate_record(null::article, resource)
    returning id into new_id;

    select find_article_by_id(new_id) into resource;
end;
$$;

-- drop procedure if exists upsert_article(inout json);