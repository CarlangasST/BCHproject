# Banco de Chile API Service

## Descripción
API RESTful desarrollada con Spring Boot para la gestión de autenticación y registro de clientes. El sistema implementa un robusto mecanismo de seguridad basado en tokens JWT con estados progresivos, garantizando la integridad y seguridad en cada etapa del servicio.

### Características Principales
- Autenticación mediante tokens JWT
- Almacenamiento seguro de datos en formato JSON
- Encriptación de contraseñas
- Control de acceso por etapas
- Limitación de consumo de APIs mediante Spring Security

## Requisitos Técnicos
- Java 17 o superior
- Maven 3.6.3 o superior
- Spring Boot 3.0 o superior

## Estructura del Proyecto
```
src/
├── main/
│   ├── java/cl/bch/technique/test/
│   │   ├── config/        # Configuraciones de la aplicación
│   │   ├── controller/    # Controladores REST
│   │   ├── dto/          # Objetos de transferencia de datos
│   │   ├── exception/    # Excepciones personalizadas
│   │   ├── model/        # Modelos de datos
│   │   ├── service/      # Lógica de negocio
│   │   └── util/         # Utilidades
│   └── resources/
│       ├── application.properties  # Configuración de la aplicación
│       └── users.json             # Datos de usuarios
└── test/
    └── java/cl/bch/technique/test/
```

## Instalación y Ejecución

### Compilación
```bash
./mvnw clean install
```

### Inicio del Servicio
```bash
./mvnw spring-boot:run
```

El servicio se iniciará en el puerto 8090 por defecto.

## Endpoints API

### 1. Autenticación de Usuario
**POST /cliente/login**

Autentica al usuario y genera un token JWT.

#### Request
```json
{
    "rut": "string",
    "password": "string"
}
```

#### Response
```json
{
    "token": "string",
    "mensaje": "Login exitoso",
    "bloqueado": false
}
```

#### Ejemplo de Uso
```bash
curl -X POST "http://localhost:8090/cliente/login" \
     -H "Content-Type: application/json" \
     -d '{"rut": "12345678-9", "password": "password"}'
```

### 2. Consulta de Usuario
**GET /cliente/consulta/{id}**

Obtiene información de un usuario específico y actualiza el token JWT para la siguiente etapa.

#### Headers Requeridos
- `Auth-x`: Token JWT

#### Response
```json
{
    "user": {
        "id": 1,
        "rut": "string",
        "firstName": "string",
        "lastName": "string",
        "dateBirth": "string",
        "mobilePhone": "string",
        "email": "string",
        "address": "string",
        "cityId": 1,
        "sessionActive": true,
        "password": null
    },
    "token": "string"
}
```

#### Ejemplo de Uso
```bash
curl -X GET "http://localhost:8090/cliente/consulta/1" \
     -H "Auth-x: <token-jwt>"
```

### 3. Registro de Usuario
**POST /cliente/guardar**

Registra un nuevo usuario en el sistema. *(Nota: Escritura de registros temporalmente deshabilitada)*

#### Headers Requeridos
- `Auth-x`: Token JWT

#### Request
```json
{
    "id": 1,
    "rut": "string",
    "firstName": "string",
    "lastName": "string",
    "dateBirth": "string",
    "mobilePhone": "string",
    "email": "string",
    "address": "string",
    "cityId": 1,
    "sessionActive": true,
    "password": "string"
}
```

#### Response
204 No Content

#### Ejemplo de Uso
```bash
curl -X POST "http://localhost:8090/cliente/guardar" \
     -H "Content-Type: application/json" \
     -H "Auth-x: <token-jwt>" \
     -d '{
         "id": 1,
         "rut": "12345678-9",
         "firstName": "John",
         "lastName": "Doe",
         "dateBirth": "1990-01-01",
         "mobilePhone": "123456789",
         "email": "john.doe@example.com",
         "address": "123 Main St",
         "cityId": 1,
         "sessionActive": true,
         "password": "password"
     }'
```

## Pruebas
Para ejecutar las pruebas unitarias:
```bash
./mvnw test
```
