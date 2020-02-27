create or replace function find_citizen_votes_by_target_ids(
    _citizen_id uuid,
    _ids uuid[],
    _reference regclass default null,
    out resource json
) language plpgsql as
$$
begin
    select
        json_agg(t)
    into resource
    from (
        select
            v.*,
            find_reference_by_id(v.target_id, v.target_reference) as target,
            find_citizen_by_id(v.created_by_id) as created_by

        from vote as v

        where
            (_reference is null or _reference = target_reference)
          and target_id = any(_ids)
          and created_by_id = _citizen_id

        order by
            _ids
        limit 100
    ) as t;
end;
$$;

-- drop function if exists find_citizen_votes_by_target_ids(uuid, uuid[], regclass);
