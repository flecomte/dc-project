create or replace procedure find_citizen_by_user_id(in id uuid, inout resource json) language plpgsql as
$$
declare
    _id alias for id;
begin
    select to_json(z) into resource
    from citizen as z
    where z.user_id = _id;
end;
$$;

-- drop procedure if exists find_citizen_by_user_id(uuid, inout json);