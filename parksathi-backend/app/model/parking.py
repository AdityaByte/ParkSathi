"""The user submits the details during the registration
of the parking spot as to be saved in db and been verified by the user."""
from decimal import DefaultContext
from beanie import Link, Document
from pydantic import BaseModel, Field
from enum import Enum

from app.model.user import User

class VerificationStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"

class Coordinates(BaseModel):
    type: str = "Point"
    coordinates: list[float] = Field(default_factory=lambda: [0.0, 0.0])

class ParkingDetails(Document):
    # The details were associated with which user.
    user: Link[User]
    uid: str
    parking_name: str
    address: str
    phone_number: str
    id_proof: str # it just tells the id name which we use in the verification file document.
    coordinates: Coordinates
    verification_file_url: str # file url.
    verification_status: VerificationStatus = VerificationStatus.PENDING
    slots: int

    class Settings:
        name = "parking_details"
        # Creating a geospatial index on the coordinates field.
        indexes = [[("coordinates.coordinates", "2dsphere")]]