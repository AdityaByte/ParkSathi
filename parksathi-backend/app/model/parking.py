"""The user submits the details during the registration
of the parking spot as to be saved in db and been verified by the user."""
from beanie import Link, Document
from pydantic import BaseModel
from enum import Enum

from app.model.user import User

class VerificationStatus(str, Enum):
    PENDING = "pending"
    APPROVED = "apporved"
    REJECTED = "rejected"

class Coordinates(BaseModel):
    lat: float
    lng: float

class ParkingDetails(Document):
    # The details were associated with which user.
    user: Link[User]
    parking_name: str
    address: str
    phone_number: str
    id_proof: str # it just tells the id name which we use in the verification file document.
    coordinates: Coordinates
    verification_file_url: str # file url.
    verification_status: VerificationStatus = VerificationStatus.PENDING

    class Settings:
        name = "parking_details"