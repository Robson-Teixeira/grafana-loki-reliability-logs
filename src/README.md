## Esquema da aplicação
![Esquema da aplicação](img/20250822205926.png)

## Instalações
- [Docker Desktop](https://docs.docker.com/desktop/install/windows-install/)
- [WSL](https://learn.microsoft.com/pt-br/windows/wsl/install)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Maven](https://maven.apache.org/install.html)
- [Java](https://www.oracle.com/java/technologies/downloads/)
  - [JDK 11](https://www.oracle.com/br/java/technologies/javase/jdk11-archive-downloads.html)
- IDE ([IntelliJ](https://www.jetbrains.com/pt-br/idea/#), [Eclipse](https://eclipseide.org/), etc.)
- [Grafana Loki](https://grafana.com/oss/loki/)
- [Loki4j Logback](https://loki4j.github.io/loki-logback-appender/)
- [Slack](https://slack.com/)

## Iniciando a aplicação
- Em caso de falha durante a geração da tabela do banco...
    - Exclua a pasta `postgres/data`, deixando apenas a `postgress/db`.
    - Após reiniciado o servidor, execute:
        ```sql
        CREATE TABLE CURSO (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        numero_matricula VARCHAR(10) NOT NULL UNIQUE,
        numero_curso VARCHAR(10) NOT NULL UNIQUE,
        nome_curso VARCHAR(120) NOT NULL,
        categoria_curso VARCHAR(120) NOT NULL,
        pre_requisito VARCHAR(120) NOT NULL,
        nome_professor VARCHAR(120) NOT NULL,
        periodo_curso VARCHAR(30) NOT NULL,
        data_inscricao TIMESTAMP NOT NULL
        );
        
        INSERT INTO CURSO VALUES
        (
        gen_random_uuid(),
        'MAT001',
        'CUR001',
        'Java Avançado',
        'Programação',
        'Java Básico',
        'Ana Silva',
        'Noturno',
        '2024-06-01 19:00:00'
        ),
        (
        gen_random_uuid(),
        'MAT002',
        'CUR002',
        'Spring Boot',
        'Frameworks',
        'Java Avançado',
        'Carlos Souza',
        'Integral',
        '2024-06-02 08:30:00'
        ),
        (
        gen_random_uuid(),
        'MAT003',
        'CUR003',
        'Banco de Dados PostgreSQL',
        'Banco de Dados',
        'SQL Básico',
        'Marina Costa',
        'Matutino',
        '2024-06-03 10:00:00'
        );
        ```
- Em caso de falha durante a renderização do dashboard no Grafana...
  - 1º Adicione o datasource Prometheus (URL: http://prometheus-api-cursos:9090) ao Grafana
  - 2º Atualize o datasource nas variáveis do dashboard
  - 3º Em algumas versões do Grafana há um bug, então...
    - Pode ser necessário editar os paineis, clicar na query e clica em outro local da tela (e assim o resultado será exibido)
    - Recarregar a aplicação e fazer o acesso via navegador

- Adicione o datasource Loki (URL: http://loki-api-cursos:3100) ao Grafana

## Comandos
- `mvn clean package` realiza o empacotamento do projeto, criando o arquivo JAR ou WAR

## LogQL
- Acessar **Grafana** > **Explore** > **Loki**
- `{app="api-cursos", host="7116afcf27e0", level="ERROR"}` consulta os logs de **erro** da aplicação api-cursos no host 7116afcf27e0
- `count_over_time({app="api-cursos", host="7116afcf27e0", level="ERROR"}[5m])` consulta a contagem de logs de **erro** da aplicação api-cursos no host 7116afcf27e0 nos últimos 5 minutos
  - `count_over_time({app="api-cursos", host="7116afcf27e0", level="ERROR"}[5m]) >= 3` retorna verdadeiro se houver 3 ou mais erros nos últimos 5 minutos
  - `count_over_time({app="api-cursos", host="7116afcf27e0", level="ERROR"}[5m]) >= bool 3` retorna 1 se houver 3 ou mais erros nos últimos 5 minutos, e 0 caso contrário
  - Legenda: `{{message}}` define o formato da legenda no gráfico
- `sum without(app, host, class, message, thread) (count_over_time({level="WARN"}[5m]))` consulta a soma de logs de **alerta** da aplicação api-cursos no host 7116afcf27e0 nos últimos 5 minutos, sem considerar os campos app, host, class, message e thread
  - Legenda: `{{message}}` define o formato da legenda no gráfico
- `sum without(level,app,host,class,message,thread) (count_over_time({level="INFO"}[5m])) > sum without(level,app,host,class,message,thread) (count_over_time({level="WARN"}[5m]))` consulta se a soma de logs de **informação** é maior que a soma de logs de **alerta** nos últimos 5 minutos
  - Legenda: `{{message}}` define o formato da legenda no gráfico
- `sum by(host) (count_over_time({app="api-cursos", level="ERROR"}[5m])) / on() sum (count_over_time({app="api-cursos"}[5m]))` calcula a taxa de erro por host nos últimos 5 minutos
- `max by (level) (count_over_time({app="api-cursos",level="ERROR"}[5m])) > ignoring(level) avg(count_over_time({app="api-cursos",level!="ERROR"}[5m]))` compara o máximo de logs de **erro** com a média de logs **não-erro** nos últimos 5 minutos
- `sum by (app, level) (rate({app="api-cursos"}[5m])) / on (app) group_left sum by (app) (rate({app="api-cursos"}[5m]))` calcula a proporção de logs por nível para cada aplicação nos últimos 5 minutos
- Consulta composta
  - `sum(count_over_time({app="api-cursos",class="SqlExceptionHelper",level="ERROR",method="logExceptions"}[5m]))  >= 5` conta o número de logs de erro gerados pela classe _SqlExceptionHelper_ nos últimos 5 minutos
    - Legenda: `ERROR` define a mensagem da legenda no gráfico
    - 🔴
  - `sum(count_over_time({app="api-cursos",class="PoolBase",level="WARN",method="isConnectionAlive"}[5m]))  >= 5` conta o número de logs de alerta gerados pela classe _PoolBase_ nos últimos 5 minutos
    - Legenda: `WARN` define a mensagem da legenda no gráfico
    - 🟠
  - Adicionar > Adicionar para dashboard > Dashboard existente > `Dashboards/api-cursos`
    - Visualização: `Série temporal (Time series)`
        - Opções do painel
            - Título: `LOG EVENTS`
            - Descrição: `Log events 5m`
- Adicionar nova linha ao dashboard
    - Título: `API LOGGING`
- Adicionar o painel `LOG EVENTS` à linha `API LOGGING`
- Adicionar painel/visualização
    - Queries
        - Data source: `Loki`
        - Label filters:
            - `app` = `api-cursos`
    - Visualização: `Logs`
        - Opções do painel
            - Título: `API LOG`
            - Descrição: `Logs da API`

## Alertas - Grafana
- Acessar **Grafana** > **Alerting** > **Alert rules** > **New alert rules**    
  - 1- Geral
    - Nome: `api-cursos-alerts`
  - 2- Consultas
    - Query A
      - Datasource: `Loki`
      - Options > Time Range: `10m to now`
      - Consulta (consulta composta: 1)
      - Tipo: `Faixa` 
    - Query B (dependendo da versão do Grafana, pode ser necessário marcar a opção "_Opções avançadas_")
      - Datasource: `Loki`
      - Options > Time Range: `10m to now`
      - Consulta (consulta composta: 2)
      - Tipo: `Faixa` 
    - Expressão
      - Operação: `Classic condition`
        - Condição A: 
          - Quando: `Contagem`
          - De: `A`
          - Está acima de: `0`
        - Condição B:
          - E: `Contagem`
          - De: `B`
          - Está acima de: `0`
    - 3- Pasta e rótulos
      - Pasta 
        - Nova pasta: `api-cursos-alerts`
      - Rótulos
        - Adicionar rótulos: 
          - app: `api-cursos`
          - level: `ERROR`
          - severity: `critical`
    - 4- Comportamento de avaliação
      - Novo grupo de avaliação: `api-cursos`
      - Configurar nenhum dado e tratamento de erros
        - Estado de alerta se não houver dados ou todos os valores forem nulos: `Normal`
        - Estado de alerta em caso de erro de execução ou tempo limite: `Erro`
    - 5- Notificações
      - Ponto de contato: `grafana-default-email` (editar na sequência o endereço de e-mail do ponto de contato e configurar o SMTP)
    - 6- Configuração mensagem de notificação
      - Descrição: Falha de comunicação com o database
      - Adicionar anotação customizada
        - Nome: Dashboard
        - Conteúdo: Link do dashboard `api-cursos` (compartilhar > encurtar link)

## Slack
- Acessar [Slack](https://slack.com/)
- Criar app (api-cursos-alerts)
- Criar canal (api-cursos-alerts)
- Adicionar app `Incoming WebHooks` ao workspace do app

## Grafana x Slack
- Acessar **Grafana** > **Alerting** > **Contact points**
  - Criar ponto de contato
    - Nome: `api-cursos-slack`
    - Integração: `Slack`
    - Webhook URL: <URL do webhook>
- Acessar **Grafana** > **Alerting** > **Notification policies**
  - Editar _Default policy_
    - Ponto de contato padrão: `api-cursos-slack`
- Acessar **Grafana** > **Alerting** > **Alert rules**
  - Editar _api-cursos-alerts_
    - Ponto de contato: `api-cursos-slack`