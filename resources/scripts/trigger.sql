create or replace function notify_event() returns trigger as
$$
declare
  data         json;
  notification json;

begin

  data = row_to_json(new);

  notification = json_build_object(
      'action', tg_op,
      'data', data
    );

  PERFORM pg_notify('im_events', notification::text);

  return null;

end;

$$ language plpgsql;

-- create trigger
create trigger mail_message_create_hook
  after insert on mail_message
  for each row execute procedure notify_event();

-- drop trigger
drop trigger mail_message_create_hook on mail_message;
