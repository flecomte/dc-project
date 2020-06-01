create or replace function find_follow(
    _target_id uuid,
    _citizen_id uuid,
    _target_reference regclass,
    out resource json,
    out following boolean
) language plpgsql as
$$
declare
    _target_ids uuid[];
begin
    if (_target_reference = 'article'::regclass) then
        select array_agg(a2.id) into _target_ids
        from article a1
        join article a2 using (version_id)
        where a1.id = _target_id;

        select to_json(t) into resource from (
            select
                f.*,
                json_build_object('id', f.target_id, 'reference', f.target_reference) as target,
                find_citizen_by_id_with_user(f.created_by_id) as created_by
            from follow as f
            where f.created_by_id = _citizen_id
              and array[f.target_id] <@ _target_ids
            limit 1
        ) as t;
    else
        select to_json(t)
        into resource
        from (
            select
                f.*,
                json_build_object('id', f.target_id, 'reference', f.target_reference) as target,
                find_citizen_by_id_with_user(f.created_by_id) as created_by
            from follow as f
            where f.created_by_id = _citizen_id
              and f.target_id = _target_id
        ) as t;
    end if;

    select resource is not null into following;
end;
$$;
