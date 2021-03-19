create or replace function find_constitutions(
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
    select json_agg(t), (select count(id) from constitution c where _search is null or _search = '' or c ==> dsl.multi_match('{title^3, intro}', _search))
    into resource, total
    from (
        select
            c.*,
            find_citizen_by_id_with_user(c.created_by_id) as created_by,
            zdb.score(c.ctid) _score
        from constitution as c
        where _search is null or _search = '' or c ==> dsl.multi_match('{title^3, intro}', _search)
        order by
        case direction when 'asc' then
            case sort
                when 'title' then c.title
                when 'created_at' then c.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'title' then c.title
                when 'created_at' then c.created_at::text
            end
        end
        desc,
        c.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;
