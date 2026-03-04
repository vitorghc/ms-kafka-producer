# POC Kafka com Spring Boot (Java 21)

Esta POC demonstra o uso do **Apache Kafka** com **Spring Boot** e **Java 21**, utilizando um **Producer** e um **Consumer (Listener)**.

O objetivo é permitir que qualquer pessoa consiga subir todo o ambiente **apenas executando um `docker-compose up`**, sem necessidade de configurações adicionais na máquina.

---

## 🧱 Arquitetura

* **Java:** 21
* **Spring Boot:** 3.5.7
* **Apache Kafka**
* **Docker & Docker Compose**

### Módulos

* **ms-kafka-producer**
  Responsável por publicar mensagens em um tópico Kafka.

* **ms-kafka-consumer**
  Responsável por consumir mensagens do tópico Kafka através de um `@KafkaListener`.

---

## 🚀 Como subir o projeto

### Pré-requisitos

* Docker
* Docker Compose

> ⚠️ **Não é necessário Java ou Maven instalados localmente**

---

### Subindo o ambiente

A partir da raiz do projeto, execute:

```bash
cd ms-kafka-producer/src/main/resources/docker-compose

docker compose up
```

Esse comando irá subir:

* Zookeeper
* Kafka
* ms-kafka-producer
* ms-kafka-consumer

---

## 🔄 Funcionamento da POC

1. O **Producer** publica mensagens em um tópico Kafka
2. O **Consumer** escuta esse tópico via `@KafkaListener`
3. Ao receber uma mensagem, o consumer realiza o log da informação

---

## 📌 Configuração do Kafka

### Tópico

O tópico utilizado nesta POC é definido via `application.yml`:

```yaml
 topics:
  payment: "topic_test"
```

---

## 🧪 Testando a aplicação

Após subir o ambiente:

* Envie uma requisição HTTP para o **Producer** (caso exista um endpoint REST)
```bash
http://localhost:8081/v1/payments
```
body:
```json
{
    "number": "1",
    "description": "pagamento valido",
    "value": 120.0
}
```

---
## 🛠 Tecnologias utilizadas

* Java 21
* Spring Boot
* Spring Kafka
* Apache Kafka
* Docker
* Docker Compose

---

## 📖 Observações

* Esta é uma **POC**, focada em simplicidade e facilidade de execução
* Não há preocupação com segurança, autenticação ou alta disponibilidade
* Ideal para estudos, testes locais e onboarding de times

