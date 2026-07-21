# FlashSaleX Guide

## Commands

- Spring boot run : mvn spring-boot:run
- Connecting to PostgreSQL - docker exec -it flash-sale-postgres psql -U postgres
  - Commonly used:
    - List DBs : `\l`
    - Connect to DB : `\c flash_sale_db`
    - List Tables: `\dt`
    - List Indexes: `\di`
    - Describe the Table : `\d <table_name>`
    - Delete 

## URLs

- Check the Endpoints Health - http://localhost:8080/actuator - ( Add HTTP manually in case error)
  Expect:

```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/actuator", "templated": false },
    "health": {
      "href": "http://localhost:8080/actuator/health",
      "templated": false
    },
    "health-path": {
      "href": "http://localhost:8080/actuator/health/{*path}",
      "templated": true
    },
    "info": {
      "href": "http://localhost:8080/actuator/info",
      "templated": false
    },
    "prometheus": {
      "href": "http://localhost:8080/actuator/prometheus",
      "templated": false
    }
  }
}
```

- Check the Info about Endpoints - http://localhost:8080/actuator/info 