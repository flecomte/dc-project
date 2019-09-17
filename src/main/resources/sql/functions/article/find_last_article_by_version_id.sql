create or replace function find_last_article_by_version_id(in version_id uuid, out resource json) language plpgsql as
$$
declare
    _version_id alias for version_id;
begin
    select to_json(t)
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by,
            count_vote('article', a.id) as votes
        into resource
        from article as a
        where a.version_id = _version_id
          and a.draft = false
          and a.deleted_at is null
        order by a.version_number desc
        limit 1
    ) as t;
end;
$$;

-- drop function if exists find_last_article_by_version_id(uuid, inout json);