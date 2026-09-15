
```markdown
# 📦 Fleet & Logistics Event Tracking Engine

Ein modernes, Cloud-fähiges Backend-System zur Echtzeit-Sendungsverfolgung und asynchronen Event-Verarbeitung für Logistik- und E-Commerce-Netzwerke.
[![CI Pipeline](https://github.com/A-Ras/logistics-tracking-service/actions/workflows/ci.yml/badge.svg)](https://github.com/A-Ras/logistics-tracking-service/actions)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange?logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)

---

## 🏛️ Architektur & Event-Fluss

Das System setzt auf eine entkoppelte, **Event-Driven Architecture (EDA)**. Wenn ein Paket-Scanner ein Event meldet, wird der HTTP-Aufruf nicht blockiert. Die Benachrichtigung erfolgt vollständig asynchron.

```text
+---------------------+
|  Scanner / Client   |
+----------+----------+
           | POST /api/v1/shipments/{trk}/events
           v
+--------------------------+
|    TrackingController    |  <--- RFC 7807 Error Handling (ProblemDetail)
+----------+---------------+
           |
           v
+--------------------------+      Idempotenz-Prüfung & Speichern
|     TrackingService      | ==================================> [( PostgreSQL 16 )]
+----------+---------------+                                       (Managed via Flyway)
           |
           | Publisht Domain-Event (JSON)
           v
+--------------------------------------+
|  RabbitMQ: shipment.topic.exchange   |
+--------------------+-----------------+
                     | RoutingKey: shipment.status.*
                     v
+--------------------------------------+
|     shipment.notification.queue      |
+--------------------+-----------------+
                     | Asynchron konsumiert
                     v
+--------------------------------------+
|     ShipmentNotificationListener     |  ---> (Trigger für E-Mail / Push)
+--------------------------------------+
```

---

## 💡 Zentrale Architekturentscheidungen (ADRs)

### 1. Idempotente REST-APIs (`idempotency_key`)
* **Problem:** In Logistikzentren führen instabile Netzwerkverbindungen oft zu doppelten Scanner-Übertragungen.
* **Lösung:** Jedes Scan-Event besitzt einen eindeutigen `idempotencyKey`. Erkennt das System ein bereits verarbeitetes Event, liefert es ohne erneutes Schreiben den aktuellen Zustand (`200 OK`) zurück, anstatt Duplikate in der Historie oder doppelte Benachrichtigungen zu erzeugen.

### 2. Schema-Versionierung mit Flyway (`ddl-auto: validate`)
* **Entscheidung:** Verzicht auf automatische Tabellengenerierung durch Hibernate.
* **Grund:** Produktionstauglichkeit. Datenbankänderungen werden strikt über SQL-Migrationsskripte (`db/migration/V1__init_schema.sql`) versioniert. Hibernate validiert beim Start lediglich die Konsistenz.

### 3. Integrationstests mit Testcontainers & `@ServiceConnection`
* **Entscheidung:** Verzicht auf In-Memory-Datenbanken wie H2.
* **Grund:** H2 verhält sich bei Transaktionen, Indizes und SQL-Dialekten anders als PostgreSQL. Testcontainers startet für Integrationstests echte PostgreSQL-Container in Docker. Dank Spring Boots `@ServiceConnection` entfällt fehleranfälliges manuelles Konfigurieren.

### 4. Entkoppelte Benachrichtigungen via RabbitMQ
* **Entscheidung:** Asynchrones Messaging statt synchroner Service-Aufrufe.
* **Grund:** Das Verarbeiten von Status-Updates darf nicht durch langsame Benachrichtigungsdienste blockiert werden. Ein `TopicExchange` ermöglicht es weiteren Microservices, sich ohne Code-Änderungen an den Event-Stream anzuhängen.

### 5. Standardisierte Fehler nach RFC 7807 (`ProblemDetail`)
* Alle API-Fehler (z. B. 404 Not Found, 400 Validation Error) folgen dem IETF-Standard RFC 7807, um ein einheitliches Fehlerformat für Frontend- und Drittsysteme bereitzustellen.

---

## 🚀 Lokales Setup & Start

### Voraussetzungen
* Docker & Docker Compose
* Java 21 JDK

### 1. Infrastruktur starten (PostgreSQL & RabbitMQ)
```bash
docker compose up -d
```
* **PostgreSQL:** Port `5433`
* **RabbitMQ UI:** `http://localhost:15672` (User: `guest` / Passwort: `guest`)

### 2. Anwendung starten
```bash
./mvnw spring-boot:run
```

### 3. Tests ausführen (startet Testcontainers)
```bash
./mvnw clean verify
```

---

## 🧪 API-Endpunkte testen

Im Projekt liegt eine fertige **`requests.http`**-Datei für IntelliJ / VS Code.

| Methode | Endpunkt | Beschreibung |
| :--- | :--- | :--- |
| `POST` | `/api/v1/shipments` | Neue Sendung anlegen (`201 Created`) |
| `GET` | `/api/v1/shipments/{trackingNumber}` | Sendungsdetails & Historie abrufen |
| `POST` | `/api/v1/shipments/{trackingNumber}/events` | Scan-Event registrieren & RabbitMQ-Event feuern |

---

## 🐳 Docker Multi-Stage Build

Das beiliegende `Dockerfile` nutzt ein optimiertes Multi-Stage-Verfahren:
1. **Build Stage:** `eclipse-temurin:21-jdk-alpine` zum Kompilieren.
2. **Runtime Stage:** Schlankes JRE-Alpine Image (Non-Root-User `appuser` für maximale Sicherheit).

```bash
docker build -t tracking-service:latest .
docker run -p 8080:8080 tracking-service:latest
```



