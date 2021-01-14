create or replace function json_to_array(json json) returns text[] language sql
    immutable parallel safe as
$$
    select array(select json_array_elements_text(json))
$$;