-- User
create or replace view user_lite as
select u.id, u.created_at, u.blocked_at, u.username
from "user" u
where u.blocked_at is null;