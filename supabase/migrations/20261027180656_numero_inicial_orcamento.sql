-- Preferências por usuário (uma linha por usuário).
-- Primeiro uso: número inicial dos orçamentos, para quem migra de outra
-- ferramenta (ex.: planilha) e precisa continuar a sequência.
create table public.configuracao_usuario (
  usuario_id uuid primary key default auth.uid()
    references auth.users(id) on delete cascade,
  numero_inicial_orcamento integer not null default 1
    check (numero_inicial_orcamento >= 1),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.configuracao_usuario enable row level security;

-- A chave primária já indexa usuario_id (coluna usada na policy).
create policy "configuracao_usuario é do dono"
  on public.configuracao_usuario
  for all
  to authenticated
  using ((select auth.uid()) = usuario_id)
  with check ((select auth.uid()) = usuario_id);

create trigger trg_configuracao_usuario_updated_at
  before update on public.configuracao_usuario
  for each row execute function public.set_updated_at();

-- Numeração: o próximo número é o maior entre (último + 1) e o número
-- inicial configurado. Sem configuração, o piso é 1 (comportamento antigo).
create or replace function public.definir_numero_orcamento()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  if new.numero is null or new.numero = 0 then
    select greatest(
             coalesce(max(o.numero), 0) + 1,
             coalesce((select c.numero_inicial_orcamento
                         from public.configuracao_usuario c
                        where c.usuario_id = new.usuario_id), 1)
           )
      into new.numero
      from public.orcamento o
     where o.usuario_id = new.usuario_id;
  end if;
  return new;
end;
$$;

-- Dados (feito à parte, não faz parte da migration):
--   insert into public.configuracao_usuario (usuario_id, numero_inicial_orcamento)
--   values ('<id do usuário>', 15);
--   + exclusão do orçamento nº 1, que era de teste.
