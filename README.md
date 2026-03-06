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


## Kubernetes

### Manifestos

Arquivos Kubernetes em `k8s/`:
- `namespace.yaml` - Namespace isolado
- `deployment.yaml` - 2 réplicas com health checks
- `service.yaml` - Service ClusterIP
- `ingress.yaml` - Ingress NGINX
- `kustomization.yaml` - Configuração Kustomize

### Opção 1: Cluster Local com Kind (Recomendado para Desenvolvimento)

**Por que Kind?**
- Clusters Kubernetes em containers Docker
- Rápido para criar/destruir
- Isolado do sistema host
- Suporte a múltiplos nós
- Perfeito para CI/CD e testes locais

**Pré-requisitos:**
- Docker Desktop instalado e rodando
- kubectl instalado
- Kind instalado: `winget install Kubernetes.kind`

**1. Criar cluster Kind:**

```bash
# Criar com configuração customizada (portas 80/443 mapeadas)
kind create cluster --config kind-config.yaml --image kindest/node:v1.31.4
```

O arquivo `kind-config.yaml` já está no projeto e configura:
- Mapeamento de portas 80 e 443 para Ingress
- Labels necessários para ingress-ready

**2. Build e carregar imagem:**

```bash
# Build da imagem
docker build -t desafio:latest .

# Carregar no Kind
kind load docker-image desafio:latest --name desafio
```

**3. Instalar NGINX Ingress Controller:**

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

# Aguardar ficar pronto
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

**4. Deploy da aplicação:**

```bash
kubectl apply -k k8s
```

**5. Verificar deployment:**

```bash
kubectl -n desafio rollout status deployment/desafio-app
kubectl -n desafio get pods,svc,ingress
```

**6. Testar a aplicação:**

Via localhost:
```powershell
# Health check
Invoke-WebRequest -UseBasicParsing -Uri "http://localhost/actuator/health" -Headers @{ Host = "desafio.local" }

# Criar cupom
$body = '{"code":"TEST01","description":"Test Coupon","discountValue":10.5,"expirationDate":"2026-12-31","published":true}'
Invoke-WebRequest -UseBasicParsing -Method POST -Uri "http://localhost/coupons" -Headers @{ Host = "desafio.local"; "Content-Type" = "application/json" } -Body $body
```

Opcional - Adicionar ao arquivo hosts (`C:\Windows\System32\drivers\etc\hosts`):
```
127.0.0.1 desafio.local
```

Depois pode acessar diretamente: http://desafio.local

**7. Limpar recursos:**

```bash
# Deletar recursos da aplicação
kubectl delete -k k8s

# Deletar cluster Kind
kind delete cluster --name desafio
```

### Opção 2: Registry Remoto (Produção)

Para publicar em um registry remoto (GitHub Container Registry, Docker Hub, etc.):

**1. Build e push da imagem:**

```bash
docker build -t ghcr.io/SEU_USUARIO/desafio:0.0.1 .
docker push ghcr.io/SEU_USUARIO/desafio:0.0.1
```

**2. Atualizar `k8s/kustomization.yaml`:**

```yaml
images:
  - name: desafio
    newName: ghcr.io/SEU_USUARIO/desafio
    newTag: 0.0.1
```

**3. Aplicar:**

```bash
kubectl apply -k k8s
```

### Comandos Úteis

```bash
# Ver logs
kubectl -n desafio logs -f deployment/desafio-app

# Exec no pod
kubectl -n desafio exec -it deployment/desafio-app -- sh

# Describe pod (troubleshooting)
kubectl -n desafio describe pod <pod-name>

# Ver eventos
kubectl -n desafio get events --sort-by='.lastTimestamp'

# Port-forward direto (sem ingress)
kubectl -n desafio port-forward deployment/desafio-app 8080:8080
```

### Integração Docker + Kubernetes

O **Docker** é usado em duas etapas do processo Kubernetes:

1. **Build da aplicação**: Docker constrói a imagem baseada no `Dockerfile`
2. **Runtime do Kind**: Kind usa o Docker para rodar os containers que simulam os nós do cluster

Fluxo completo:
```
Dockerfile → docker build → Imagem Docker → kind load → Pod Kubernetes
```

## Testes

```bash
./mvnw test
```


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

