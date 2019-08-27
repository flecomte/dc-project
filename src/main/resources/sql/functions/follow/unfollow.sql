create or replace function unfollow(reference regclass, _target_id uuid, _created_by_id uuid) returns void
    language plpgsql as
$$
begin
    delete
    from follow f
    where f.created_by_id = _created_by_id
      and f.target_id = _target_id
      and f.target_reference = reference;
end;
$$;

-- drop function if exists unfollow(regclass, uuid, uuid);