create or replace function insert_citizen_with_user(inout resource json)
    language plpgsql as
$$
declare
    new_id        uuid;
    inserted_user json;
begin
    select insert_user(resource->'user') into inserted_user;

    insert into citizen (id, name, birthday, user_id, vote_annonymous, follow_annonymous)
    select
       coalesce(id, uuid_generate_v4()),
       name,
       birthday,
       (inserted_user->>'id')::uuid,
       coalesce(vote_annonymous, true),
       coalesce(follow_annonymous, true)
    from json_populate_record(null::citizen, resource)
    on conflict (id) do update set
        name = excluded.name,
        birthday = excluded.birthday,
        user_id = excluded.user_id,
        vote_annonymous = excluded.vote_annonymous,
        follow_annonymous = excluded.follow_annonymous
    returning id into new_id;

    select find_citizen_by_id_with_user(new_id) into resource;
end;
$$;

-- drop function if exists create_citizen_with_user(inout json);