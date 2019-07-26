create or replace procedure insert_user(inout resource json) language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into "user" (username, password, blocked_at)
    select
        username,
        crypt(resource->>'plain_password', gen_salt('bf', 8)),
        case when blocked_at is not null then now() else null end
    from json_populate_record(null::"user", resource)
    returning id into new_id;

    select to_json(u) into resource
    from "user" as u
    where u.id = new_id;
end;
$$;

-- drop procedure if exists insert_user(inout json);