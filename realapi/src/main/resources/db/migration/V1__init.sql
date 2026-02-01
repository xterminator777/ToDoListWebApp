create table users (
  id bigserial primary key,
  email varchar(255) not null unique,
  password_hash varchar(255) not null,
  created_at timestamptz not null default now()
);

create table todos (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  title varchar(255) not null,
  done boolean not null default false,
  created_at timestamptz not null default now()
);

create index idx_todos_user_id on todos(user_id);
