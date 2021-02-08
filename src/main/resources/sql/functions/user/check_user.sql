create or replace function check_user(in _username text, in _password text, out resource json) language plpgsql as
$$
begin
    select
       case when count(u) = 1
       then to_jsonb(u) - 'password'
       else null end
    into resource
    from "user" as u
    where u.username = lower(_username)
      and u.password = crypt(_password, u.password)
    group by u;
end;
$$;

