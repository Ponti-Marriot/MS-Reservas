# MS-Reservas

Microservicio de Gestión de Reservas - Sistema Ponti Marriot

## Descripción

Este microservicio gestiona el ciclo de vida completo de las reservas de hotel, incluyendo:
- Búsqueda de disponibilidad
- Creación de reservas (estado PENDING)
- Confirmación de pagos
- Consulta de reservas
- Cancelación de reservas

## Arquitectura

### Tecnologías
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring WebFlux** (para comunicación con otros microservicios)
- **Apache Kafka** (para eventos asíncronos)
- **Maven** (gestor de dependencias)

### Dependencias con otros microservicios
- **MS-ManejadorBD**: Persistencia de datos (Reservations, Payments, AuditLogs)
- **MS-Propiedades**: Validación de disponibilidad de habitaciones
- **MS-Turismo**: Recibe eventos de confirmación/cancelación vía Kafka

## Estructura del Proyecto

```
src/main/java/com/pontimarriot/reservas/
├── api/
│   ├── controller/          # Controladores REST
│   └── dto/                 # DTOs de request/response
├── application/
│   └── service/             # Lógica de negocio
│       └── impl/
├── domain/
│   ├── enums/               # Estados de reservas y pagos
│   ├── event/               # Eventos de Kafka
│   └── model/               # Entidades del dominio
└── infrastructure/
    ├── config/              # Configuración (Kafka, WebClient)
    ├── dbclient/            # Cliente REST para MS-ManejadorBD
    ├── kafka/               # Publicadores de eventos
    └── properties/          # Cliente REST para MS-Propiedades
```

## API Endpoints

### 1. Búsqueda de Disponibilidad
```http
POST /api/v1/availability/search
Content-Type: application/json
X-Correlation-Id: <uuid>

{
  "hotelId": "hotel-1",
  "roomTypeCode": "SUITE",
  "checkIn": "2025-12-01",
  "checkOut": "2025-12-05",
  "numRooms": 2,
  "numAdults": 4
}
```

### 2. Crear Reserva
```http
POST /api/v1/reservations
Content-Type: application/json
Idempotency-Key: <uuid>
X-Correlation-Id: <uuid>

{
  "hotelId": "hotel-1",
  "roomId": "room-101",
  "clientId": "client-123",
  "checkIn": "2025-12-01",
  "checkOut": "2025-12-05",
  "totalPrice": 500000,
  "currency": "COP"
}
```

### 3. Confirmar Reserva
```http
POST /api/v1/reservations/{id}/confirm
Content-Type: application/json
X-Correlation-Id: <uuid>

{
  "transactionId": "BANCOLOMBIA-20251201-1234",
  "approved": true
}
```

### 4. Consultar Reserva
```http
GET /api/v1/reservations/{id}
X-Correlation-Id: <uuid>
```

### 5. Cancelar Reserva
```http
DELETE /api/v1/reservations/{id}
Content-Type: application/json
X-Correlation-Id: <uuid>

{
  "reason": "Cliente canceló",
  "origin": "CLIENTE",
  "refundAmount": 500000
}
```

## Eventos Kafka

El microservicio publica eventos en el topic `reservation-events`:

- **CREATED_PENDING**: Nueva reserva creada, esperando pago
- **CONFIRMED**: Pago aprobado, reserva confirmada
- **REJECTED**: Pago rechazado
- **CANCELLED**: Reserva cancelada

## Configuración

### Variables de Entorno / application.properties

```properties
# Puerto del servicio
server.port=8081

# MS-ManejadorBD
dbclient.base-url=http://localhost:8083

# MS-Propiedades
properties.client.base-url=http://localhost:8082

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
kafka.topics.reservation-events=reservation-events
```

## Cómo Ejecutar

### Prerrequisitos
- Java 17+
- Maven 3.8+
- Kafka (corriendo en localhost:9092)
- MS-ManejadorBD (corriendo en puerto 8083)
- MS-Propiedades (corriendo en puerto 8082)

### Compilar
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

O con el JAR generado:
```bash
java -jar target/ms-reservas-1.0.0.jar
```

### Endpoints de Actuator
- Health: http://localhost:8081/actuator/health
- Metrics: http://localhost:8081/actuator/metrics
- Prometheus: http://localhost:8081/actuator/prometheus

## Patrones Implementados

1. **Idempotencia**: Uso de `Idempotency-Key` para evitar reservas duplicadas
2. **Correlation ID**: Trazabilidad de requests a través de microservicios
3. **Event-Driven**: Publicación de eventos en Kafka para comunicación asíncrona
4. **Auditoría**: Registro de todas las acciones en AuditLog
5. **Client REST**: Comunicación síncrona con otros MS vía WebClient

## Estados de Reserva

- **PENDING**: Reserva creada, esperando confirmación de pago
- **CONFIRMED**: Pago aprobado, reserva activa
- **CHECKED_IN**: Cliente realizó check-in
- **CHECKED_OUT**: Cliente realizó check-out
- **CANCELLED**: Reserva cancelada
- **REJECTED**: Pago rechazado o validación fallida

## Estados de Pago

- **PENDING**: Pago en proceso
- **PAID**: Pago exitoso
- **FAILED**: Pago fallido
- **REFUNDED**: Pago reembolsado

## Autor

Equipo de Arquitectura - Ponti Marriot
