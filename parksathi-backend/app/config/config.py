from pydantic_settings import BaseSettings, SettingsConfigDict
import os

class Settings(BaseSettings):
    # This will automatically look for a MONGO_URI variable in your .env file
    mongo_uri: str | None = os.getenv("MONGO_URI")
    firebase_credentials_base64: str | None = os.getenv("FIREBASE_CREDENTIALS_BASE64")

    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()