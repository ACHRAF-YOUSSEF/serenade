# Product Context

Users should be able to open app and play music without sign-in, then authenticate only for protected actions like playlist mutation, upload, rating, and sync. Mobile should feel offline-capable: cached tracks/playlists remain visible, downloads play without network, local edits queue then sync.

Backend protects expensive paths with rate limiting and async queueing. Uploads must not block HTTP while FFmpeg or subtitle generation runs.

