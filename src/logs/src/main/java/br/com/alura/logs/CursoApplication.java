package br.com.alura.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
@EnableJpaRepositories
public class CursoApplication {
	
	private static Logger logger = LoggerFactory.getLogger(CursoApplication.class);

	public static void main(String[] args) {
		logger.info("Iniciando a API de Cursos da Alura");
		SpringApplication.run(CursoApplication.class, args);
		logger.info("API para cadastro para cursos da plataforma Alura.");
	}
	
	
}
