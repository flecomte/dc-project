create or replace function find_articles_versions_by_version_id(in _version_id uuid, out resource json) language plpgsql as
$$
begin
    select json_agg(t order by t.version_number desc)
    into resource
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by
        from article as a
        where a.version_id = _version_id
        order by a.version_number desc
    ) as t;
end;
$$;

-- drop function if exists find_articles_versions_by_version_id(uuid, out json);
