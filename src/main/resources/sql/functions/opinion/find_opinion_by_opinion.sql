create or replace function find_opinion_by_opinion(
    inout resource json
) language plpgsql as
$$
declare
    _target_id uuid = (resource#>>'{target, id}')::uuid;
    _created_by_id uuid = (resource#>>'{created_by, id}')::uuid;
    _choice_id uuid = (resource#>>'{choice, id}')::uuid;
begin
    select to_json(t)
    into resource
    from (
        select
            o.*,
            find_reference_by_id(o.target_id, o.target_reference) as target,
            find_citizen_by_id(o.created_by_id) as created_by,
            to_json(ol) as choice
        from "opinion" as o
        join opinion_choice ol on o.choice_id = ol.id
        where o.target_id = _target_id
          and o.created_by_id = _created_by_id
          and o.choice_id = _choice_id
    ) as t;
end;
$$;


