create or replace function find_opinion_by_id(
    _id uuid,
    out resource json
) language plpgsql as
$$
begin
    select to_json(t)
    into resource
    from (
        select
            o.*,
            find_reference_by_id(o.target_id, o.target_reference) as target,
            find_citizen_by_id_with_user(o.created_by_id) as created_by,
            to_json(ol) as choice
        from "opinion" as o
        join opinion_choice ol on o.choice_id = ol.id
        where o.id = _id
    ) as t;
end;
$$;


