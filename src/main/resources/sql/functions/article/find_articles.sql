create or replace function find_articles(
    _search text default null,
    _filter json default '{}',
    direction text default 'desc',
    sort text default 'created_at',
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (
        select count(id)
        from article a
        where (_search is null or _search = '' or a ==> dsl.multi_match('{title^3, content, description, tags}', _search))
          and (_filter->>'created_by_id' is null or a.created_by_id = (_filter->>'created_by_id')::uuid)
          and (_filter->>'workgroup_id' is null or a.workgroup_id = (_filter->>'workgroup_id')::uuid)
          and a.last_version = true
        )
    into resource, total
    from (
        select
            a.id,
            a.title,
            a.created_at,
            a.deleted_at,
            a.draft,
            find_citizen_by_id_with_user(a.created_by_id) as created_by,
            find_workgroup_by_id(a.workgroup_id) as workgroup,
            count_vote(a.id) as votes,
            zdb.score(a.ctid) _score
        from article as a
        left join vote_cache ca using (id)
        where (
              _search is null
           or _search = ''
           or a ==> dsl.multi_match('{title^3, content, description, tags}', _search)
        )
          and (_filter->>'created_by_id' is null or a.created_by_id = (_filter->>'created_by_id')::uuid)
          and (_filter->>'workgroup_id' is null or a.workgroup_id = (_filter->>'workgroup_id')::uuid)
          and a.last_version = true

        order by
        _score desc,
        case direction when 'asc' then
            case sort
                when 'title' then a.title
                when 'createdAt' then a.created_at::text
                when 'vote' then ca.score::text
                when 'popularity' then ca.total::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'title' then a.title
                when 'createdAt' then a.created_at::text
                when 'vote' then ca.score::text
                when 'popularity' then ca.total::text
            end
        end
        desc,
        a.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;
