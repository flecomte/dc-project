create or replace function insert_user(inout resource json) language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into "user" (id, username, password, blocked_at, roles)
    select
        coalesce(t.id, uuid_generate_v4()),
        t.username,
        crypt(resource->>'plain_password', gen_salt('bf', 8)),
        case when t.blocked_at is not null then now() else null end,
        t.roles
    from json_populate_record(null::"user", resource) t
    returning id into new_id;

    select find_user_by_id(new_id) into resource;
end;
$$;

