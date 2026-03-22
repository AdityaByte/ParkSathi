from pydantic import BaseModel, Field, ConfigDict
from beanie import PydanticObjectId

from app.model.parking import VerificationStatus

class CoordinatesSchema(BaseModel):
    lat: float
    lng: float

class ParkingCreate(BaseModel):
    parking_name: str
    address: str
    phone_number: str
    id_proof: str
    coordinates: CoordinatesSchema

class ParkingResponse(BaseModel):
    id: PydanticObjectId = Field(alias="_id")
    parking_name: str
    verification_file_url: str
    verification_status: VerificationStatus

    model_config = ConfigDict(
        from_attributes=True,
        populate_by_name=True
    )