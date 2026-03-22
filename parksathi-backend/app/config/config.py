from pydantic_settings import BaseSettings, SettingsConfigDict
import os

class Settings(BaseSettings):
    # This will automatically look for a MONGO_URI variable in your .env file
    mongo_uri: str = os.getenv("MONGO_URI")

    model_config = SettingsConfigDict(env_file=".env")

settings = Settings()