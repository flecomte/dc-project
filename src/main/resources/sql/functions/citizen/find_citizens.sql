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
    select json_agg(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id) as "user"
        from citizen as z
        where "search" is null or (
            (name->'first_name')::text ilike '%'||"search"||'%' or
            (name->'last_name')::text ilike '%'||"search"||'%'
        )
        order by
        case direction when 'asc' then
            case sort
                when 'name' then (z.name->'first_name')::text
                when 'createdAt' then z.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'name' then (z.name->'first_name')::text
                when 'createdAt' then z.created_at::text
            end
        end
        desc,
        z.created_at desc
        limit "limit" offset "offset"
    ) as t;

    select count(id) into total
    from citizen
    where "search" is null or (
        (name->'first_name')::text ilike '%'||"search"||'%' or
        (name->'last_name')::text ilike '%'||"search"||'%'
    );
end;
$$;


