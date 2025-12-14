# Ledger Service 💰

**Simulador de ledger financeiro** focado em demonstrar engenharia robusta para sistemas distribuídos.

> [!IMPORTANT]
> **Não é**: Sistema de pagamento real, gateway ou produto comercial  
> **É**: Demonstração técnica de padrões de engenharia financeira em produção

## 🎯 O que este projeto demonstra

Este projeto simula problemas **reais** de sistemas financeiros distribuídos:

- ✅ **Duplicidade de eventos** - Mesmo webhook chegando N vezes
- ✅ **Operações fora de ordem** - Eventos chegando em sequência não garantida
- ✅ **Saldo como cálculo derivado** - Balance é `SUM(entries)`, nunca UPDATE direto
- ✅ **Idempotência completa** - `external_reference` como chave de idempotência
- ✅ **Reconciliação automática** - Detecção de divergências entre sistemas

## 🏗️ Princípios de Negócio

1. **Dinheiro não é estado mutável** - Saldo é calculado, nunca editado
2. **Idempotência obrigatória** - Mesma operação N vezes = mesmo resultado
3. **Rastreabilidade total** - Nada some sem deixar rastro
4. **Falha é cenário normal** - Sistema assume retry, atraso, duplicidade
5. **Ordem não garantida** - Decisões baseadas em dados persistidos
6. **Divergência é esperada** - Sistema detecta e explica, não corrige automaticamente

## 🚀 Quick Start

### Com Docker (recomendado)

```bash
# Subir toda a stack (PostgreSQL + App)
docker-compose up --build

# Ou usando Makefile
make up
```

Acesse:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### Desenvolvimento Local

```bash
# Subir apenas PostgreSQL
docker-compose up postgres -d

# Rodar aplicação localmente
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📚 Documentação

- **[DOCKER.md](./DOCKER.md)** - Guia completo de Docker
- **[Implementation Plan](./docs/implementation-plan.md)** - Plano de implementação detalhado
- **Swagger UI** - Documentação interativa da API

## 🛠️ Tech Stack

- **Java 21** - Versão LTS moderna
- **Spring Boot 4.0.0** - Framework web
- **PostgreSQL 16** - Banco de dados relacional
- **Testcontainers** - Testes de integração reais
- **Docker** - Containerização completa
- **Flyway** - Migrations de banco
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI** - Documentação automática

## 🗄️ Modelo de Dados

### Core Entities

- **`accounts`** - Contas (sem coluna `balance`!)
- **`operations`** - Operações com `external_reference` único
- **`entries`** - Entries contábeis (double-entry bookkeeping)
- **`reconciliation_records`** - Histórico de reconciliações

### Por que saldo não é coluna?

```sql
-- ❌ ERRADO (estado mutável)
UPDATE accounts SET balance = balance + 100 WHERE id = ?;

-- ✅ CORRETO (derivado de fatos)
SELECT SUM(amount) FROM entries WHERE account_id = ?;
```

**Razão**: Saldo é consequência de movimentações, não um valor editável.

## 🎯 Endpoints Principais

### Processar Operação
```bash
POST /api/v1/operations
{
  "externalReference": "EXT-001",
  "type": "deposit",
  "targetAccountId": "uuid",
  "amount": 100.00
}
```

### Consultar Saldo
```bash
GET /api/v1/accounts/{id}/balance
```

### Reconciliar
```bash
POST /api/v1/reconciliation
{
  "accountId": "uuid",
  "expectedBalance": 500.00
}
```

## 🧪 Testes

### Testes de Integração
```bash
./mvnw test
```

### Testes com Testcontainers
Os testes sobem PostgreSQL automaticamente usando Testcontainers.

## 💡 Decisões Arquiteturais

### 1. Double-Entry Bookkeeping
Cada operação gera ao menos uma entry. Para transferências, gera duas (débito + crédito).

### 2. Idempotência via External Reference
```java
// Permite retry seguro
operations.external_reference → UNIQUE constraint
```

### 3. Balance como Função, não Estado
```java
// Sempre recalculado em tempo real
BigDecimal balance = entryRepository
    .findByAccountId(accountId)
    .stream()
    .map(Entry::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

## 🔍 Observabilidade

- **Correlation ID** em todos os logs (header `X-Correlation-ID`)
- **Structured Logging** em formato JSON
- **Health Checks** via Actuator
- **Metrics** expostos via `/actuator/metrics`

## 🏛️ Arquitetura

```
ledger-service/
├── api/                    # Controllers, DTOs, validators
├── application/            # Use cases, orchestration
├── domain/                 # Entities, value objects, business rules
└── infrastructure/         # Repositories, persistence, observability
```

**Estilo**: Monolito modular (não microserviços)  
**Razão**: Clareza arquitetural > hype

## 🎤 Elevator Pitch

> "Simulador de ledger financeiro que demonstra como lidar com duplicidade de eventos, operações fora de ordem, e reconciliação de divergências. Usa double-entry bookkeeping, saldo calculado (nunca UPDATE direto), e idempotência completa. Inclui testes com Testcontainers rodando cenários sujos. Java 21, Spring Boot 4, PostgreSQL."

## 📊 Status do Projeto

- [x] Stack configurado (Java 21 + Spring Boot 4 + PostgreSQL)
- [x] Docker environment completo
- [ ] Migrations de banco (Fase 1)
- [ ] Domain layer (Fase 2)
- [ ] Use cases (Fase 3)
- [ ] API REST (Fase 4)
- [ ] Failure simulation (Fase 5)
- [ ] Reconciliation system (Fase 6)
- [ ] Observability completa (Fase 7)

## 🤝 Sobre

Projeto desenvolvido para demonstrar padrões de engenharia financeira robusta.  
Ideal para discussões técnicas sobre sistemas distribuídos, idempotência e reconciliação.

---

**Nota**: Este é um projeto educacional/demonstrativo. Não use em produção sem adaptações de segurança e compliance necessárias.
