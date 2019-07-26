create or replace procedure find_user_by_username(in username text, inout resource json) language plpgsql as
$$
declare
    _username alias for username;
begin
    select to_json(u) into resource
    from "user" as u
    where u.username = _username;
end;
$$;

-- drop procedure if exists find_user_by_username(text, inout json);