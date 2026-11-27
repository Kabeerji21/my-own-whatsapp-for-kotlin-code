-- 1. Profiles Table (யூசர் விவரங்கள்)
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text not null,
  display_name text not null,
  created_at timestamptz default now()
);

alter table public.profiles enable row level security;

create policy "Anyone authenticated can view profiles"
on public.profiles for select to authenticated using (true);

create policy "Users can insert own profile"
on public.profiles for insert to authenticated with check (auth.uid() = id);

create policy "Users can update own profile"
on public.profiles for update to authenticated using (auth.uid() = id);

-- 2. Messages Table (Real-time Chat)
create table if not exists public.messages (
  id uuid primary key default gen_random_uuid(),
  sender_id uuid not null references auth.users(id) on delete cascade,
  receiver_id uuid not null references auth.users(id) on delete cascade,
  content text not null check (char_length(trim(content)) > 0),
  created_at timestamptz not null default now()
);

alter table public.messages enable row level security;

create policy "Users can read own messages"
on public.messages for select to authenticated
using (auth.uid() = sender_id or auth.uid() = receiver_id);

create policy "Users can send messages"
on public.messages for insert to authenticated
with check (auth.uid() = sender_id);

-- 3. Realtime Enable
alter publication supabase_realtime add table public.messages;
