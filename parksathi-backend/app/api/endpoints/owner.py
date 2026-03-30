"""There were specific routes for dealing with owner specific tasks."""
import shutil
import uuid
import os
from pathlib import Path
from fastapi import UploadFile

from app.model.parking import ParkingDetails, Coordinates, VerificationStatus
from app.model.user import User
from app.schema.parking import ParkingResponse

UPLOADS_DIR = "uploads"


async def create_owner(
        parking_name: str,
        address: str,
        phone_number: str,
        lat: float,
        lng: float,
        id_proof: str,
        slots: int,
        file: UploadFile,
        current_user: User
):
    
    _, file_extension = os.path.splitext(file.filename)
    unique_filename = f"{uuid.uuid4()}{file_extension}"
    file_path = os.path.join(UPLOADS_DIR, unique_filename)

    # Firstly, we have to create the dir if not exists.
    Path(UPLOADS_DIR).mkdir(parents=True, exist_ok=True)

    try:
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    finally:
        file.file.close()
        
    # For creating a unique id for the parking spot.

    new_parking = ParkingDetails(
        user=current_user,
        uid=current_user.uid,
        parking_id=uuid.uuid4(),
        parking_name=parking_name,
        address=address,
        phone_number=phone_number,
        id_proof=id_proof,
        coordinates=Coordinates(type="Point", coordinates=[lng, lat]),
        verification_file_url=file_path,
        verification_status=VerificationStatus.PENDING,
        slots=slots
    )

    await new_parking.insert()

    return new_parking