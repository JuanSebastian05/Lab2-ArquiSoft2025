# 🔒 Security Improvements - JwtUtil

## Problemas Solucionados

### 🚨 Critical Security Issues Fixed

#### 1. Hardcoded JWT Secret (Security Hotspot - Critical)
**Antes:**
```java
private final String SECRET = "MiSecretoMuyLargoParaJWTQueDebeSerSeguro12345";
```

**Ahora:**
```java
@Value("${jwt.secret}")
private String secret;
```

**Beneficios:**
- ✅ El secreto ya NO está en el código fuente
- ✅ Se puede cambiar por entorno sin recompilar
- ✅ Se puede usar variables de entorno o AWS Secrets Manager en producción
- ✅ Cumple con OWASP y estándares de seguridad

#### 2. Hardcoded Expiration Time
**Antes:**
```java
private final long EXPIRATION = 1000 * 60 * 60 * 24;
```

**Ahora:**
```java
@Value("${jwt.expiration}")
private long expiration;
```

## Configuración

### Desarrollo (.env local)
```bash
JWT_SECRET=MiSecretoMuyLargoParaJWTQueDebeSerSeguro12345ChangeMeInProduction
JWT_EXPIRATION=86400000
```

### Producción
**Opción 1: Variables de Entorno**
```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION=3600000
```

**Opción 2: AWS Secrets Manager**
```yaml
# application-prod.yml
jwt:
  secret: ${AWS_SECRET_MANAGER_JWT_SECRET}
  expiration: 3600000
```

**Opción 3: Kubernetes Secret**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: jwt-config
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
```

## Generar un Secret Seguro

### Usando OpenSSL
```bash
openssl rand -base64 32
```

### Usando Node.js
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

### Usando Python
```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

## Checklist de Seguridad

- [x] Secret NO está hardcodeado en código
- [x] Secret se lee desde configuración externa
- [x] `.env` está en `.gitignore`
- [x] `.env.example` está versionado (sin valores reales)
- [x] Tests usan valores de prueba (no los de producción)
- [ ] En producción, usar AWS Secrets Manager o similar
- [ ] Rotar el secret periódicamente
- [ ] Monitorear intentos de tokens inválidos

## SonarCloud Metrics Expected

| Métrica | Antes | Después |
|---------|-------|---------|
| Security Hotspots | 1-2 | 0 |
| Security Rating | E | A |
| Vulnerabilities | 1 | 0 |

## Referencias

- [OWASP - JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Boot - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [SonarCloud - Security Hotspots](https://docs.sonarcloud.io/digging-deeper/security-hotspots/)
