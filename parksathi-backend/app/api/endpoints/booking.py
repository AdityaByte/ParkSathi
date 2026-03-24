"""This file mainly serves the booking routes."""
from http import HTTPStatus
from uuid import UUID, uuid4
import logging
from datetime import datetime


from fastapi import HTTPException

from app.model.booking import Booking, BookingStatus
from app.model.parking import ParkingDetails

async def acquire_booking(uid: str, parking_id: UUID) -> Booking:
    # Here we need to fetch the parking_details from the parking id and thus...
    parking_details = await ParkingDetails.find_one(ParkingDetails.parking_id == parking_id)
    if parking_details is None:
        logging.error(f"No parking details were found of the parking id {parking_id}")
        raise HTTPException(status_code=HTTPStatus.BAD_REQUEST, detail="No parking details were found of the parking id.")
    
    # Now if the details were found we need to make a booking id associated with the user id.
    # We don't need to check to the uid as of the authentication part is already done.
    # Each time a new booking is generated so we cannot upsert it.
    
    new_booking = Booking(
        uid = uid,
        booking_id = uuid4(),
        booking_status=BookingStatus.BOOKED,
        created_at=datetime.now(),
        parking_details=parking_details # linking is automatically handled by beanie.
    )
    
    # Now we just need to save the document.
    return new_booking