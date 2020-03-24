create or replace function delete_workgroup(_id uuid) returns void
    language plpgsql as
$$
begin
    update workgroup set deleted_at = now()
    where id = _id;
end;
$$;

