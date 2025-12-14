# Database Schema - Fase 1

Modelo de dados que suporta os **princípios de negócio** do ledger service.

## 🎯 Decisões Arquiteturais Principais

### 1. ❌ **SEM coluna `balance` na tabela `accounts`**

**Por quê?**  
Saldo é **consequência** de movimentações, não um valor editável.

```sql
-- ❌ ERRADO (estado mutável)
UPDATE accounts SET balance = balance + 100 WHERE id = ?;

-- ✅ CORRETO (derivado de fatos imutáveis)
SELECT SUM(amount) FROM entries WHERE account_id = ?;
```

**Benefícios:**
- ✅ Auditoria completa (entries são imutáveis)
- ✅ Impossível ter saldo inconsistente
- ✅ Recalculável a qualquer momento
- ✅ Permite "time-travel" (saldo em data específica)

---

### 2. ✅ **`external_reference` UNIQUE na tabela `operations`**

**Por quê?**  
Garante idempotência - mesma operação pode chegar N vezes, mas é processada apenas uma vez.

```sql
CREATE UNIQUE INDEX idx_operations_external_reference 
ON operations(external_reference);
```

**Fluxo:**
1. Webhook chega com `external_reference = "PSP-123"`
2. Sistema verifica: `SELECT * FROM operations WHERE external_reference = 'PSP-123'`
3. Se existe → retorna `200 OK` (não é erro!)
4. Se não existe → processa e persiste

**Benefícios:**
- ✅ Retry seguro (rede instável)
- ✅ Duplicidade não gera efeito colateral
- ✅ Simplicidade (constraint do banco faz o trabalho)

---

### 3. ✅ **`amount != 0` na tabela `entries`**

**Por quê?**  
Entry com valor zero não faz sentido contábil.

```sql
CHECK (amount != 0)
```

**Benefícios:**
- ✅ Validação no nível do banco (camada extra de segurança)
- ✅ Previne bugs silenciosos

---

### 4. ✅ **Double-Entry Bookkeeping na tabela `entries`**

**Por quê?**  
Princípio contábil de 500+ anos - funciona.

**Estrutura:**
- Cada operação gera **ao menos uma entry**
- Transferências geram **duas entries** (débito + crédito)
- Entries são **imutáveis** (NEVER UPDATE/DELETE)

**Exemplo - Transferência de R$ 100:**
```sql
-- Entry 1: Débito da conta origem
INSERT INTO entries (account_id, amount, direction, entry_type)
VALUES ('conta-A', -100, 'debit', 'transfer_out');

-- Entry 2: Crédito da conta destino
INSERT INTO entries (account_id, amount, direction, entry_type)
VALUES ('conta-B', +100, 'credit', 'transfer_in');
```

**Benefícios:**
- ✅ Auditoria completa (quem enviou, quem recebeu)
- ✅ "Conservation law" - soma de todas entries = 0
- ✅ Detecta vazamentos/criação de dinheiro

---

## 📊 Diagrama ER

```
┌─────────────┐          ┌───────────────┐          ┌──────────────┐
│  accounts   │          │  operations   │          │   entries    │
├─────────────┤          ├───────────────┤          ├──────────────┤
│ id (PK)     │◄─────────│ id (PK)       │──────────│ id (PK)      │
│ type        │          │ ext_reference │          │ operation_id │
│ created_at  │          │ status        │          │ account_id   │
│             │          │ operation_type│          │ amount       │
│             │          │ created_at    │          │ direction    │
│ ❌ NO balance│          │ processed_at  │          │ entry_type   │
└─────────────┘          └───────────────┘          │ source       │
                                                      │ created_at   │
                                                      └──────────────┘
                                │
                                │
                                ▼
                         ┌─────────────────────────┐
                         │ reconciliation_records  │
                         ├─────────────────────────┤
                         │ id (PK)                 │
                         │ account_id (FK)         │
                         │ reconciliation_date     │
                         │ expected_balance        │
                         │ calculated_balance      │
                         │ difference              │
                         │ status                  │
                         │ created_at              │
                         └─────────────────────────┘
```

---

## 📋 Tabelas Detalhadas

### 1. `accounts`

Armazena contas **sem saldo**. Tipos de conta:

| Tipo | Descrição |
|------|-----------|
| `user` | Contas de usuários finais |
| `system` | Contas internas do sistema |
| `transit` | Contas temporárias para operações |

**Constraints:**
- `type` deve ser um dos valores: `user`, `system`, `transit`
- `created_at` default `CURRENT_TIMESTAMP`

