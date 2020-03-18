create or replace function count_opinion(_target_id uuid, out resource json)
    language plpgsql as
$$
declare
    agg jsonb;
    empty jsonb = '{}'::jsonb;
begin
    select jsonb_object_agg(t.label, t.total)
    into agg
    from (
        select
            count(o) as total,
            ol.name as label
        from opinion o
        join opinion_choice ol on o.choice_id = ol.id
        where o.target_id = _target_id
        group by ol.name
        order by ol.name
    ) t;

    resource = coalesce(agg, empty);
end;
$$;
