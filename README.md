# ⚙️ RN Planner API - Back-End

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Azure](https://img.shields.io/badge/azure-%230072C6.svg?style=for-the-badge&logo=microsoftazure&logoColor=white)

> **API RESTful desenvolvida para sustentar o ecossistema do RN Planner, um "Hub de Execução" corporativo.**

## 📖 Sobre o Projeto

O **RN Planner API** é o serviço de back-end responsável por processar e gerenciar todas as regras de negócio do aplicativo RN Planner. O sistema foi idealizado para digitalizar e otimizar a rotina de Representantes de Negócios (vendas externas/B2B), substituindo anotações manuais e planilhas por um ecossistema centralizado e em nuvem.

Este back-end gerencia a lógica de pontuação para o atingimento da remuneração variável (meta de 100.000 pontos), processando de forma inteligente as **Tasks, Ofertas, Missões, Compradores e Pendências (Acordos)** de cada Ponto de Venda (PDV).

---

## 🚀 Tecnologias e Arquitetura

O projeto foi construído utilizando os padrões mais modernos da indústria para garantir escalabilidade, segurança e fácil manutenção:

* **Linguagem:** Java (POO, Expressões Lambda)
* **Framework Principal:** Spring Boot
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Banco de Dados:** PostgreSQL (Relacional)
* **Arquitetura:** RESTful API (Controllers, Services, Repositories, DTOs)
* **Documentação:** Swagger / OpenAPI
* **Cloud & Deploy:** Microsoft Azure (Azure App Service)

---

## ⚡ Funcionalidades Principais (Business Logic)

* **Gestão de PDVs:** Cadastro, listagem e detalhamento de Pontos de Venda.
* **Sistema de Visitas:** Registro de visitas diárias, com rastreamento de status e ações realizadas no cliente.
* **Cálculo de Metas:** Processamento das regras de negócio atreladas a Tasks, Ofertas de Pontos e Missões, retornando o progresso para o atingimento da remuneração variável.
* **Hub de Pendências:** Gerenciamento de acordos e problemas por PDV (ex: solicitações de material, impasses financeiros), controlando o estado de cada item (`PENDENTE` ou `RESOLVIDO`).
* **Tratamento Global de Exceções:** Respostas padronizadas de erro (HTTP 400, 404, 500) para garantir a resiliência da aplicação cliente (Front-End).

---

## 🛠️ Como executar o projeto localmente

### Pré-requisitos
* Java 17 ou superior
* Maven
* PostgreSQL rodando localmente (ou em container Docker)
* pgAdmin (Opcional, para visualização do banco)

### 🚀 Passos para rodar localmente:

1. Clone este repositório:
```bash
git clone https://github.com/CarlosssEduardo/rnplanner-backend.git 