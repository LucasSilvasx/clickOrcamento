-- FKs não passam pelo RLS: sem esta checagem, um usuário que soubesse o id
-- de um cliente (ou empresa) de outra pessoa conseguiria ligar esse registro
-- a um orçamento próprio. Roda em todo INSERT e em UPDATE das colunas de dono.
-- (A checagem da empresa que existia no trigger de numeração era pulada quando
--  o INSERT vinha com número explícito; esta vale sempre.)
create or replace function public.conferir_dono_orcamento()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
  if not exists (
    select 1 from public.cliente c
     where c.id = new.cliente_id and c.usuario_id = new.usuario_id
  ) then
    raise exception 'cliente não encontrado para este usuário'
      using errcode = 'P0001';
  end if;

  if not exists (
    select 1 from public.prestador p
     where p.id = new.prestador_id and p.usuario_id = new.usuario_id
  ) then
    raise exception 'empresa não encontrada para este usuário'
      using errcode = 'P0001';
  end if;

  return new;
end;
$$;

-- Nome em ordem alfabética antes de trg_orcamento_numero: confere o dono
-- antes de gerar o número.
drop trigger if exists trg_orcamento_dono on public.orcamento;
create trigger trg_orcamento_dono
  before insert or update of cliente_id, prestador_id, usuario_id on public.orcamento
  for each row execute function public.conferir_dono_orcamento();
