create or replace function find_citizen_opinions_by_target_ids(
    _citizen_id uuid,
    _ids uuid[],
    out resource json
) language plpgsql as
$$
begin
    select json_agg(t) into resource
    from (
        select
            o.*,
            find_reference_by_id(o.target_id, o.target_reference) as target,
            find_citizen_by_id_with_user(o.created_by_id) as created_by,
            to_json(ol) as choice
        from opinion as o
        join opinion_choice ol on o.choice_id = ol.id

        where target_id = any(_ids)
            and created_by_id = _citizen_id

        order by
            ol.name
        limit 100
    ) t;
end;
$$;
