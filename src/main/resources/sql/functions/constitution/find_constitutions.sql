create or replace function find_constitutions(
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
    select json_agg(t), (select count(id) from constitution)
    into resource, total
    from (
        select
            c.*,
            find_citizen_by_id(c.created_by_id) as created_by,
            find_constitution_titles_by_id(c.id) as titles
        from constitution as c
        where "search" is null or title ilike '%'||"search"||'%'
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

-- drop function if exists find_constitutions(json, int, int);
