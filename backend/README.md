# RSMad Backend

## Requisitos
- Java 21
- Maven Wrapper (incluido en el repo)

## Configuracion rapida (Windows)
Usa `mvnw21.cmd` para forzar Java 21 automaticamente si esta instalado en:

`C:\Program Files\Java\jdk-21`

Ejemplos:

```bat
.\mvnw21.cmd test
.\mvnw21.cmd -Dtest=HealthControllerTest test
.\mvnw21.cmd spring-boot:run
```

## Alternativa manual
Si no usas `mvnw21.cmd`, configura `JAVA_HOME` a Java 21 antes de ejecutar `mvnw.cmd`.

