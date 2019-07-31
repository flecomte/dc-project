create or replace function find_articles(
    search text default null,
    direction text default 'desc',
    sort text default 'created_at',
    "limit" int default 50,
    "offset" int default 0,
    out resource json,
    out total int
) language plpgsql as
$$
begin
    select json_agg(t), (select count(id) from article)
    into resource, total
    from (
        select
            a.*,
            find_citizen_by_id(a.created_by_id) as created_by
        from article as a
        where "search" is null or title ilike '%'||"search"||'%'
        order by
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

-- drop function if exists find_articles(json, int, int);
