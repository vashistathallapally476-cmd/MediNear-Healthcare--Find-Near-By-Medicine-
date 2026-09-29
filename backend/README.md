# MediNear Backend

## Quick Start
```bash
# 1. Ensure PostgreSQL is running with database "MedNear"
# 2. Set API keys (optional — app works without them, just no AI/Maps features)
export GOOGLE_MAPS_API_KEY=your_key_here
export ANTHROPIC_API_KEY=your_key_here

# 3. Build and run
cd MediNear-backend
mvn clean package -DskipTests
java -jar target/mednear-backend-4.0.0.jar
```

## Fixes Applied
| File | Fix |
|---|---|
| `pom.xml` | Added `-parameters` compiler flag so Spring can read method param names |
| `application.properties` | `spring.cache.type=simple` — no Redis required |
| `MedicineService.java` | `@Cacheable` uses `#root.args[N]` instead of named params |
| `RedisConfig.java` | `@ConditionalOnProperty` so it only loads when Redis is enabled |

## Database
- Default: `postgres` / `akhil1101` on `localhost:5432/MedNear`
- Run `DB_SCHEMA.sql` once to create tables and seed data

## API Endpoints
```
POST   /api/auth/login
POST   /api/auth/register
GET    /api/medicines/nearby?medicine=&latitude=&longitude=&radius=&page=&size=
GET    /api/medicines/autocomplete?q=
POST   /api/stores            (JWT)
GET    /api/stores/my         (JWT)
DELETE /api/stores/{id}       (JWT)
GET    /api/inventory/store/{storeId}  (JWT)
PUT    /api/inventory                  (JWT)
DELETE /api/inventory/{inventoryId}    (JWT)
POST   /api/ai/chat
GET    /api/maps/route
GET    /api/maps/static-url
WS     /ws  →  /topic/inventory/{storeId}
```
