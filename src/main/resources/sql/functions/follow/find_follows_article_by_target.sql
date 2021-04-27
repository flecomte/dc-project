create or replace function find_follows_article_by_target(
    _target_id uuid,
    _limit int default 50,
    _start_id uuid default null,
    out resource json
) language plpgsql as
$$
declare
    _version_id uuid = (select version_id from article where id = _target_id);
    _start_at timestamp default '2000-01-01 00:00:00'::timestamp;
    _article_creator_id uuid = (select created_by_id from article where id = _target_id);
begin
    if _start_id is not null then
        select created_at into _start_at from follow where id = _start_id;
    end if;

    select json_agg(t)
    into resource
    from (
        select
            f.id,
            f.created_at,
            f.target_reference,
            json_build_object('id', f.target_id) as target,
            find_citizen_by_id_with_user(f.created_by_id) as created_by
        from follow as f
        left join article a on f.target_reference = 'article'::regclass and f.target_id = a.id
        where (
            (f.target_reference = 'article'::regclass and a.version_id = _version_id)
            or
            (f.target_reference = 'citizen'::regclass and f.target_id = _article_creator_id)
          )
          and f.created_at >= _start_at
          and (_start_id is null or f.id != _start_id)
        order by f.created_at
        limit _limit
    ) as t;
end
$$;
