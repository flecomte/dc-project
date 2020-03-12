create or replace function find_workgroups(
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
        from workgroup w
        where deleted_at is null
          and (_search is null or _search = '' or w ==> dsl.multi_match('{name^3, description}', _search))
          and (_filter->>'created_by_id' is null or w.created_by_id = (_filter->>'created_by_id')::uuid)
        )
    into resource, total
    from (
        select
            w.*,
            find_citizen_by_id(w.created_by_id) as created_by,
            find_citizen_by_id(w.owner_id) as owner,
            zdb.score(w.ctid) _score
        from workgroup as w
        where deleted_at is null
        and (
              _search is null
           or _search = ''
           or w ==> dsl.multi_match('{name^3, description}', _search)
        )
          and (_filter->>'created_by_id' is null or w.created_by_id = (_filter->>'created_by_id')::uuid)

        order by
        _score desc,
        case direction when 'asc' then
            case sort
                when 'name' then w.name
                when 'created_at' then w.created_at::text
                else null
            end
        end,
        case direction when 'desc' then
            case sort
                when 'title' then w.name
                when 'created_at' then w.created_at::text
            end
        end
        desc,
        w.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;

-- drop function if exists find_workgroups(text, json, text, text, int, int);
-- select * from find_workgroups('49', "limit" := 2)