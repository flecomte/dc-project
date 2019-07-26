create or replace function find_user_by_id(in id uuid, out resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_jsonb(u) - 'password' into resource
    from "user" as u
    where u.id = _id;
end;
$$;

-- drop function if exists find_user_by_id(uuid, out json);