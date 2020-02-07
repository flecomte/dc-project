create or replace function find_citizen_opinions_by_target_ids(
    _citizen_id uuid,
    _ids uuid[],
    out resource json
) language plpgsql as
$$
begin
        select
            jsonb_agg(find_citizen_opinions_by_target_id(_citizen_id, o)) into resource
        from unnest(_ids) o

        order by
            _ids
        limit 100;
end;
$$;

-- drop function if exists find_citizen_votes_by_target_ids(uuid, uuid[], regclass);
