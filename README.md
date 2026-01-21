# Tecos API

API REST desenvolvida em Spring Boot seguindo Clean Architecture.

## Pré-requisitos

- Java 17+
- Maven 3.6+
- Docker e Docker Compose

## Configuração do Banco de Dados

### Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` e configure as variáveis:

```bash
cp .env.example .env
```

Edite o arquivo `.env` com suas configurações:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tecos_db
DB_USERNAME=tecos_user
DB_PASSWORD=tecos_password
DB_TIMEZONE=America/Sao_Paulo

POSTGRES_DB=tecos_db
POSTGRES_USER=tecos_user
POSTGRES_PASSWORD=tecos_password
```

**Nota**: O arquivo `.env` está no `.gitignore` e não será commitado. Use `.env.example` como referência.

### Iniciar o PostgreSQL com Docker Compose

```bash
docker-compose up -d
```

O Docker Compose irá ler as variáveis do arquivo `.env` automaticamente.

### Parar o banco de dados

```bash
docker-compose down
```

### Parar e remover volumes (apaga os dados)

```bash
docker-compose down -v
```

## Executando a Aplicação

Certifique-se de que o arquivo `.env` está configurado antes de executar:

```bash
# Iniciar o banco de dados primeiro
docker-compose up -d

# Opção 1: Usar o script start.sh (recomendado - carrega .env automaticamente)
./start.sh

# Opção 2: Carregar variáveis manualmente e executar
export $(cat .env | grep -v '^#' | xargs)
mvn spring-boot:run

# Opção 3: Executar diretamente (usa valores padrão do application.properties)
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### Configuração via Variáveis de Ambiente

Você também pode passar as variáveis diretamente:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=tecos_db
export DB_USERNAME=tecos_user
export DB_PASSWORD=tecos_password
mvn spring-boot:run
```

Ou inline:

```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=tecos_db DB_USERNAME=tecos_user DB_PASSWORD=tecos_password mvn spring-boot:run
```

## Monitoramento com Grafana e Prometheus

### Iniciar Stack de Monitoramento

```bash
# Iniciar PostgreSQL, Prometheus e Grafana
docker-compose up -d

# Verificar se todos os serviços estão rodando
docker-compose ps
```

### Acessar os Serviços

- **Grafana**: http://localhost:3000
  - Usuário: `admin`
  - Senha: `admin`
  
- **Prometheus**: http://localhost:9090

- **Métricas da API**: http://localhost:8081/actuator/prometheus
- **Status dos Targets no Prometheus**: http://localhost:9090/targets

### Dashboard do Grafana (Padrão RED - SRE)

Após iniciar o Grafana, o dashboard **"Tecos API - SRE Dashboard (RED Method)"** será carregado automaticamente seguindo as melhores práticas de SRE:

#### 🔴 RED Method (Core Metrics)
- **Rate (Taxa)**: Requisições por segundo (RPS) por método e URI
- **Errors (Erros)**: Taxa de erros HTTP (4xx/5xx) com thresholds coloridos
- **Duration (Duração)**: Latência P95 e P99 das requisições

#### 🟢 JVM Health
- **Heap Memory**: Uso e máximo de memória heap com alertas em 80% e 90%
- **Garbage Collection**: Tempo de pausa do GC (mostra o "serrilhado" do GC)
- **Threads**: Threads ativas e peak com alertas

#### 🔵 Database & Infrastructure
- **HikariCP Connection Pool**: Conexões ativas, idle e pendentes com alertas
- **System CPU**: Uso de CPU do sistema e do processo

#### 📊 Business Metrics
- **Empresas Criadas**: Taxa e total de empresas criadas
- **Operações de Empresas**: Criadas, atualizadas e deletadas por segundo
- **Duração das Operações**: P95 e P99 das operações de negócio

#### 📈 Summary Stats
- **Application Health**: Status UP/DOWN da aplicação
- **Total Requisições**: Total de requisições na última hora
- **Taxa de Erro**: Percentual de erros com cores (verde < 1%, amarelo < 5%, vermelho >= 5%)
- **Latência Média**: Latência média em milissegundos

### Métricas Customizadas (Business Metrics)

A aplicação expõe métricas customizadas seguindo padrão Prometheus:

- `tecos_companies_created_total`: Contador total de empresas criadas
- `tecos_companies_updated_total`: Contador total de empresas atualizadas
- `tecos_companies_deleted_total`: Contador total de empresas deletadas
- `tecos_companies_operation_duration_seconds`: Histograma da duração das operações (com tags `operation`: create, update, delete)

**Uso no Prometheus/Grafana:**
```promql
# Taxa de empresas criadas por segundo
rate(tecos_companies_created_total[5m])

# Percentil 95 da duração de operações
histogram_quantile(0.95, rate(tecos_companies_operation_duration_seconds_bucket[5m]))
```

### Alertas Configurados

O Grafana está configurado com alertas automáticos para:

- **Alta taxa de erros HTTP**: Alerta quando há mais de 5 erros/s por 5 minutos

### Configuração do Prometheus

O Prometheus está configurado para coletar métricas da aplicação Spring Boot em:
- `host.docker.internal:8081/actuator/prometheus`

**Nota**: Se estiver rodando em Linux nativo (não WSL), altere `host.docker.internal` para `localhost` no arquivo `monitoring/prometheus.yml`.

## Endpoints da API

### Companies

- `GET /api/companies` - Lista empresas (com filtros e paginação)
- `POST /api/companies` - Cria uma nova empresa
- `GET /api/companies/{id}` - Busca empresa por ID
- `PUT /api/companies/{id}` - Atualiza empresa
- `DELETE /api/companies/{id}` - Deleta empresa

### Filtros e Paginação

- `is_active` (boolean) - Filtra por status ativo/inativo
- `search` (string) - Busca por nome ou email
- `page` (int, padrão: 0) - Número da página
- `per_page` (int, padrão: 15) - Itens por página

Exemplo:
```
GET /api/companies?is_active=true&search=tech&page=0&per_page=10
```

## Estrutura do Projeto

```
src/main/java/br/com/baggiotech/tecos_api/
├── domain/              # Regras de negócio e entidades
├── application/         # Casos de uso (Use Cases)
├── infrastructure/     # Implementações técnicas (JPA, etc)
└── presentation/       # Controllers, DTOs e Exception Handlers
```
