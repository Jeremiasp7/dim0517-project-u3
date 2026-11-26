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
```

## Endpoints
| Operação | Requisição | Resposta |
|---|---|---|
| Cadastrar Produto | `POST /loja/produto` `{"codigo":123,"preco":59.90}` | `201` `{"codigo":123,"preco":59.90,"estoque":0}`; `409` se já existe; `400` |
| Consultar Estoque | `GET /loja/produto/{codigo}/estoque` | `200` `{"codigo":123,"estoque":0}`; `404` se não existe |

`400`: campo ausente ou com tipo inválido (ex.: `"abc"`, `2.5` em `codigo`) ou preço negativo.

## Uso direto da camada de serviço (testes unitários futuros)
```java
LojaService s = new LojaService();
s.cadastrarProduto(123, new BigDecimal("59.90"));
int estoque = s.consultarEstoque(123);
```