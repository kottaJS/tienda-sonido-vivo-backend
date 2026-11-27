# Tienda Sonido Vivo – Backend API REST (EP3 - DSY1104)

Backend desarrollado en **Java 21 + Spring Boot 3.5.7 + Spring Security (JWT) + Spring Data JPA + MySQL + Swagger/OpenAPI** para la tienda de instrumentos musicales **Sonido Vivo** (Viña del Mar).

- **Repositorio Frontend React:** https://github.com/kottaJS/tienda-sonido-vivo
- **Grupo:** 02 (Forma B) · **Sección:** DSY1104-006D
- **Integrantes:** Maximiliano Dinamarca, Patricio Muñoz

## Requisitos
- Java JDK 21+
- Maven 3.9+
- MySQL 8 / MariaDB (puerto `3306`, o `docker compose up -d`)

## Ejecución
1. Revisa `.env.example` para configurar puerto (`8081`), credenciales MySQL (`sonido_vivo_db`), secreto JWT y orígenes CORS (`http://localhost:8080`).
2. Levanta MySQL y ejecuta:
```bash
mvn spring-boot:run
```
3. Documentación Swagger/OpenAPI disponible en:
   - `http://localhost:8081/swagger-ui.html`
   - `http://localhost:8081/v3/api-docs`

## Cuentas Demo Sembradas
| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| Administrador | `maximiliano.dueno@sonidovivo.cl` | `AdminDemo2026!` |
| Vendedor | `patricio.ventas@sonidovivo.cl` | `VendedorDemo2026!` |
| Cliente | `cliente@gmail.com` | `ClienteDemo2026!` |

## Pruebas de Integración
```bash
mvn test
```
Incluye 5 pruebas `@SpringBootTest` + `MockMvc` sobre H2 en modo MySQL (`SonidoVivoBackendTests.java`).
