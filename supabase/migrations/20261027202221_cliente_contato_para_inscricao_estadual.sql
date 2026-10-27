-- Aplicada em 27/10/2026 (versão 20261027202221 no Supabase).
--
-- O campo "Contato" do cliente vira "Inscrição estadual" (IE).
-- Renomear mantém o tipo e o padrão (text NOT NULL DEFAULT '').
alter table public.cliente rename column contato to inscricao_estadual;
