# Serenade — Architecture & Workflows

## Vue d'ensemble

Serenade est une plateforme de streaming musical full-stack composée de trois couches : une application Android, un backend Spring Boot, et un pipeline de workers Python asynchrones. L'infrastructure locale repose sur Docker Compose (PostgreSQL 18, Redis 7, RabbitMQ 3, MinIO, MailHog).

---

## Technologies

| Couche | Stack |
|--------|-------|
| Android | Kotlin, Jetpack Compose, Material 3, Hilt, Room, Retrofit, Media3/ExoPlayer, WorkManager |
| Backend | Spring Boot 4, Java 25, Maven, Spring Security, JWT, JPA, Flyway |
| Base de données | PostgreSQL 18 (données), Redis 7 (cache) |
| Messaging | RabbitMQ 3 (aio-pika côté workers) |
| Stockage objet | MinIO (compatible S3) |
| Worker Transcoder | Python 3.12, FastAPI, FFmpeg, aio-pika, Pydantic |
| Worker Subtitler | Python 3.12, FastAPI, faster-whisper, httpx, aio-pika, Pydantic |

---

## Workflow 1 — Authentification

1. L'utilisateur s'inscrit → le backend génère un code OTP à 5 chiffres (hash SHA-256) et l'envoie par email via MailHog.
2. L'utilisateur saisit le code → le backend vérifie le hash → émet une paire JWT (access + refresh).
3. L'Android stocke les tokens dans Room. Les actions protégées (playlist, upload, notation) requièrent ce JWT.
4. Le refresh token permet de renouveler la session sans ressaisir les identifiants.

---

## Workflow 2 — Upload & Pipeline de traitement

```
[Android]
  1. Upload multipart → POST /api/tracks/upload

[Backend]
  2. Stocke le fichier brut dans MinIO  →  raw/{trackId}
  3. Crée un Track en statut PROCESSING dans PostgreSQL
  4. Publie un message sur la queue RabbitMQ "transcoder"
  5. Publie un message sur la queue RabbitMQ "subtitler"
  6. Répond 202 immédiatement (non-bloquant)

[Worker Transcoder]
  7. Consomme le message RabbitMQ { trackId, objectKey }
  8. Télécharge raw/{trackId} depuis MinIO
  9. FFmpeg génère un manifeste HLS VOD audio-only + segments .ts
  10. Upload des segments dans MinIO → hls/{trackId}/
  11. Callback POST /internal/tracks/{id}/hls (X-Api-Key) → Track passe READY

[Worker Subtitler]
  12. Consomme le message RabbitMQ { trackId, objectKey }
  13. Télécharge raw/{trackId} depuis MinIO
  14. faster-whisper transcrit l'audio → segments (start_ms, end_ms, text)
  15. Sérialise en WebVTT
  16. Callback POST /internal/tracks/{id}/subtitles (X-Api-Key)

[Android]
  17. Refresh → Track READY visible avec URL HLS présignée (≤ 15 min)
  18. Media3 / ExoPlayer stream le HLS depuis MinIO
```

---

## Workflow 3 — Lecture en streaming

1. Android demande l'URL HLS au backend → reçoit une URL présignée MinIO (TTL ≤ 15 min).
2. Media3/ExoPlayer charge le manifeste `.m3u8` puis les segments `.ts` en streaming adaptatif.
3. La lecture continue en arrière-plan via `MediaSessionService` (service foreground, wake lock réseau, gestion du focus audio).
4. En cas de débranchement des écouteurs, la lecture se met en pause automatiquement.

---

## Workflow 4 — Mode hors-ligne & synchronisation

1. Toute mutation locale (créer une playlist, noter une piste) écrit d'abord dans Room (SQLite) et insère une `PendingOpEntity` dans l'outbox.
2. Un `SyncRepository` vide l'outbox en arrière-plan dès que le réseau est disponible.
3. Les pistes téléchargées sont lues directement depuis le stockage local sans requête réseau.
4. Le cache Room reste visible même sans connexion (données affichées en mode dégradé).

---

## Rôle de chaque brique d'infrastructure

### PostgreSQL 18
Stockage relationnel principal : utilisateurs, codes d'authentification (hash uniquement), pistes (statut PROCESSING/READY, clé HLS MinIO), playlists, tables de jointure, notations. Clés primaires en UUID, migrations versionnées avec Flyway.

### Redis 7
Cache réactif : tokens de rafraîchissement, rate limiting sur les endpoints coûteux (upload, recherche). Accès via Spring Data Redis Reactive.

### RabbitMQ 3
Bus de messages asynchrone qui découple l'API du pipeline de traitement. Deux queues : `transcoder` et `subtitler`. Le backend publie après chaque upload ; les workers consomment de manière indépendante, stateless et idempotente. Payload : `{ "trackId": "uuid", "objectKey": "raw/uuid" }`.

### MailHog
Serveur SMTP de développement (image `mailhog/mailhog`). Capte tous les emails sortants (vérification de compte, réinitialisation de mot de passe) sans les envoyer réellement. Interface web sur le port 8025.

### MinIO
Stockage objet compatible S3 auto-hébergé. Contient les fichiers audio bruts (`raw/`) et les sorties HLS (`hls/`). Les clients Android n'accèdent jamais directement à MinIO : le backend sert des URLs présignées à courte durée de vie.

---

## Sécurité

- Les endpoints `/internal/**` (callbacks workers) utilisent `X-Api-Key`, jamais JWT.
- L'algorithme JWT `alg: none` est rejeté inconditionnellement.
- Aucun secret dans le code source — variables d'environnement uniquement.
- Les codes de vérification/reset sont stockés en hash SHA-256, jamais en clair.
- Les subprocessus FFmpeg utilisent une liste d'arguments explicite (`shell=False`).
- En release Android : `allowBackup="false"`, `debuggable="false"`.
