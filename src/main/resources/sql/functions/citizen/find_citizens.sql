create or replace function find_citizens(
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
    select json_agg(t), (select count(id) from citizen)
    into resource, total
    from (
        select
            z.*
        from citizen as z
        where "search" is null or (
            (name->'first_name')::text ilike '%'||"search"||'%' or
            (name->'last_name')::text ilike '%'||"search"||'%'
        )
        order by
        case direction when 'asc' then
            case sort
                when 'name' then (z.name->'first_name')::text
                when 'created_at' then z.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'name' then (z.name->'first_name')::text
                when 'created_at' then z.created_at::text
            end
        end
        desc,
        z.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_citizens(text, text, text, int, int);
