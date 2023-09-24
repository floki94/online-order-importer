# Online Order Importer

Este proyecto está diseñado para importar pedidos desde una API REST a una base de datos y generar un archivo CSV. Está construido con Spring Boot.

## Requisitos

- JDK 11 o superior
- Maven

## Instrucciones para ejecutar el proyecto

1. Clona el repositorio:

```
git clone https://github.com/floki94/online-order-importer.git
cd online-order-importer
```

## Compila y empaqueta el proyecto:

```
mvn clean install
```

## Ejecuta la aplicación:

```
java -jar target/online-order-importer-0.0.1-SNAPSHOT.jar
```

Una vez esté la aplicación ejecutándose ya se puede arrancar el proyecto front-importer para hacer uso de los distintos endpoints y visualizar los datos.
