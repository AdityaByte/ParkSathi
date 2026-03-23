from pydantic import BaseModel

from app.model.parking import VerificationStatus

class Coordinates(BaseModel):
    lat: float
    lng: float

class NearbyParkingSpot(BaseModel):
    uid: str # The parking spot linked with which owner.
    parking_name: str
    address: str
    phone_number: str
    coordinates: Coordinates
    slots: int
    available_slots: int
    verification_status: VerificationStatus
    distance: float