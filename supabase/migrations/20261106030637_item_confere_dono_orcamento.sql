-- FKs não passam pelo RLS: sem esta checagem, um usuário que soubesse o id de um orçamento
-- de outra pessoa conseguiria criar itens apontando para ele. O item fica invisível para o
-- dono, mas ocupa a posição em (orcamento_id, ordem) e faz o salvamento dele falhar.
create or replace function public.conferir_dono_item_orcamento()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  if not exists (
    select 1 from public.orcamento o
     where o.id = new.orcamento_id and o.usuario_id = new.usuario_id
  ) then
    raise exception 'orçamento não encontrado para este usuário'
      using errcode = 'P0001';
  end if;

  return new;
end;
$$;

drop trigger if exists trg_item_orcamento_dono on public.item_orcamento;
create trigger trg_item_orcamento_dono
  before insert or update of orcamento_id, usuario_id on public.item_orcamento
  for each row execute function public.conferir_dono_item_orcamento();
