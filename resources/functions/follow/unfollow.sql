create or replace function unfollow(reference regclass, target_id uuid, citizen_id uuid) returns void
    language plpgsql as
$$
declare
    _citizen_id alias for citizen_id;
    _target_id alias for target_id;
begin
    delete
    from follow f
    where f.citizen_id = _citizen_id
      and f.target_id = _target_id
      and f.target_reference = reference;
end;
$$;

-- drop function if exists unfollow(regclass, uuid, uuid);