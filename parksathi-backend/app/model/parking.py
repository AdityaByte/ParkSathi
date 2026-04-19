"""The user submits the details during the registration
of the parking spot as to be saved in db and been verified by the user."""
from decimal import DefaultContext
from beanie import Link, Document
from uuid import UUID, uuid4
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
    parking_id: UUID = Field(default_factory=uuid4)
    parking_name: str
    address: str
    phone_number: str
    id_proof: str # it just tells the id name which we use in the verification file document.
    coordinates: Coordinates
    verification_file_url: str # file url.
    verification_status: VerificationStatus = VerificationStatus.PENDING
    # We are not maintaing a seperate relation for the slots although we can maintain just for testing we are doing this 
    # in the next phase we will create a seperate collection of this and map it with the UID.
    slots: int
    booked_slots: int = 0
    acquired_slots: int = 0
    total_income: float = 0.0
    hourly_rate: float = 0.0 # The owner will decide like how much he has to pay for minutes or for hours likewise we are currently concluding at 1 hour.

    class Settings:
        name = "parking_details"
        # Creating a geospatial index on the coordinates field.
        indexes = [[("coordinates.coordinates", "2dsphere")]]