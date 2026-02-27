# Coupons API

API REST para gerenciamento de cupons de desconto com regras de negócio específicas.

## Arquitetura

Padrão MVC com separação de responsabilidades:

```
Controller → Service Interface → Service Implementation → Repository → Model
```

Estrutura de pacotes:

```
com.outforce.desafio/
├── config/           Configurações (Swagger)
├── controller/       Controllers REST
├── document/         Interfaces de documentação da API
├── dto/              DTOs Request/Response (Java Records)
├── exception/        Exceções e tratamento global
├── model/            Entidades JPA (Lombok)
├── repository/       Repositories JPA
└── service/          Interface + Implementação
    └── impl/
```

## Regras de Negócio

### CREATE
- Código alfanumérico de 6 caracteres
- Sanitização de caracteres especiais (AB-12$C3 → AB12C3)
- Desconto mínimo de 0.5 (sem máximo)
- Data de expiração não pode estar no passado
- Cupom pode ser criado como publicado
- Campos obrigatórios: code, description, discountValue, expirationDate

### DELETE
- Soft delete (campo deletedAt mantém histórico)
- Não permite deletar cupom já deletado (retorna 409 Conflict)
- Retorna 404 se cupom não existe

## Executar

### Localmente
```bash
./mvnw spring-boot:run
```

### Docker

Build e iniciar:
```bash
docker-compose up --build
```

Iniciar em background:
```bash
docker-compose up -d
```

Parar:
```bash
docker-compose down
```

**Usar porta diferente:**

Editar `.env` e mudar `APP_PORT`:
```env
APP_PORT=8081
```

Ou via comando:
```bash
APP_PORT=8081 docker-compose up --build
```

Após subir, a aplicação estará disponível em:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:mem:desafio
  - User: sa
  - Password: (vazio)

**Nota:** Se porta 8080 estiver em uso, consulte `SOLUCAO_PORTA_8080.md`

## Testes

```bash
./mvnw test
```

36 testes unitários com JUnit 5 e Mockito:
- 21 testes de Service (regras de negócio)
- 14 testes de Controller (endpoints REST)
- 1 teste de contexto Spring
- Execução: ~2 segundos
- Cobertura: 100% das regras de negócio

Cobertura JaCoCo:
```bash
./mvnw test
open target/site/jacoco/index.html
```



## Documentação

A API é documentada automaticamente usando a especificação OpenAPI 3.0.

- Interface visual (Swagger UI): http://localhost:8080/swagger-ui.html
- Especificação OpenAPI (JSON): http://localhost:8080/api-docs
- Console H2 Database: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:desafio`
  - User: `sa`
  - Password: (vazio)

## Endpoints

### POST /coupons

Cria um novo cupom.

Request:
```json
{
  "code": "AB-12$C3",
  "description": "Summer sale",
  "discountValue": 10.00,
  "expirationDate": "2026-12-31",
  "published": true
}
```

Response 201 Created:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "AB12C3",
  "description": "Summer sale",
  "discountValue": 10.00,
  "expirationDate": "2026-12-31",
  "status": "ACTIVE",
  "published": true,
  "redeemed": false
}
```

### DELETE /coupons/{id}

Deleta um cupom (soft delete).

Responses:
- 204 No Content: Cupom deletado com sucesso
- 404 Not Found: Cupom não encontrado
- 409 Conflict: Cupom já foi deletado

## Tecnologias

- Java 17
- Spring Boot 3.2.2
- Spring Data JPA
- H2 Database (in-memory)
- SpringDoc (OpenAPI 3 + Swagger UI)
- Lombok (Builder Pattern)
- JaCoCo (cobertura de testes)
- JUnit 5 + Mockito
- Spring Boot Actuator

