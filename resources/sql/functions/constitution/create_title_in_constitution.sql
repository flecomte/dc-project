create or replace function create_title_in_constitution(title json, constitution_id uuid default null, out resource json)
    language plpgsql as
$$
declare
    _title alias for title;
    _constitution_id uuid = coalesce(constitution_id, (title#>>'{constitution_id}')::uuid);
    _author_id uuid = (title#>>'{created_by, id}')::uuid;
    new_id uuid;
begin
    insert into title (created_by_id, name, rank, constitution_id)
    select
        _author_id,
        ti.name,
        row_number() OVER (),
        _constitution_id
    from json_populate_record(null::title, _title) ti
    returning id into new_id;

    if (_title->'articles' is not null) then
        insert into article_in_title (created_by_id, rank, title_id, article_id, constitution_id)
        select
            _author_id,
            row_number() over (),
            new_id,
            id,
            coalesce ((_title->>'constitution_id')::uuid, _constitution_id)
        from json_populate_recordset(null::article, _title->'articles') ;
    end if;

    select find_constitution_title_by_id(new_id)
    into resource;
end;
$$;

-- drop function if exists create_title_in_constitution(out json);