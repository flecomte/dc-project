create or replace function find_citizen_by_email(_email text, out resource json) language plpgsql as
$$
begin
    select to_json(t) into resource
    from (
        select
            z.*,
            find_user_by_id(z.user_id) as "user"
        from citizen as z
        where z.email = _email
    ) as t;
end;
$$;

-- drop function if exists find_citizen_by_email(text, out json);