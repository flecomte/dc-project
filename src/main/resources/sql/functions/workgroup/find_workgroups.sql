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
        left join citizen_in_workgroup ciw on w.id = ciw.workgroup_id
        where deleted_at is null
          and (_search is null or _search = '' or w ==> dsl.multi_match('{name^3, description}', _search))
          and (_filter->>'created_by_id' is null or w.created_by_id = (_filter->>'created_by_id')::uuid)
          and (_filter->>'members' is null or to_jsonb(array[ciw.citizen_id]) <@ (_filter->'members')::jsonb)
        )
    into resource, total
    from (
        select
            w.*,
            find_citizen_by_id_with_user(w.created_by_id) as created_by,
            zdb.score(w.ctid) _score
        from workgroup as w
        left join citizen_in_workgroup ciw on w.id = ciw.workgroup_id
        where deleted_at is null
        and (
              _search is null
           or _search = ''
           or w ==> dsl.multi_match('{name^3, description}', _search)
        )
          and (_filter->>'created_by_id' is null or w.created_by_id = (_filter->>'created_by_id')::uuid)
          and (_filter->>'members' is null or to_jsonb(array[ciw.citizen_id]) <@ (_filter->'members')::jsonb)

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
                when 'name' then w.name
                when 'created_at' then w.created_at::text
            end
        end
        desc,
        w.created_at desc
        limit "limit" offset "offset"
    ) as t;
end;
$$;
