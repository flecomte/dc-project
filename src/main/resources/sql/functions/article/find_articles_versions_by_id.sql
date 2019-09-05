create or replace function find_articles_versions_by_id(
    _id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
declare
    _version_id uuid = (select version_id from article where id = _id);
begin
    select a.resource, a.total
    into resource, total
    from find_articles_versions_by_version_id(_version_id, "limit", "offset") a;
end;
$$;

-- drop function if exists find_articles_versions_by_id(uuid, int, int, out json);
