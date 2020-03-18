create or replace function count_vote(_target_id uuid, out resource json)
    language plpgsql as
$$
declare
    agg jsonb;
    empty jsonb = '{"down":0,"neutral":0,"up":0,"total":0,"score":0}'::jsonb;
    score int;
    percent numeric;
    total int;
begin
    select jsonb_object_agg(t.label, t.total)
    into agg
    from (
        select
            count(v.note) as total,
            (case v.note
                when -1 then 'down'
                when  0 then 'neutral'
                when  1 then 'up'
            end) as label
        from vote v
        where v.target_id = _target_id
        group by v.note
        order by v.note
    ) t;

    agg = empty || coalesce(agg, empty);
    score = ((agg->>'up')::int - (agg->>'down')::int);
    percent = ((agg->>'up')::int - (agg->>'down')::int);
    total = ((agg->>'up')::int + (agg->>'down')::int + (agg->>'neutral')::int);

    resource = agg ||
               jsonb_build_object('updated_at', now()) ||
               jsonb_build_object('total', total) ||
               jsonb_build_object('score', score) ||
               jsonb_build_object('percent', percent);
end;
$$;
