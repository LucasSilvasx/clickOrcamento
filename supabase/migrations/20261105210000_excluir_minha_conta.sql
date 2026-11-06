-- Exclusão da própria conta (direito de eliminação da LGPD). O app só tem a chave
-- publicável, que não pode apagar usuários, por isso a função roda como dona (definer).
-- Só age sobre auth.uid(): não recebe parâmetro que permita apontar para outra conta.
create or replace function public.excluir_minha_conta()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_usuario uuid := auth.uid();
begin
  if v_usuario is null then
    raise exception 'usuário não autenticado' using errcode = '28000';
  end if;

  -- orcamento aponta para cliente e prestador com RESTRICT: apagar direto em auth.users
  -- faria a cascata esbarrar nessas chaves, conforme a ordem em que dispara.
  delete from public.orcamento where usuario_id = v_usuario;
  delete from public.cliente where usuario_id = v_usuario;
  delete from public.prestador where usuario_id = v_usuario;
  delete from auth.users where id = v_usuario;
end;
$$;

revoke all on function public.excluir_minha_conta() from public, anon;
grant execute on function public.excluir_minha_conta() to authenticated;
