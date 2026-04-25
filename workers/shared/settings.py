from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    rabbitmq_url: str
    minio_endpoint: str
    minio_access_key: str
    minio_secret_key: str
    minio_bucket: str = "serenade"
    backend_url: str
    # Worker API key — env var only, never logged
    internal_api_key: str
    whisper_model: str = "base"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


settings = Settings()
