create or replace function insert_user(inout resource json) language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into "user" (id, username, password, blocked_at)
    select
        coalesce(t.id, uuid_generate_v4()),
        t.username,
        crypt(resource->>'plain_password', gen_salt('bf', 8)),
        case when t.blocked_at is not null then now() else null end
    from json_populate_record(null::"user", resource) t
    returning id into new_id;

    select find_user_by_id(new_id) into resource;
end;
$$;

-- drop function if exists insert_user(inout json);