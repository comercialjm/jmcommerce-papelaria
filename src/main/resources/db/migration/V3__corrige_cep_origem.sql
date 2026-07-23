-- V3: Corrige o CEP de origem da loja, que estava com um valor placeholder
-- inválido (00000-000) desde a V1, impedindo qualquer cotação de frete de
-- funcionar (o Melhor Envio não consegue calcular rota partindo de um CEP
-- inexistente). Este será substituído por um campo editável no admin (M8),
-- mas até lá o valor real evita esse tipo de falha silenciosa.

UPDATE configuracao_loja SET cep_origem = '28621-350';
