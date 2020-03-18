create or replace function check_user(in username text, in plain_password text, out resource json) language plpgsql as
$$
declare
    _username alias for username;
begin
    select
       case when count(u) = 1
       then to_jsonb(u) - 'password'
       else null end
    into resource
    from "user" as u
    where u.username = lower(_username)
      and u.password = crypt(plain_password, u.password)
    group by u;
end;
$$;