---

### 2. `operations`

Operações financeiras com **garantia de idempotência**.

**Tipos de operação:**
- `deposit` - Entrada de dinheiro
- `withdrawal` - Saída de dinheiro
- `transfer` - Transferência entre contas

**Status:**
- `processing` - Em processamento
- `processed` - Completada com sucesso
- `ignored` - Duplicata detectada
- `failed` - Erro no processamento

**Constraints:**
- `external_reference` **UNIQUE** (idempotência!)
- `operation_type` deve ser um dos valores acima
- `status` deve ser um dos valores acima

---

### 3. `entries`

Entries contábeis - **fonte da verdade** para saldos.

**Campos importantes:**
- `amount`: pode ser `+` ou `-`, mas **nunca zero**
- `direction`: `credit` (entrada) ou `debit` (saída)
- `entry_type`: contexto de negócio (ex: `initial_deposit`, `transfer_out`)
- `source`: origem do evento (ex: `bank_api`, `psp_webhook`, `internal`)

**Constraints:**
- `amount != 0` (CHECK constraint)
- `direction` deve ser `credit` ou `debit`
- Foreign keys com `ON DELETE RESTRICT` (entries são imutáveis!)

---

### 4. `reconciliation_records`

Histórico de reconciliações - **detecta divergências**.

**Campos:**
- `expected_balance`: o que o PSP/banco diz que deveria ter
- `calculated_balance`: `SUM(entries)` do nosso sistema
- `difference`: `expected - calculated`
- `status`: `match` (ok) ou `mismatch` (divergência!)

**Importante:**
Sistema **NÃO corrige automaticamente**. Apenas detecta e registra.

---

## 🔍 Índices Estratégicos

### Performance Crítica

```sql
-- Cálculo de saldo (query mais frequente)
CREATE INDEX idx_entries_account_created 
ON entries(account_id, created_at);

-- Verificação de idempotência (toda operação verifica)
CREATE UNIQUE INDEX idx_operations_external_reference 
ON operations(external_reference);
```

### Queries Comuns

```sql
-- Saldo atual de uma conta
SELECT SUM(amount) FROM entries WHERE account_id = ?;

-- Verificar se operação já foi processada
SELECT * FROM operations WHERE external_reference = ?;

-- Últimas reconciliações de uma conta
SELECT * FROM reconciliation_records 
WHERE account_id = ? 
ORDER BY created_at DESC;
```

---

## 🎯 Princípios Validados

✅ **Dinheiro não é estado mutável** - `accounts` sem coluna `balance`  
✅ **Idempotência obrigatória** - `external_reference UNIQUE`  
✅ **Rastreabilidade total** - `entries` imutáveis (ON DELETE RESTRICT)  
✅ **Falha é cenário normal** - `operations.status` suporta `failed`, `ignored`  
✅ **Ordem não garantida** - `entries` com `created_at` para ordenação  
✅ **Divergência é esperada** - `reconciliation_records` detecta, não corrige  

---

## 🚀 Como Rodar Migrations

### Com Docker (recomendado)

```bash
make up
```

Flyway roda automaticamente ao subir a aplicação.

### Localmente

```bash
./mvnw flyway:migrate
```

### Validar Schema

```bash
./mvnw flyway:validate
```

### Ver Histórico

```bash
./mvnw flyway:info
```

---

## 📝 Versionamento

Migrations seguem padrão Flyway:

```
V1__create_accounts_table.sql
V2__create_operations_table.sql
V3__create_entries_table.sql
V4__create_reconciliation_records_table.sql
```

**Regras:**
- ✅ Migrations são **imutáveis** (nunca edite após aplicada)
- ✅ Nova mudança = nova migration (V5, V6, etc)
- ✅ Flyway controla o que já foi aplicado

---

## 🔐 Segurança

### Foreign Keys com RESTRICT

```sql
operation_id UUID NOT NULL REFERENCES operations(id) ON DELETE RESTRICT
```

**Por quê?**  
Impede deleção acidental de operações que têm entries.  
Entries são **auditoria** - nunca podem ser órfãs.

### CHECK Constraints

Validações no nível do banco = camada extra de segurança.

---

## 🎉 Próximos Passos

- [ ] Fase 2: Criar entities Java (domain layer)
- [ ] Fase 2: Criar value objects (`Money`, `ExternalReference`)
- [ ] Fase 2: Implementar `BalanceCalculator`

---

**Criado**: Fase 1 - Foundation & Data Model  
**Migrations**: Flyway  
**Database**: PostgreSQL 16
