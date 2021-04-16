create or replace function find_follows_article_by_target(
    _target_id uuid,
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
declare
    _version_id uuid = (select version_id from article where id = _target_id);
begin
    select json_agg(t), (
        select count(f.id)
        from follow f
        join article a on f.target_id = a.id
        where a.version_id = _version_id)
    into resource, total
    from (
        select
            f.id,
            f.created_at,
            f.target_reference,
            json_build_object('id', f.target_id) as target,
            find_citizen_by_id_with_user(f.created_by_id) as created_by
        from follow_article as f
        join article a on f.target_id = a.id
        where a.version_id = _version_id
        order by f.created_at
        limit "limit" offset "offset"
    ) as t;
end
$$;
