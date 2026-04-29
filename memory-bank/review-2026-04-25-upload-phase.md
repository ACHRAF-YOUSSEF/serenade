# Code Review — 2026-04-25 Upload Phase

## Fixed This Pass
- Backend and workers had a broken raw-object contract: backend uploaded `raw/{trackId}.{ext}` while workers now validate and read `raw/{trackId}`. Backend upload now stores and publishes the UUID-derived key only.
- Upload status was missing, so mobile could not poll processing state after multipart upload. Added authenticated `GET /api/uploads/{trackId}` scoped to the uploader.
- Backend accepted any uploaded file shape as long as multipart parsing succeeded. Added empty-file rejection and a conservative audio extension/content-type allow-list.
- MinIO upload passed a nullable multipart content type through directly; it now defaults to `application/octet-stream` when clients omit it.
- Mobile M7 had no SAF picker, multipart request, upload progress, or status polling. Added `UploadScreen`, `UploadViewModel`, `UploadRepository`, progress `RequestBody`, Retrofit multipart API, and Home navigation entry.
- Mobile upload mutation now checks auth at ViewModel level before starting the network upload.

## Still Needs Work
- Uploads require network and authentication; offline upload outbox is not implemented because durable file payload queuing needs a separate design.
- Uploaded READY tracks are synced by refreshing the normal track feed, but stream URL expiry is still not tracked from backend responses.
- Workers still need FastAPI `/health`, `/metrics`, and protected `/admin/reprocess` surfaces.
- Full HLS package download remains pending for `.m3u8` manifests.
- Playlist add/remove/reorder UI and outbox are still pending.
- Backend B9-B10 rate limiting, security headers, request IDs, and metrics remain pending.
