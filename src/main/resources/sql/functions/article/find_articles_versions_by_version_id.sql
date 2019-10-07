create or replace function find_articles_versions_by_version_id(
    _version_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select
        json_agg(t order by t.version_number desc),
        (select count(a) from article a where a.version_id = _version_id)
    into resource, total
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by,
            count_vote(a.id) as votes
        from article as a
        where a.version_id = _version_id
        order by a.version_number desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_articles_versions_by_version_id(uuid, int, int, out json);
