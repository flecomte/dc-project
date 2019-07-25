drop trigger generate_version_number_trigger on article;
drop table article;
drop function generate_version_number(regclass, uuid);
drop function set_version_number();