create or replace function change_user_password(resource json) returns void language plpgsql as
$$
begin
    update "user"
    set password = crypt(resource->>'plain_password', gen_salt('bf', 8))
    where id = (resource->>'id')::uuid;

    return;
end;
$$;

-- drop function if exists change_user_password(json);