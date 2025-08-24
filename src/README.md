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
  - Legend: `{{message}}` define o formato da legenda no gráfico
- `sum without(app, host, class, message, thread) (count_over_time({level="WARN"}[5m]))` consulta a soma de logs de **alerta** da aplicação api-cursos no host 7116afcf27e0 nos últimos 5 minutos, sem considerar os campos app, host, class, message e thread
  - Legend: `{{message}}` define o formato da legenda no gráfico
- `sum without(level,app,host,class,message,thread) (count_over_time({level="INFO"}[5m])) > sum without(level,app,host,class,message,thread) (count_over_time({level="WARN"}[5m]))` consulta se a soma de logs de **informação** é maior que a soma de logs de **alerta** nos últimos 5 minutos
  - Legend: `{{message}}` define o formato da legenda no gráfico
- `sum by(host) (count_over_time({app="api-cursos", level="ERROR"}[5m])) / on() sum (count_over_time({app="api-cursos"}[5m]))` calcula a taxa de erro por host nos últimos 5 minutos
- `max by (level) (count_over_time({app="api-cursos",level="ERROR"}[5m])) > ignoring(level) avg(count_over_time({app="api-cursos",level!="ERROR"}[5m]))` compara o máximo de logs de **erro** com a média de logs **não-erro** nos últimos 5 minutos
- `sum by (app, level) (rate({app="api-cursos"}[5m])) / on (app) group_left sum by (app) (rate({app="api-cursos"}[5m]))` calcula a proporção de logs por nível para cada aplicação nos últimos 5 minutos