# Sistema de Loja Virtual

Projeto da terceira unidade para a disciplina de Gerência de 
Configuração e Mudanças (DIM-0517). Sem front-end: 
tudo é executado pelo terminal.

## Arquitetura
| Camada | Pacote | Responsabilidade |
|---|---|---|
| Serviço | `br.ufrn.loja.service` | Regras de negócio (`LojaService`), independente da API |
| API REST | `br.ufrn.loja.api` | Traduz HTTP ↔ serviço, sem regras de negócio |

`br.ufrn.loja.util.Json` é um leitor/escritor JSON mínimo (sem dependências externas).

## Requisitos
JDK 17+ e Maven 3.8+.

## Executar
```bash
mvn package
java -jar target/loja-virtual-1.0.0.jar [porta]   # padrão 8080
```

## Usar pelo terminal (curl)
```bash
# Cadastrar produto
curl -i -X POST localhost:8080/loja/produto -H "Content-Type: application/json" \
     -d '{"codigo":123,"preco":59.90}'

# Consultar estoque
curl -i localhost:8080/loja/produto/123/estoque

# Entrada de estoque (reposição)
curl -i -X PUT localhost:8080/loja/produto/123/entrada -H "Content-Type: application/json" \
     -d '{"quantidade":10}'

# Saída de estoque (perda, avaria ou ajuste)
curl -i -X PUT localhost:8080/loja/produto/123/saida -H "Content-Type: application/json" \
     -d '{"quantidade":5}'

# Venda
curl -i -X POST localhost:8080/loja/venda -H "Content-Type: application/json" \
     -d '{"itens":[{"codigo":123,"quantidade":2},{"codigo":456,"quantidade":1}]}'
```

## Endpoints
| Operação | Requisição | Resposta |
|---|---|---|
| Cadastrar Produto | `POST /loja/produto` `{"codigo":123,"preco":59.90}` | `201` `{"codigo":123,"preco":59.90,"estoque":0}`; `409` se já existe; `400` |
| Consultar Estoque | `GET /loja/produto/{codigo}/estoque` | `200` `{"codigo":123,"estoque":0}`; `404` se não existe |
| Entrada de Estoque | `PUT /loja/produto/{codigo}/entrada` `{"quantidade":10}` | `200` `{"codigo":123,"estoque":10}`; `404` se não existe; `400` |
| Saída de Estoque | `PUT /loja/produto/{codigo}/saida` `{"quantidade":5}` | `200` `{"codigo":123,"estoque":5}`; `404` se não existe; `400` |
| Venda | `POST /loja/venda` `{"itens":[{"codigo":123,"quantidade":2},{"codigo":456,"quantidade":1}]}` | `201` `{"total":189.70,"itens":[{"codigo":123,"quantidade":2,"subtotal":119.80,"estoque":13},{"codigo":456,"quantidade":1,"subtotal":69.90,"estoque":7}]}`; `404` se algum produto não existe; `400` |

`400`: campo ausente ou com tipo inválido (ex.: `"abc"`, `2.5` em `codigo` ou `quantidade`), preço negativo, quantidade menor ou igual a zero, venda maior que o estoque disponível, ou lista de itens ausente ou vazia. Em caso de erro, nenhum estoque é alterado.

A saída de estoque pode deixar o saldo negativo.

A venda é atômica: todos os itens são validados antes de qualquer baixa. Se algum item for inválido, o estoque de nenhum produto é alterado.

## Testes unitários

Execute com `mvn test`.

## Uso direto da camada de serviço
```java
LojaService s = new LojaService();
s.cadastrarProduto(123, new BigDecimal("59.90"));
int estoque = s.consultarEstoque(123);
s.entradaEstoque(123, 10);
s.saidaEstoque(123, 5);
Venda venda = s.realizarVenda(List.of(new ItemVenda(123, 2)));
```