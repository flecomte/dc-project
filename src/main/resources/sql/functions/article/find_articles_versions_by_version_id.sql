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
           a.id,
           a.created_at,
           find_citizen_by_id_with_user(a.created_by_id) as created_by,
           find_workgroup_by_id(a.workgroup_id) as workgroup,
           a.version_id,
           a.version_number,
           a.title,
           a.deleted_at,
           a.draft,
           a.last_version,
           count_vote(a.id) as votes
        from article as a
        where a.version_id = _version_id
        order by a.version_number desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;


