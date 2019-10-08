create or replace function upsert_citizen(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into citizen (id, name, birthday, user_id, vote_anonymous, follow_anonymous, email)
    select
       coalesce(id, uuid_generate_v4()),
       name,
       birthday,
       (resource#>>'{user, id}')::uuid,
       coalesce(vote_anonymous, true),
       coalesce(follow_anonymous, true),
       email
    from json_populate_record(null::citizen, resource)
    on conflict (id) do update set
        name = excluded.name,
        birthday = excluded.birthday,
        user_id = excluded.user_id,
        vote_anonymous = excluded.vote_anonymous,
        follow_anonymous = excluded.follow_anonymous,
        email = excluded.email
    returning id into new_id;

    select find_citizen_by_id(new_id) into resource;
end;
$$;

-- drop function if exists upsert_citizen(inout json);