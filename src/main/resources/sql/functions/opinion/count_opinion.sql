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
            count(o.opinion) as total,
            ol.name as label
        from opinion o
        join opinion_list ol on o.opinion = ol.id
        where o.target_id = _target_id
        group by ol.name
        order by ol.name
    ) t;

    resource = coalesce(agg, empty);
end;
$$;

-- drop function if exists count_opinion(uuid);

-- select * from count_opinion('d91aa0cd-61d6-83cc-41bb-8d5656e130f7');