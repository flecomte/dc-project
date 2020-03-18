create or replace function find_follow(
    _target_id uuid,
    _citizen_id uuid,
    out resource json,
    out following boolean
) language plpgsql as
$$
begin
    select to_json(t)
    into resource
    from (
        select
            f.*,
            json_build_object('id', f.target_id, 'reference', f.target_reference) as target,
            find_citizen_by_id(f.created_by_id) as created_by
        from follow as f
        where f.created_by_id = _citizen_id
          and f.target_id = _target_id
    ) as t;

    select resource is not null into following;
end;
$$;
