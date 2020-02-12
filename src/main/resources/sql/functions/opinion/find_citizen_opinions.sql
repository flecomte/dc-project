create or replace function find_citizen_opinions(
    _citizen_id uuid,
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
        select count(o.id)
        from opinion o
        where o.created_by_id = _citizen_id
    )
    into resource, total
    from (
        select
            o.*,
            to_json(ol) as choice
        from opinion as o
        join opinion_choice ol on o.choice_id = ol.id

        where created_by_id = _citizen_id

        order by
        case direction when 'asc' then
            case sort
                when 'created_at' then o.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'created_at' then o.created_at::text
            end
        end
        desc,
        o.created_at desc
        limit "limit" offset "offset"
    ) t;
end
$$;

-- select * from find_citizen_opinions('6434f4f9-f570-f22a-c134-8668350651ff', null, null, 2, 2);
