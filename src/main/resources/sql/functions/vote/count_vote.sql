create or replace function count_vote(reference regclass, _target_id uuid, out resource json)
    language plpgsql as
$$
declare
    agg jsonb;
    empty jsonb = '{"down":0,"neutral":0,"up":0}'::jsonb;
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
        where v.target_reference = reference
          and v.target_id = _target_id
        group by v.note
        order by v.note
    ) t;

    resource = coalesce(agg, empty) || jsonb_build_object('updated_at', now());
end;
$$;

-- drop function if exists count_vote(regclass,uuid);