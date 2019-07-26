create or replace procedure find_user_by_id(in id uuid, inout resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(u) into resource
    from "user" as u
    where u.id = _id;
end;
$$;

-- drop procedure if exists find_user_by_id(inout json);