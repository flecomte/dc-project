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
            json_build_object('id', f.created_by_id) as created_by
        from follow_article as f
        join article a on f.target_id = a.id
        where a.version_id = _version_id
        order by f.created_at
        limit "limit" offset "offset"
    ) as t;
end
$$;

-- drop function if exists find_follows_article_by_target(uuid, int, int);
-- select * from find_follows_article_by_target('32518c76-5c58-3cd1-00cd-7f9d0bb872cd', 20, 0);
-- select * from find_follows_article_by_target('24a373f4-c321-4006-8d05-3c50f95a561b', 100, 0);
-- SELECT * FROM find_follows_article_by_target ("_target_id" := '24a373f4-c321-4006-8d05-3c50f95a561b'::uuid, "offset" := 0::int, "limit" := 300::int)