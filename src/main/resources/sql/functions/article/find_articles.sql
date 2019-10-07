create or replace function find_articles(
    _search text default null,
    direction text default 'desc',
    sort text default 'created_at',
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from article a where (_search is null or _search = '' or a ==> dsl.multi_match('{title^3, content, description, tags}', _search)) and a.last_version = true)
    into resource, total
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by,
            count_vote(a.id) as votes,
            zdb.score(a.ctid) _score
        from article as a
        where (
              _search is null
           or _search = ''
           or a ==> dsl.multi_match('{title^3, content, description, tags}', _search)
        ) and a.last_version = true
        order by
        _score desc,
        case direction when 'asc' then
            case sort
                when 'title' then a.title
                when 'created_at' then a.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'title' then a.title
                when 'created_at' then a.created_at::text
            end
        end
        desc,
        a.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_articles(text, text, text, int, int);
