# Guía de Pruebas - MS-Reservas

## Prerrequisitos

Antes de ejecutar, necesitas instalar:

### 1. Java Development Kit (JDK) 17 o superior
```powershell
# Verificar si está instalado
java -version

# Si no está instalado, descargar de:
# https://adoptium.net/temurin/releases/
```

### 2. Apache Maven
```powershell
# Verificar si está instalado
mvn -version

# Si no está instalado, descargar de:
# https://maven.apache.org/download.cgi
# Luego agregar al PATH de Windows
```

### 3. (Opcional) Docker para Kafka
```powershell
# Verificar si está instalado
docker --version
```

---

## Opción 1: Probar Compilación (Sin ejecutar)

### Paso 1: Compilar
```powershell
cd MS-Reservas
mvn clean compile
```

Si compila sin errores, el código está correctamente estructurado.

### Paso 2: Empaquetar (crear JAR)
```powershell
mvn clean package -DskipTests
```

Esto creará `target/ms-reservas-1.0.0.jar`

---

## Opción 2: Ejecutar Localmente (Requiere dependencias)

### Paso 1: Iniciar Kafka con Docker
```powershell
# Crear archivo docker-compose.yml en el directorio MS-Reservas
docker-compose up -d
```

### Paso 2: Configurar Mocks de Dependencias
Dado que este MS depende de:
- **MS-ManejadorBD** (puerto 8083)
- **MS-Propiedades** (puerto 8082)

Puedes:
1. Ejecutar esos microservicios si los tienes
2. Usar WireMock o similar para mockearlos
3. Modificar temporalmente el código para usar datos en memoria

### Paso 3: Ejecutar el microservicio
```powershell
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8081`

---

## Opción 3: Pruebas Unitarias (Sin dependencias externas)

### Crear pruebas unitarias básicas
```powershell
mvn test
```

---

## Opción 4: Validación Rápida sin Maven

### Verificar estructura del código con el compilador Java
```powershell
# Navegar al directorio src
cd MS-Reservas/src/main/java

# Compilar un archivo individual para verificar
javac -cp . com/pontimarriot/reservas/domain/model/Reservation.java
```

---

## Pruebas de Endpoints (Cuando el servicio esté corriendo)

### 1. Health Check
```powershell
curl http://localhost:8081/actuator/health
```

### 2. Crear una Reserva (ejemplo con curl)
```powershell
curl -X POST http://localhost:8081/api/v1/reservations `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: test-123" `
  -H "X-Correlation-Id: corr-456" `
  -d '{
    "hotelId": "hotel-1",
    "roomId": "room-101",
    "clientId": "client-123",
    "checkIn": "2025-12-01",
    "checkOut": "2025-12-05",
    "totalPrice": 500000,
    "currency": "COP"
  }'
```

### 3. Con Postman o Insomnia
Importar los siguientes endpoints:
- POST `/api/v1/availability/search`
- POST `/api/v1/reservations`
- POST `/api/v1/reservations/{id}/confirm`
- GET `/api/v1/reservations/{id}`
- DELETE `/api/v1/reservations/{id}`

---

## Verificación de Código sin Ejecutar

### Verificar que todos los archivos tengan package declarations
```powershell
Get-ChildItem -Recurse -Filter *.java | Select-String -Pattern "^package com.pontimarriot"
```

### Verificar que no haya TODOs pendientes
```powershell
Get-ChildItem -Recurse -Filter *.java | Select-String -Pattern "// TODO|// FIXME"
```

---

## Próximos Pasos Recomendados

1. **Instalar Maven**: Es esencial para compilar proyectos Spring Boot
2. **Crear pruebas unitarias**: Para validar la lógica sin dependencias externas
3. **Setup Docker Compose**: Para tener Kafka corriendo localmente
4. **Mockear dependencias**: Crear versiones mock de MS-ManejadorBD y MS-Propiedades

---

## Errores Comunes y Soluciones

### "mvn no se reconoce"
- Instalar Maven y agregarlo al PATH de Windows

### "Port 8081 already in use"
- Cambiar `server.port` en application.properties

### "Connection refused" a Kafka
- Verificar que Kafka esté corriendo en localhost:9092

### "Connection refused" a otros microservicios
- Verificar que MS-ManejadorBD (8083) y MS-Propiedades (8082) estén corriendo
- O modificar los URLs en application.properties para usar mocks
