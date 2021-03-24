create or replace function change_user_password(resource json) returns void language plpgsql as
$$
begin
    update "user"
    set password = crypt(resource->>'password', gen_salt('bf', 8))
    where id = (resource->>'id')::uuid;

    return;
end;
$$;

