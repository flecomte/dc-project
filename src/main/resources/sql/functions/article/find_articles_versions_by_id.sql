create or replace function find_articles_versions_by_id(in _id uuid, out resource json) language plpgsql as
$$
declare
    _version_id uuid = (select version_id from article where id = _id);
begin
    select find_articles_versions_by_version_id(_version_id)
    into resource;
end;
$$;

-- drop function if exists find_articles_versions_by_id(uuid, out json);
