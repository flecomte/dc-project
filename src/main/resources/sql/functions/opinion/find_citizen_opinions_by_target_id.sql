create or replace function find_citizen_opinions_by_target_id(
    _citizen_id uuid,
    _id uuid,
    out resource json
) language plpgsql as
$$
begin
    select
        json_agg(t)
    into resource
    from (
        select
            o.*,
            ol.name
        from opinion as o
        join opinion_list ol on o.opinion = ol.id

        where target_id = _id
          and created_by_id = _citizen_id

        order by
            ol.name
        limit 100
    ) as t;
end;
$$;

-- drop function if exists find_citizen_votes_by_target_ids(uuid, uuid[], regclass);
