from beanie import Document, Indexed
from enum import Enum

class UserType(str, Enum):
    USER = "user"
    OWNER = "owner"

class User(Document):
    uid: Indexed(str, unique=True)
    name: str
    email: str
    roles: list[UserType] = [UserType.USER]

    class Settings:
        name = "users"