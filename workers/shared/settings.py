from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    rabbitmq_url: str
    minio_endpoint: str
    minio_access_key: str
    minio_secret_key: str
    minio_bucket: str = "serenade"
    backend_url: str
    worker_api_key: str
    whisper_model: str = "base"
    admin_port: int = 8001


settings = Settings()
