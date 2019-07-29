create or replace procedure upsert_citizen(inout resource json)
    language plpgsql as
$$
declare
    new_id uuid;
begin
    insert into citizen (id, name, birthday, user_id, vote_annonymous, follow_annonymous)
    select
       coalesce(id, uuid_generate_v4()),
       name,
       birthday,
       (resource#>>'{user, id}')::uuid,
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

    select find_citizen_by_id(new_id) into resource;
end;
$$;

-- drop procedure if exists insert_user(inout json);