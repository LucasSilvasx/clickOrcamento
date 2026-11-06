-- Numeração dos orçamentos passa a ser por empresa (prestador), não por usuário.
--   Empresa nova            -> o primeiro orçamento é o nº 1.
--   Empresa que já emitiu   -> continua do maior número dela + 1.
-- Substitui o "número inicial por usuário" (configuracao_usuario), que fica sem uso:
-- quem já tem orçamentos continua a sequência pelo maior número da própria empresa.

-- 1) Unicidade por empresa (antes era por usuário).
--    O índice único (prestador_id, numero) também atende o max() do trigger e
--    a FK prestador_id, por isso o índice simples de prestador_id sai.
alter table public.orcamento drop constraint orcamento_usuario_id_numero_key;
alter table public.orcamento
  add constraint orcamento_prestador_id_numero_key unique (prestador_id, numero);
drop index if exists public.orcamento_prestador_idx;
-- orcamento_usuario_numero_idx (usuario_id, numero desc) fica: é o da lista.

-- 2) Trigger de numeração.
create or replace function public.definir_numero_orcamento()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  -- UPDATE: só renumera se a empresa mudou (o número de uma empresa não
  -- serve na outra). Qualquer outra edição mantém o número.
  if tg_op = 'UPDATE' and new.prestador_id is not distinct from old.prestador_id then
    return new;
  end if;
  -- INSERT com número explícito: respeita (mesmo comportamento de antes).
  if tg_op = 'INSERT' and coalesce(new.numero, 0) <> 0 then
    return new;
  end if;

  -- A FK não passa pelo RLS: sem esta checagem, daria para apontar para a
  -- empresa de outro usuário (se alguém descobrisse o id) e "gastar" números dela.
  if not exists (
    select 1 from public.prestador p
     where p.id = new.prestador_id and p.usuario_id = new.usuario_id
  ) then
    raise exception 'empresa não encontrada para este usuário'
      using errcode = 'P0001';
  end if;

  -- Trava por empresa até o fim da transação: dois orçamentos criados ao
  -- mesmo tempo esperam a vez em vez de pegar o mesmo número.
  perform pg_advisory_xact_lock(hashtextextended(new.prestador_id::text, 0));

  select coalesce(max(o.numero), 0) + 1
    into new.numero
    from public.orcamento o
   where o.prestador_id = new.prestador_id;

  return new;
end;
$$;

drop trigger if exists trg_orcamento_numero on public.orcamento;
create trigger trg_orcamento_numero
  before insert or update of prestador_id on public.orcamento
  for each row execute function public.definir_numero_orcamento();

-- 3) O número inicial por usuário deixa de existir.
drop table public.configuracao_usuario;
